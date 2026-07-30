package com.numbericsuserportal.taxintake.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.taxintake.dto.ProconnectTaxIntakeRecordDto;
import com.numbericsuserportal.taxintake.entity.ProconnectTaxIntakeRecord;
import com.numbericsuserportal.taxintake.repo.ProconnectTaxIntakeRepository;
import com.numbericsuserportal.taxintake.service.ProconnectTaxIntakeService;
import com.numbericsuserportal.twilio.dto.Form1099ExtractedData;
import com.numbericsuserportal.twilio.dto.GovernmentIdExtractedData;
import com.numbericsuserportal.twilio.dto.W2ExtractedData;
import com.numbericsuserportal.twilio.impl.DocumentClassifierServiceImpl;
import com.numbericsuserportal.twilio.service.DataExtractionService;
import com.numbericsuserportal.twilio.service.DocumentClassifierService;
import com.numbericsuserportal.twilio.service.FileHandlerService;
import com.numbericsuserportal.twilio.service.OCRService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProconnectTaxIntakeServiceImpl implements ProconnectTaxIntakeService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int DEFAULT_DPI = 300;
    private static final List<String> TEMPLATE_HEADERS = List.of(
            "client_id", "first_name", "last_name", "ssn", "dob", "filing_status",
            "email", "phone", "address", "city", "state", "zip",
            "w2_employer", "w2_wages", "w2_federal_withholding",
            "form1099_type", "form1099_payer", "form1099_amount"
    );

    @Autowired
    private ProconnectTaxIntakeRepository intakeRepository;

    @Autowired
    private FileHandlerService fileHandlerService;

    @Autowired
    private OCRService ocrService;

    @Autowired
    private DocumentClassifierService documentClassifierService;

    @Autowired
    private DataExtractionService dataExtractionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ProconnectTaxIntakeRecordDto uploadAndExtract(MultipartFile file, User currentUser) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new RuntimeException("Authentication required");
        }
        validateFile(file);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        String contentType = file.getContentType();

        Path storedPath = saveUpload(file, currentUser.getUserId(), originalName);

        ProconnectTaxIntakeRecord record = new ProconnectTaxIntakeRecord();
        record.setUserId(currentUser.getUserId());
        record.setSoftwareName("PROCONNECT");
        record.setOriginalFileName(originalName);
        record.setStoredFilePath(storedPath.toString());
        record.setContentType(contentType);
        record.setFileSize(file.getSize());
        record.setProcessingStatus("FAILED");
        record.setCreatedBy(String.valueOf(currentUser.getUserId()));
        record.setModifiedBy(String.valueOf(currentUser.getUserId()));
        record.setIsActive(true);

        try (InputStream inputStream = Files.newInputStream(storedPath)) {
            String detectedType = fileHandlerService.detectFileType(contentType, originalName);
            if ("UNKNOWN".equals(detectedType)) {
                throw new RuntimeException("Unsupported file type. Only PDF, JPG, JPEG, PNG are supported.");
            }

            List<BufferedImage> images;
            if ("PDF".equals(detectedType)) {
                images = fileHandlerService.convertPdfToImages(inputStream, DEFAULT_DPI);
            } else {
                BufferedImage image = fileHandlerService.loadImage(inputStream);
                images = List.of(image);
            }

            if (images.isEmpty()) {
                throw new RuntimeException("Could not parse document pages.");
            }

            List<String> extractedTexts = ocrService.extractTextFromImages(images);
            String rawText = extractedTexts.stream()
                    .filter(t -> t != null && !t.isBlank())
                    .collect(Collectors.joining("\n\n--- Page Break ---\n\n"));

            if (rawText == null || rawText.isBlank()) {
                throw new RuntimeException("OCR did not extract text from file.");
            }

            String documentType = documentClassifierService.classifyDocument(rawText);
            Object extractedData = extractStructuredData(documentType, rawText, images);

            record.setRawText(rawText);
            record.setDocumentType(documentType);
            record.setExtractedDataJson(toJson(extractedData));
            record.setPageCount(images.size());
            record.setOverallConfidence(resolveConfidence(extractedData));
            record.setProcessingStatus("SUCCESS");
            record.setErrorMessage(null);
        } catch (Exception e) {
            record.setErrorMessage(e.getMessage());
            record.setProcessingStatus("FAILED");
        }

        return toDto(intakeRepository.save(record));
    }

    @Override
    @Transactional
    public ProconnectTaxIntakeRecordDto uploadSpreadsheet(MultipartFile file, User currentUser) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new RuntimeException("Authentication required");
        }
        validateFile(file);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "sheet";
        String ext = fileExtension(originalName);
        if (!List.of(".csv", ".xlsx", ".xls").contains(ext)) {
            throw new RuntimeException("Only CSV/XLS/XLSX sheets are supported for intake sheet upload.");
        }

        Path storedPath = saveUpload(file, currentUser.getUserId(), originalName);

        ProconnectTaxIntakeRecord record = new ProconnectTaxIntakeRecord();
        record.setUserId(currentUser.getUserId());
        record.setSoftwareName("PROCONNECT");
        record.setOriginalFileName(originalName);
        record.setStoredFilePath(storedPath.toString());
        record.setContentType(file.getContentType());
        record.setFileSize(file.getSize());
        record.setDocumentType("INTAKE_SHEET");
        record.setPageCount(1);
        record.setProcessingStatus("FAILED");
        record.setCreatedBy(String.valueOf(currentUser.getUserId()));
        record.setModifiedBy(String.valueOf(currentUser.getUserId()));
        record.setIsActive(true);

        try {
            Map<String, Object> parsed = parseSheet(storedPath, ext);
            record.setExtractedDataJson(toJson(parsed));
            record.setOverallConfidence(1.0);
            record.setRawText(null);
            record.setProcessingStatus("SUCCESS");
            record.setErrorMessage(null);
        } catch (Exception e) {
            record.setProcessingStatus("FAILED");
            record.setErrorMessage(e.getMessage());
        }

        return toDto(intakeRepository.save(record));
    }

    @Override
    public List<ProconnectTaxIntakeRecordDto> listMyRecords(User currentUser) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new RuntimeException("Authentication required");
        }
        return intakeRepository.findByUserIdAndIsActiveTrueOrderByCreatedOnDesc(currentUser.getUserId())
                .stream().map(this::toSummaryDto).toList();
    }

    @Override
    public ProconnectTaxIntakeRecordDto getRecord(Long id, User currentUser) {
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new RuntimeException("Authentication required");
        }
        ProconnectTaxIntakeRecord record = intakeRepository.findByIdAndUserIdAndIsActiveTrue(id, currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Tax intake record not found: " + id));
        return toDto(record);
    }

    @Override
    public byte[] buildCsvTemplate() {
        String csv = String.join(",", TEMPLATE_HEADERS) + "\n";
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] buildExcelTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("proconnect_intake");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
                header.createCell(i).setCellValue(TEMPLATE_HEADERS.get(i));
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build Excel template", e);
        }
    }

    private static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds 10MB");
        }
    }

    private static Path saveUpload(MultipartFile file, Long userId, String originalName) {
        try {
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0 && dot < originalName.length() - 1) {
                ext = originalName.substring(dot).toLowerCase();
            }
            String safeName = UUID.randomUUID() + ext;
            Path dir = Paths.get("uploads", "tax-intake", "proconnect", String.valueOf(userId));
            Files.createDirectories(dir);
            Path target = dir.resolve(safeName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store upload", e);
        }
    }

    private Map<String, Object> parseSheet(Path filePath, String ext) throws IOException {
        if (".csv".equals(ext)) {
            return parseCsv(filePath);
        }
        return parseExcel(filePath);
    }

    private Map<String, Object> parseCsv(Path filePath) throws IOException {
        try (CSVParser parser = CSVParser.parse(
                Files.newBufferedReader(filePath, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            List<String> headers = parser.getHeaderNames();
            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord rec : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String h : headers) {
                    row.put(h, rec.get(h));
                }
                rows.add(row);
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("format", "CSV");
            payload.put("headers", headers);
            payload.put("rowCount", rows.size());
            payload.put("rows", rows);
            return payload;
        }
    }

    private Map<String, Object> parseExcel(Path filePath) throws IOException {
        try (InputStream in = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new RuntimeException("Excel sheet is empty.");
            }
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new RuntimeException("Excel header row is missing.");
            }
            DataFormatter formatter = new DataFormatter();
            List<String> headers = new ArrayList<>();
            int maxCol = headerRow.getLastCellNum();
            for (int c = 0; c < maxCol; c++) {
                headers.add(formatter.formatCellValue(headerRow.getCell(c)));
            }
            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row rowObj = sheet.getRow(r);
                if (rowObj == null) {
                    continue;
                }
                Map<String, String> row = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = rowObj.getCell(c);
                    String value = formatter.formatCellValue(cell);
                    if (value != null && !value.isBlank()) {
                        hasValue = true;
                    }
                    row.put(headers.get(c), value);
                }
                if (hasValue) {
                    rows.add(row);
                }
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("format", "EXCEL");
            payload.put("sheetName", sheet.getSheetName());
            payload.put("headers", headers);
            payload.put("rowCount", rows.size());
            payload.put("rows", rows);
            return payload;
        } catch (Exception e) {
            throw new RuntimeException("Could not parse Excel file: " + e.getMessage(), e);
        }
    }

    private static String fileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot).toLowerCase();
    }

    private Object extractStructuredData(String documentType, String rawText, List<BufferedImage> images) {
        return switch (documentType) {
            case DocumentClassifierServiceImpl.DOC_TYPE_W2 -> {
                W2ExtractedData w2 = ocrService.extractW2DataStructured(images);
                boolean hasKeyFields = w2.getEmployeeName() != null || w2.getSsn() != null
                        || w2.getEmployerName() != null || w2.getEin() != null;
                if (!hasKeyFields) {
                    yield dataExtractionService.extractW2Data(rawText);
                }
                yield w2;
            }
            case DocumentClassifierServiceImpl.DOC_TYPE_1099_NEC ->
                    dataExtractionService.extract1099Data(rawText, "1099-NEC");
            case DocumentClassifierServiceImpl.DOC_TYPE_1099_MISC ->
                    dataExtractionService.extract1099Data(rawText, "1099-MISC");
            case DocumentClassifierServiceImpl.DOC_TYPE_GOVERNMENT_ID -> {
                GovernmentIdExtractedData gov = ocrService.extractGovernmentIdDataStructured(images);
                boolean missingEssentials = gov.getIdNumber() == null || gov.getIdNumber().isBlank();
                if (missingEssentials) {
                    yield dataExtractionService.extractGovernmentIdData(rawText);
                }
                yield gov;
            }
            default -> buildFallbackExtractionBundle(rawText);
        };
    }

    private Map<String, Object> buildFallbackExtractionBundle(String rawText) {
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("strategy", "fallback_multi_extract");
        bundle.put("w2Guess", dataExtractionService.extractW2Data(rawText));
        bundle.put("form1099NecGuess", dataExtractionService.extract1099Data(rawText, "1099-NEC"));
        bundle.put("form1099MiscGuess", dataExtractionService.extract1099Data(rawText, "1099-MISC"));
        bundle.put("governmentIdGuess", dataExtractionService.extractGovernmentIdData(rawText));
        return bundle;
    }

    private String toJson(Object extractedData) {
        if (extractedData == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(extractedData);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double resolveConfidence(Object extractedData) {
        if (extractedData instanceof W2ExtractedData w2) {
            return normalize(w2.getOverallConfidence());
        }
        if (extractedData instanceof Form1099ExtractedData f1099) {
            return normalize(f1099.getOverallConfidence());
        }
        if (extractedData instanceof GovernmentIdExtractedData gov) {
            return normalize(gov.getOverallConfidence());
        }
        if (extractedData instanceof Map<?, ?> map) {
            double max = 0.0;
            for (Object value : map.values()) {
                Double c = resolveConfidence(value);
                if (c != null && c > max) {
                    max = c;
                }
            }
            return max;
        }
        return 0.0;
    }

    private static Double normalize(Double value) {
        return value == null ? 0.0 : value;
    }

    private ProconnectTaxIntakeRecordDto toDto(ProconnectTaxIntakeRecord record) {
        return new ProconnectTaxIntakeRecordDto(
                record.getId(),
                record.getSoftwareName(),
                record.getOriginalFileName(),
                record.getContentType(),
                record.getFileSize(),
                record.getDocumentType(),
                record.getRawText(),
                record.getExtractedDataJson(),
                record.getOverallConfidence(),
                record.getPageCount(),
                record.getProcessingStatus(),
                record.getErrorMessage(),
                record.getCreatedOn(),
                record.getCreatedBy()
        );
    }

    private ProconnectTaxIntakeRecordDto toSummaryDto(ProconnectTaxIntakeRecord record) {
        return new ProconnectTaxIntakeRecordDto(
                record.getId(),
                record.getSoftwareName(),
                record.getOriginalFileName(),
                record.getContentType(),
                record.getFileSize(),
                record.getDocumentType(),
                null,
                record.getExtractedDataJson(),
                record.getOverallConfidence(),
                record.getPageCount(),
                record.getProcessingStatus(),
                record.getErrorMessage(),
                record.getCreatedOn(),
                record.getCreatedBy()
        );
    }
}
