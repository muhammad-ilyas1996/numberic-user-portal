package com.numbericsuserportal.recieptupload.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.recieptupload.dto.ReceiptDataResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveRequestDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSearch;
import com.numbericsuserportal.recieptupload.dto.ReceiptUploadResponseDTO;
import com.numbericsuserportal.recieptupload.entity.Receipt;
import com.numbericsuserportal.recieptupload.entity.ReceiptData;
import com.numbericsuserportal.recieptupload.respository.ReceiptDataRepository;
import com.numbericsuserportal.recieptupload.respository.ReceiptRepository;
import com.numbericsuserportal.recieptupload.service.ReceiptDataExtractor;
import com.numbericsuserportal.recieptupload.service.ReceiptService;
import com.numbericsuserportal.twilio.service.OCRService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@Slf4j
public class ReceiptServiceImpl implements ReceiptService {
    
    private static final String UPLOAD_DIR = "uploads/receipts/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/jpg"};
    
    @Autowired
    private ReceiptRepository receiptRepository;
    
    @Autowired
    private ReceiptDataRepository receiptDataRepository;
    
    @Autowired
    private OCRService ocrService;
    
    @Autowired
    private ReceiptDataExtractor dataExtractor;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public ReceiptUploadResponseDTO uploadReceipt(MultipartFile file, Long userId) {
        ReceiptUploadResponseDTO response = new ReceiptUploadResponseDTO();
        response.setSuccess(false);
        
        try {
            // Validate file
            validateFile(file);
            
            // Save file to disk
            String imagePath = saveFile(file, userId);
            
            // Create Receipt entity (no processingStatus or entryType - moved to ReceiptData)
            Receipt receipt = new Receipt();
            receipt.setUserId(userId);
            receipt.setImagePath(imagePath);
            receipt.setOriginalFilename(file.getOriginalFilename());
            receipt = receiptRepository.save(receipt);
            
            try {
                // Convert MultipartFile to BufferedImage for OCR
                BufferedImage image = convertToBufferedImage(file);
                
                // Extract text using OCR
                String extractedText = ocrService.extractText(image);
                log.info("OCR extraction completed. Text length: {}", extractedText.length());
                
                // Get confidence score
                Double confidenceScore = ocrService.getConfidenceScore(image);
                
                // Extract structured data
                String merchantName = dataExtractor.extractMerchantName(extractedText);
                java.time.LocalDate receiptDate = dataExtractor.extractDate(extractedText);
                BigDecimal totalAmount = dataExtractor.extractTotalAmount(extractedText);
                BigDecimal taxAmount = dataExtractor.extractTaxAmount(extractedText);
                
                // NOTE: ReceiptData is NOT saved here - it will be saved when user clicks save
                
                // Read image file and convert to base64
                String imageBase64 = convertImageToBase64(imagePath, file.getContentType());
                
                // Build response
                response.setSuccess(true);
                response.setMessage("Receipt uploaded and data extracted successfully. Please review and save the data.");
                response.setReceiptId(receipt.getId());
                response.setMerchantName(merchantName);
                response.setDate(receiptDate);
                response.setTotalAmount(totalAmount);
                response.setTaxAmount(taxAmount);
                response.setImageBase64(imageBase64);
                response.setConfidenceScore(confidenceScore);
                
                log.info("Receipt processed successfully. Receipt ID: {}", receipt.getId());
                
            } catch (Exception e) {
                log.error("Error processing OCR for receipt ID: {}", receipt.getId(), e);
                
                // Still return response with saved receipt info
                response.setSuccess(false);
                response.setMessage("Receipt uploaded but OCR processing failed: " + e.getMessage());
                response.setReceiptId(receipt.getId());
                
                // Try to get base64 image even if OCR failed
                try {
                    String imageBase64 = convertImageToBase64(imagePath, file.getContentType());
                    response.setImageBase64(imageBase64);
                } catch (Exception ex) {
                    log.error("Error converting image to base64", ex);
                }
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            response.setMessage("Validation error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading receipt", e);
            response.setMessage("Error uploading receipt: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
        
        String contentType = file.getContentType();
        boolean isAllowed = false;
        if (contentType != null) {
            for (String allowedType : ALLOWED_TYPES) {
                if (allowedType.equalsIgnoreCase(contentType)) {
                    isAllowed = true;
                    break;
                }
            }
        }
        
        if (!isAllowed) {
            throw new IllegalArgumentException("File type not allowed. Only JPEG and PNG are allowed");
        }
    }
    
    /**
     * Save file to disk and return relative path
     */
    private String saveFile(MultipartFile file, Long userId) throws IOException {
        // Create directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR + userId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path
        return UPLOAD_DIR + userId + "/" + filename;
    }
    
    /**
     * Convert MultipartFile to BufferedImage
     */
    private BufferedImage convertToBufferedImage(MultipartFile file) throws IOException {
        return ImageIO.read(file.getInputStream());
    }
    
    /**
     * Convert image file to base64 encoded string
     */
    private String convertImageToBase64(String imagePath, String contentType) throws IOException {
        Path filePath = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(filePath);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        
        // Determine MIME type
        String mimeType = "image/jpeg";
        if (contentType != null && contentType.startsWith("image/")) {
            mimeType = contentType;
        } else if (imagePath.toLowerCase().endsWith(".png")) {
            mimeType = "image/png";
        }
        
        return "data:" + mimeType + ";base64," + base64;
    }
    
    @Override
    @Transactional
    public ReceiptUploadResponseDTO uploadReceiptManual(MultipartFile file, Long userId) {
        ReceiptUploadResponseDTO response = new ReceiptUploadResponseDTO();
        response.setSuccess(false);
        
        try {
            // Validate file
            validateFile(file);
            
            // Save file to disk
            String imagePath = saveFile(file, userId);
            
            // Create Receipt entity (no processingStatus or entryType - moved to ReceiptData)
            Receipt receipt = new Receipt();
            receipt.setUserId(userId);
            receipt.setImagePath(imagePath);
            receipt.setOriginalFilename(file.getOriginalFilename());
            receipt = receiptRepository.save(receipt);
            
            // Read image file and convert to base64
            String imageBase64 = convertImageToBase64(imagePath, file.getContentType());
            
            // Build response (no extracted data for manual entry)
            response.setSuccess(true);
            response.setMessage("Receipt uploaded for manual entry. Please enter data manually.");
            response.setReceiptId(receipt.getId());
            response.setImageBase64(imageBase64);
            // All other fields (merchantName, date, totalAmount, etc.) will be null
            // User will enter them manually
            
            log.info("Receipt uploaded for manual entry. Receipt ID: {}", receipt.getId());
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            response.setMessage("Validation error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading receipt for manual entry", e);
            response.setMessage("Error uploading receipt: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional
    public ReceiptSaveResponseDTO saveReceiptData(ReceiptSaveRequestDTO saveRequest, MultipartFile file) {
        ReceiptSaveResponseDTO response = new ReceiptSaveResponseDTO();
        response.setSuccess(false);
        
        try {
            // Validate entryType
            if (saveRequest.getEntryType() == null || 
                (!saveRequest.getEntryType().equals("MANUAL") && !saveRequest.getEntryType().equals("OCR"))) {
                throw new IllegalArgumentException("Entry Type must be MANUAL or OCR");
            }
            
            Receipt receipt;
            Long userId;
            
            // For manual entry, file is required
            if ("MANUAL".equals(saveRequest.getEntryType())) {
                if (file == null || file.isEmpty()) {
                    throw new IllegalArgumentException("File is required for manual entry");
                }
                
                // Validate file
                validateFile(file);
                
                // Get userId
                userId = saveRequest.getUserId();
                if (userId == null) {
                    // Try to get from existing receipt if receiptId provided
                    if (saveRequest.getReceiptId() != null) {
                        Receipt existingReceipt = receiptRepository.findById(saveRequest.getReceiptId()).orElse(null);
                        if (existingReceipt != null) {
                            userId = existingReceipt.getUserId();
                        }
                    }
                    if (userId == null) {
                        throw new IllegalArgumentException("User ID is required for manual entry");
                    }
                }
                
                // Save file to disk
                String imagePath = saveFile(file, userId);
                
                // Check if receipt exists or create new
                if (saveRequest.getReceiptId() != null) {
                    receipt = receiptRepository.findById(saveRequest.getReceiptId()).orElse(null);
                    if (receipt != null) {
                        // Update existing receipt
                        receipt.setImagePath(imagePath);
                        receipt.setOriginalFilename(file.getOriginalFilename());
                    } else {
                        // Create new receipt with provided receiptId (if valid)
                        receipt = new Receipt();
                        receipt.setUserId(userId);
                        receipt.setImagePath(imagePath);
                        receipt.setOriginalFilename(file.getOriginalFilename());
                    }
                } else {
                    // Create new receipt
                    receipt = new Receipt();
                    receipt.setUserId(userId);
                    receipt.setImagePath(imagePath);
                    receipt.setOriginalFilename(file.getOriginalFilename());
                }
                
                receipt = receiptRepository.save(receipt);
                saveRequest.setReceiptId(receipt.getId()); // Update receiptId if new receipt created
                
            } else {
                // OCR entry - receipt should already exist
                if (saveRequest.getReceiptId() == null) {
                    throw new IllegalArgumentException("Receipt ID is required for OCR entry");
                }
                
                receipt = receiptRepository.findById(saveRequest.getReceiptId())
                    .orElseThrow(() -> new IllegalArgumentException("Receipt not found with ID: " + saveRequest.getReceiptId()));
                
                userId = receipt.getUserId();
            }
            
            // Check if ReceiptData already exists (update) or create new
            ReceiptData receiptData = receiptDataRepository.findByReceiptId(saveRequest.getReceiptId())
                .orElse(new ReceiptData());
            
            // Set receipt ID, user ID, and entry type
            receiptData.setReceiptId(saveRequest.getReceiptId());
            receiptData.setUserId(userId);
            receiptData.setEntryType(saveRequest.getEntryType());
            
            // Set user-entered data
            receiptData.setMerchantName(saveRequest.getMerchantName());
            receiptData.setReceiptDate(saveRequest.getDate());
            receiptData.setTotalAmount(saveRequest.getTotalAmount());
            receiptData.setTaxAmount(saveRequest.getTaxAmount());
            receiptData.setCategory(saveRequest.getCategory());
            receiptData.setStatus(saveRequest.getStatus());
            
            // Save or update ReceiptData
            receiptDataRepository.save(receiptData);
            
            response.setSuccess(true);
            response.setMessage("Receipt data saved successfully");
            response.setReceiptId(saveRequest.getReceiptId());
            
            log.info("Receipt data saved successfully. Receipt ID: {}, Entry Type: {}", 
                     saveRequest.getReceiptId(), saveRequest.getEntryType());
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            response.setMessage("Validation error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error saving receipt data", e);
            response.setMessage("Error saving receipt data: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public List<ReceiptDataResponseDTO> getUserReceipts(Long userId) {
        // Get all receipt data for user (both MANUAL and OCR)
        List<ReceiptData> receiptDataList = receiptDataRepository.findByUserId(userId);
        
        return receiptDataList.stream().map(receiptData -> {
            ReceiptDataResponseDTO dto = new ReceiptDataResponseDTO();
            
            // Map all receipt_data table columns
            dto.setId(receiptData.getId());
            dto.setReceiptId(receiptData.getReceiptId());
            dto.setUserId(receiptData.getUserId());
            dto.setEntryType(receiptData.getEntryType());
            dto.setMerchantName(receiptData.getMerchantName());
            dto.setReceiptDate(receiptData.getReceiptDate());
            dto.setTotalAmount(receiptData.getTotalAmount());
            dto.setTaxAmount(receiptData.getTaxAmount());
            dto.setRawText(receiptData.getRawText());
            dto.setConfidenceScore(receiptData.getConfidenceScore());
            dto.setCategory(receiptData.getCategory());
            dto.setStatus(receiptData.getStatus());
            dto.setExtractedAt(receiptData.getExtractedAt());
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Page<ReceiptDataResponseDTO> getUserReceipts(Long userId, ReceiptSearch search) {
        // Get paginated receipt data for user (both MANUAL and OCR)
        // Invoice module style pagination: pageNumber-1 for 0-indexed, default sort by extractedAt DESC
        
        // Default values if not provided
        int pageNumber = (search.getPageNumber() != null && search.getPageNumber() > 0) 
            ? search.getPageNumber() - 1  // Convert to 0-indexed
            : 0;
        int pageSize = (search.getPageSize() != null && search.getPageSize() > 0) 
            ? search.getPageSize() 
            : 10;
        
        // Create PageRequest with sorting (default: extractedAt DESC)
        PageRequest pageRequest = PageRequest.of(
            pageNumber,
            pageSize,
            Sort.Direction.DESC,
            "extractedAt"
        );
        
        Page<ReceiptData> receiptDataPage = receiptDataRepository.findByUserId(userId, pageRequest);
        
        // Convert to DTOs
        List<ReceiptDataResponseDTO> dtoList = receiptDataPage.getContent().stream().map(receiptData -> {
            ReceiptDataResponseDTO dto = new ReceiptDataResponseDTO();
            
            // Map all receipt_data table columns
            dto.setId(receiptData.getId());
            dto.setReceiptId(receiptData.getReceiptId());
            dto.setUserId(receiptData.getUserId());
            dto.setEntryType(receiptData.getEntryType());
            dto.setMerchantName(receiptData.getMerchantName());
            dto.setReceiptDate(receiptData.getReceiptDate());
            dto.setTotalAmount(receiptData.getTotalAmount());
            dto.setTaxAmount(receiptData.getTaxAmount());
            dto.setRawText(receiptData.getRawText());
            dto.setConfidenceScore(receiptData.getConfidenceScore());
            dto.setCategory(receiptData.getCategory());
            dto.setStatus(receiptData.getStatus());
            dto.setExtractedAt(receiptData.getExtractedAt());
            
            return dto;
        }).collect(Collectors.toList());
        
        // Return as Page (Invoice module style)
        return new PageImpl<>(dtoList, pageRequest, receiptDataPage.getTotalElements());
    }
}
