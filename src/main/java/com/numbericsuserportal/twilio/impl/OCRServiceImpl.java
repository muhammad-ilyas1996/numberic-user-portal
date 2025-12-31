package com.numbericsuserportal.twilio.impl;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.DocumentField;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.numbericsuserportal.twilio.dto.GovernmentIdExtractedData;
import com.numbericsuserportal.twilio.dto.W2ExtractedData;
import com.numbericsuserportal.twilio.service.OCRService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for OCR operations using Azure Document Intelligence
 */
@Service
public class OCRServiceImpl implements OCRService {
    
    @Value("${azure.document.intelligence.endpoint:}")
    private String endpoint;
    
    @Value("${azure.document.intelligence.key:}")
    private String apiKey;
    
    private DocumentIntelligenceClient client;
    
    /**
     * Initialize Azure Document Intelligence client
     */
    private void initializeClient() {
        if (client != null) {
            return; // Already initialized
        }
        
        if (endpoint == null || endpoint.trim().isEmpty() || 
            apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("Azure Document Intelligence endpoint and key must be configured in application.properties. " +
                    "Please set azure.document.intelligence.endpoint and azure.document.intelligence.key");
        }
        
        try {
            client = new DocumentIntelligenceClientBuilder()
                    .endpoint(endpoint)
                    .credential(new AzureKeyCredential(apiKey))
                    .buildClient();
            
            System.out.println("Azure Document Intelligence client initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing Azure Document Intelligence client: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Azure Document Intelligence client", e);
        }
    }
    
    @Override
    public String extractText(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        
        // Lazy initialization
        if (client == null) {
            initializeClient();
        }
        
        try {
            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Analyze document using Azure Document Intelligence
            // Using "prebuilt-read" model for general text extraction
            AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(BinaryData.fromBytes(imageBytes));
            AnalyzeResult analyzeResult = client.beginAnalyzeDocument("prebuilt-read", options).getFinalResult();

            // Extract text from all pages
            StringBuilder extractedText = new StringBuilder();
            
            if (analyzeResult.getPages() != null) {
                for (var page : analyzeResult.getPages()) {
                    if (page.getLines() != null) {
                        for (var line : page.getLines()) {
                            if (line.getContent() != null) {
                                extractedText.append(line.getContent()).append("\n");
                            }
                        }
                    }
                }
            }
            
            // Also try to get content from paragraphs if available (more comprehensive)
            if (analyzeResult.getContent() != null && !analyzeResult.getContent().isEmpty()) {
                String content = analyzeResult.getContent();
                // Use content if it's more comprehensive than lines
                if (content.length() > extractedText.length()) {
                    extractedText = new StringBuilder(content);
                }
            }
            
            String result = extractedText.toString().trim();
            System.out.println("Azure Document Intelligence extraction completed. Text length: " + result.length());
            return result;
            
        } catch (IOException e) {
            System.err.println("Error converting image to bytes: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to convert image: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Azure Document Intelligence error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to extract text from image: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<String> extractTextFromImages(List<BufferedImage> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> extractedTexts = new ArrayList<>();
        
        // Lazy initialization
        if (client == null) {
            initializeClient();
        }
        
        // Process each image separately
        // Azure Document Intelligence can handle multi-page documents, but for consistency
        // with the existing interface, we'll process each page separately
        for (int i = 0; i < images.size(); i++) {
            BufferedImage image = images.get(i);
            try {
                // Convert BufferedImage to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", baos);
                byte[] imageBytes = baos.toByteArray();
                
                // Analyze document using Azure Document Intelligence
                AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(BinaryData.fromBytes(imageBytes));
                AnalyzeResult analyzeResult = client.beginAnalyzeDocument("prebuilt-read", options).getFinalResult();
                
                // Extract text from result
                String extractedText = extractTextFromResult(analyzeResult);
                extractedTexts.add(extractedText);
                System.out.println("Extracted text from page " + (i + 1) + " of " + images.size());
                
            } catch (IOException e) {
                System.err.println("Error converting image to bytes for page " + (i + 1) + ": " + e.getMessage());
                extractedTexts.add(""); // Add empty string for failed page
            } catch (Exception e) {
                System.err.println("Error extracting text from page " + (i + 1) + ": " + e.getMessage());
                e.printStackTrace();
                extractedTexts.add(""); // Add empty string for failed page
            }
        }
        
        return extractedTexts;
    }
    
    /**
     * Extract text from Azure Document Intelligence AnalyzeResult
     */
    private String extractTextFromResult(AnalyzeResult analyzeResult) {
        StringBuilder extractedText = new StringBuilder();
        
        if (analyzeResult.getPages() != null) {
            for (var page : analyzeResult.getPages()) {
                if (page.getLines() != null) {
                    for (var line : page.getLines()) {
                        if (line.getContent() != null) {
                            extractedText.append(line.getContent()).append("\n");
                        }
                    }
                }
            }
        }
        
        // Also try to get content from paragraphs if available (more comprehensive)
        if (analyzeResult.getContent() != null && !analyzeResult.getContent().isEmpty()) {
            String content = analyzeResult.getContent();
            // Use content if it's more comprehensive than lines
            if (content.length() > extractedText.length()) {
                extractedText = new StringBuilder(content);
            }
        }
        
        return extractedText.toString().trim();
    }
    
    @Override
    public Double getConfidenceScore(BufferedImage image) {
        if (image == null) {
            return 0.0;
        }
        
        // Lazy initialization
        if (client == null) {
            initializeClient();
        }
        
        try {
            String extractedText = extractText(image);
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                return 0.0;
            }
            
            // Azure Document Intelligence provides high accuracy for OCR
            // We can use a higher base confidence
            int textLength = extractedText.trim().length();
            int alphanumericCount = 0;
            
            for (char c : extractedText.toCharArray()) {
                if (Character.isLetterOrDigit(c)) {
                    alphanumericCount++;
                }
            }
            
            double alphanumericRatio = textLength > 0 ? (double) alphanumericCount / textLength : 0.0;
            
            // Higher confidence for Azure Document Intelligence (0.0 to 1.0)
            // Azure provides high accuracy for document text extraction
            double confidence = Math.min(1.0, alphanumericRatio * 0.9 + (textLength > 50 ? 0.1 : 0.0));
            
            return confidence;
        } catch (Exception e) {
            System.err.println("Error calculating confidence score: " + e.getMessage());
            return 0.0;
        }
    }
    
    @Override
    public W2ExtractedData extractW2DataStructured(List<BufferedImage> images) {
        if (images == null || images.isEmpty()) {
            return createEmptyW2Data();
        }
        
        // Lazy initialization
        if (client == null) {
            initializeClient();
        }
        
        try {
            // Use first image (W-2 is typically single page)
            BufferedImage image = images.get(0);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Use Azure pre-built W-2 model for structured extraction
            AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(BinaryData.fromBytes(imageBytes));
            AnalyzeResult analyzeResult = client.beginAnalyzeDocument("prebuilt-tax.us.w2", options).getFinalResult();
            
            // Extract structured data from Azure result
            W2ExtractedData w2Data = new W2ExtractedData();
            Map<String, W2ExtractedData.BoxValue> boxValues = new HashMap<>();
            
            if (analyzeResult.getDocuments() != null && !analyzeResult.getDocuments().isEmpty()) {
                var document = analyzeResult.getDocuments().get(0);
                Map<String, DocumentField> fields = document.getFields();
                
                // Debug: Log all available field names to understand Azure response structure
                System.out.println("DEBUG: Azure W-2 fields found: " + (fields != null ? fields.keySet() : "null"));
                
                // Extract employee information - try multiple field name variations
                // Azure SDK field names may vary, so we check multiple possibilities
                String employeeName = null;
                for (String fieldName : new String[]{"EmployeeName", "Employee", "EmployeeName", "Name"}) {
                    if (fields.containsKey(fieldName)) {
                        employeeName = getFieldValueAsString(fields.get(fieldName));
                        if (employeeName != null && !employeeName.trim().isEmpty()) {
                            w2Data.setEmployeeName(employeeName);
                            w2Data.setEmployeeNameConfidence(getFieldConfidence(fields.get(fieldName)));
                            System.out.println("DEBUG: Found employee name in field: " + fieldName + " = " + employeeName);
                            break;
                        }
                    }
                }
                
                // Employee SSN - try multiple field name variations
                String ssn = null;
                for (String fieldName : new String[]{"EmployeeSocialSecurityNumber", "SocialSecurityNumber", "SSN", "EmployeeSSN"}) {
                    if (fields.containsKey(fieldName)) {
                        DocumentField ssnField = fields.get(fieldName);
                        if (ssnField != null) {
                            ssn = getFieldValueAsString(ssnField);
                            if (ssn != null && !ssn.trim().isEmpty()) {
                                w2Data.setSsn(ssn);
                                w2Data.setSsnConfidence(getFieldConfidence(ssnField));
                                System.out.println("DEBUG: Found SSN in field: " + fieldName + " = " + ssn);
                                break;
                            }
                        }
                    }
                }
                
                // Employee address - try multiple field name variations
                String employeeAddress = null;
                for (String fieldName : new String[]{"EmployeeAddress", "Address", "EmployeeAddressAndZip"}) {
                    if (fields.containsKey(fieldName)) {
                        DocumentField addrField = fields.get(fieldName);
                        if (addrField != null) {
                            employeeAddress = getFieldValueAsString(addrField);
                            if (employeeAddress != null && !employeeAddress.trim().isEmpty()) {
                                w2Data.setAddress(employeeAddress);
                                w2Data.setAddressConfidence(getFieldConfidence(addrField));
                                System.out.println("DEBUG: Found employee address in field: " + fieldName);
                                break;
                            }
                        }
                    }
                }
                
                // Extract employer information - try multiple field name variations
                // Employer name
                String employerName = null;
                for (String fieldName : new String[]{"EmployerName", "Employer", "EmployerNameAddressAndZip"}) {
                    if (fields.containsKey(fieldName)) {
                        employerName = getFieldValueAsString(fields.get(fieldName));
                        if (employerName != null && !employerName.trim().isEmpty()) {
                            w2Data.setEmployerName(employerName);
                            w2Data.setEmployerNameConfidence(getFieldConfidence(fields.get(fieldName)));
                            System.out.println("DEBUG: Found employer name in field: " + fieldName + " = " + employerName);
                            break;
                        }
                    }
                }
                
                // Employer EIN
                if (fields.containsKey("EmployerIdNumber") || fields.containsKey("EIN")) {
                    DocumentField einField = fields.get("EmployerIdNumber");
                    if (einField == null) {
                        einField = fields.get("EIN");
                    }
                    if (einField != null) {
                        String ein = getFieldValueAsString(einField);
                        w2Data.setEin(ein);
                        w2Data.setEinConfidence(getFieldConfidence(einField));
                    }
                }
                
                // Employer address
                if (fields.containsKey("EmployerAddress")) {
                    String address = getFieldValueAsString(fields.get("EmployerAddress"));
                    w2Data.setEmployerAddress(address);
                    w2Data.setEmployerAddressConfidence(getFieldConfidence(fields.get("EmployerAddress")));
                }
                
                // Extract tax year
                if (fields.containsKey("TaxYear")) {
                    String taxYear = getFieldValueAsString(fields.get("TaxYear"));
                    w2Data.setTaxYear(taxYear);
                    w2Data.setTaxYearConfidence(getFieldConfidence(fields.get("TaxYear")));
                }
                
                // Extract control number
                if (fields.containsKey("ControlNumber")) {
                    String controlNumber = getFieldValueAsString(fields.get("ControlNumber"));
                    w2Data.setControlNumber(controlNumber);
                    w2Data.setControlNumberConfidence(getFieldConfidence(fields.get("ControlNumber")));
                }
                
                // Extract box values
                if (fields.containsKey("WagesTipsAndOtherCompensation")) {
                    String box1 = getFieldValueAsString(fields.get("WagesTipsAndOtherCompensation"));
                    boxValues.put("1", new W2ExtractedData.BoxValue(box1, getFieldConfidence(fields.get("WagesTipsAndOtherCompensation"))));
                }
                
                if (fields.containsKey("FederalIncomeTaxWithheld")) {
                    String box2 = getFieldValueAsString(fields.get("FederalIncomeTaxWithheld"));
                    boxValues.put("2", new W2ExtractedData.BoxValue(box2, getFieldConfidence(fields.get("FederalIncomeTaxWithheld"))));
                }
                
                if (fields.containsKey("SocialSecurityWages")) {
                    String box3 = getFieldValueAsString(fields.get("SocialSecurityWages"));
                    boxValues.put("3", new W2ExtractedData.BoxValue(box3, getFieldConfidence(fields.get("SocialSecurityWages"))));
                }
                
                if (fields.containsKey("SocialSecurityTaxWithheld")) {
                    String box4 = getFieldValueAsString(fields.get("SocialSecurityTaxWithheld"));
                    boxValues.put("4", new W2ExtractedData.BoxValue(box4, getFieldConfidence(fields.get("SocialSecurityTaxWithheld"))));
                }
                
                if (fields.containsKey("MedicareWagesAndTips")) {
                    String box5 = getFieldValueAsString(fields.get("MedicareWagesAndTips"));
                    boxValues.put("5", new W2ExtractedData.BoxValue(box5, getFieldConfidence(fields.get("MedicareWagesAndTips"))));
                }
                
                if (fields.containsKey("MedicareTaxWithheld")) {
                    String box6 = getFieldValueAsString(fields.get("MedicareTaxWithheld"));
                    boxValues.put("6", new W2ExtractedData.BoxValue(box6, getFieldConfidence(fields.get("MedicareTaxWithheld"))));
                }
                
                // Add more box values as needed
                w2Data.setBoxValues(boxValues);
                
                // Calculate overall confidence
                double overallConf = calculateOverallConfidenceW2(w2Data);
                w2Data.setOverallConfidence(overallConf);
                
                // Check if we got meaningful data - if most fields are null, Azure extraction might have failed
                int extractedFieldCount = 0;
                if (w2Data.getEmployeeName() != null) extractedFieldCount++;
                if (w2Data.getSsn() != null) extractedFieldCount++;
                if (w2Data.getAddress() != null) extractedFieldCount++;
                if (w2Data.getEmployerName() != null) extractedFieldCount++;
                if (w2Data.getEin() != null) extractedFieldCount++;
                
                System.out.println("DEBUG: Azure W-2 extraction - extracted " + extractedFieldCount + " key fields");
                
                // If we got very few fields, return empty data to trigger regex fallback
                if (extractedFieldCount < 2 && (w2Data.getBoxValues() == null || w2Data.getBoxValues().isEmpty())) {
                    System.out.println("WARNING: Azure W-2 extraction returned minimal data, will trigger regex fallback");
                    return createEmptyW2Data();
                }
            } else {
                System.out.println("WARNING: Azure W-2 extraction returned no documents");
                return createEmptyW2Data();
            }
            
            System.out.println("W-2 structured data extracted using Azure pre-built model");
            return w2Data;
            
        } catch (Exception e) {
            System.err.println("Error extracting W-2 data using Azure pre-built model: " + e.getMessage());
            e.printStackTrace();
            return createEmptyW2Data();
        }
    }
    
    @Override
    public GovernmentIdExtractedData extractGovernmentIdDataStructured(List<BufferedImage> images) {
        if (images == null || images.isEmpty()) {
            return createEmptyGovernmentIdData();
        }
        
        // Lazy initialization
        if (client == null) {
            initializeClient();
        }
        
        try {
            // Use first image (ID is typically single page)
            BufferedImage image = images.get(0);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Use Azure pre-built ID document model for structured extraction
            AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(BinaryData.fromBytes(imageBytes));
            AnalyzeResult analyzeResult = client.beginAnalyzeDocument("prebuilt-idDocument", options).getFinalResult();
            
            // Extract structured data from Azure result
            GovernmentIdExtractedData idData = new GovernmentIdExtractedData();
            
            if (analyzeResult.getDocuments() != null && !analyzeResult.getDocuments().isEmpty()) {
                var document = analyzeResult.getDocuments().get(0);
                Map<String, DocumentField> fields = document.getFields();
                
                // Extract name
                if (fields.containsKey("FirstName") || fields.containsKey("LastName")) {
                    String firstName = getFieldValueAsString(fields.get("FirstName"));
                    String lastName = getFieldValueAsString(fields.get("LastName"));
                    String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    idData.setFullName(fullName.trim());
                    idData.setFullNameConfidence(Math.max(
                        getFieldConfidence(fields.get("FirstName")),
                        getFieldConfidence(fields.get("LastName"))
                    ));
                } else if (fields.containsKey("DocumentNumber")) {
                    // Sometimes name is in a different field
                    String name = getFieldValueAsString(fields.get("DocumentNumber"));
                    if (name == null && fields.containsKey("Address")) {
                        // Try to extract from address or other fields
                    }
                }
                
                // Extract date of birth
                if (fields.containsKey("DateOfBirth")) {
                    String dob = getFieldValueAsString(fields.get("DateOfBirth"));
                    idData.setDateOfBirth(dob);
                    idData.setDateOfBirthConfidence(getFieldConfidence(fields.get("DateOfBirth")));
                }
                
                // Extract ID number
                if (fields.containsKey("DocumentNumber")) {
                    String idNumber = getFieldValueAsString(fields.get("DocumentNumber"));
                    idData.setIdNumber(idNumber);
                    idData.setIdNumberConfidence(getFieldConfidence(fields.get("DocumentNumber")));
                }
                
                // Extract issuing state
                if (fields.containsKey("AddressState")) {
                    String state = getFieldValueAsString(fields.get("AddressState"));
                    idData.setIssuingState(state);
                    idData.setIssuingStateConfidence(getFieldConfidence(fields.get("AddressState")));
                } else if (fields.containsKey("Region")) {
                    String state = getFieldValueAsString(fields.get("Region"));
                    idData.setIssuingState(state);
                    idData.setIssuingStateConfidence(getFieldConfidence(fields.get("Region")));
                }
                
                // Extract issue date
                if (fields.containsKey("DateOfIssue")) {
                    String issueDate = getFieldValueAsString(fields.get("DateOfIssue"));
                    idData.setIssueDate(issueDate);
                    idData.setIssueDateConfidence(getFieldConfidence(fields.get("DateOfIssue")));
                }
                
                // Extract expiration date
                if (fields.containsKey("DateOfExpiration")) {
                    String expDate = getFieldValueAsString(fields.get("DateOfExpiration"));
                    idData.setExpirationDate(expDate);
                    idData.setExpirationDateConfidence(getFieldConfidence(fields.get("DateOfExpiration")));
                }
                
                // Determine ID type
                String idType = "UNKNOWN";
                if (fields.containsKey("DocumentType")) {
                    String docType = getFieldValueAsString(fields.get("DocumentType"));
                    if (docType != null && docType.toLowerCase().contains("driver")) {
                        idType = "Driver License";
                    } else if (docType != null && docType.toLowerCase().contains("id")) {
                        idType = "State ID";
                    }
                }
                idData.setIdType(idType);
                idData.setIdTypeConfidence(idType.equals("UNKNOWN") ? 0.0 : 0.8);
                
                // Calculate overall confidence
                double overallConf = calculateOverallConfidenceGovernmentId(idData);
                idData.setOverallConfidence(overallConf);
            }
            
            System.out.println("Government ID structured data extracted using Azure pre-built model");
            return idData;
            
        } catch (Exception e) {
            System.err.println("Error extracting Government ID data using Azure pre-built model: " + e.getMessage());
            e.printStackTrace();
            return createEmptyGovernmentIdData();
        }
    }
    
    // Helper methods for extracting field values
    private String getFieldValueAsString(DocumentField field) {
        if (field == null) {
            return null;
        }
        try {
            // Use getContent() for string values - this is the correct method in Azure SDK
            if (field.getContent() != null) {
                return field.getContent();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private Double getFieldConfidence(DocumentField field) {
        if (field == null) {
            return 0.0;
        }
        try {
            return field.getConfidence() != null ? field.getConfidence() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private W2ExtractedData createEmptyW2Data() {
        W2ExtractedData data = new W2ExtractedData();
        data.setBoxValues(new HashMap<>());
        data.setOverallConfidence(0.0);
        return data;
    }
    
    private GovernmentIdExtractedData createEmptyGovernmentIdData() {
        GovernmentIdExtractedData data = new GovernmentIdExtractedData();
        data.setOverallConfidence(0.0);
        return data;
    }
    
    private Double calculateOverallConfidenceW2(W2ExtractedData data) {
        List<Double> confidences = new ArrayList<>();
        if (data.getEmployeeNameConfidence() != null) confidences.add(data.getEmployeeNameConfidence());
        if (data.getSsnConfidence() != null) confidences.add(data.getSsnConfidence());
        if (data.getAddressConfidence() != null) confidences.add(data.getAddressConfidence());
        if (data.getEmployerNameConfidence() != null) confidences.add(data.getEmployerNameConfidence());
        if (data.getEinConfidence() != null) confidences.add(data.getEinConfidence());
        if (data.getTaxYearConfidence() != null) confidences.add(data.getTaxYearConfidence());
        
        if (confidences.isEmpty()) return 0.0;
        return confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    private Double calculateOverallConfidenceGovernmentId(GovernmentIdExtractedData data) {
        List<Double> confidences = new ArrayList<>();
        if (data.getFullNameConfidence() != null) confidences.add(data.getFullNameConfidence());
        if (data.getDateOfBirthConfidence() != null) confidences.add(data.getDateOfBirthConfidence());
        if (data.getIdNumberConfidence() != null) confidences.add(data.getIdNumberConfidence());
        if (data.getIssuingStateConfidence() != null) confidences.add(data.getIssuingStateConfidence());
        if (data.getIssueDateConfidence() != null) confidences.add(data.getIssueDateConfidence());
        if (data.getExpirationDateConfidence() != null) confidences.add(data.getExpirationDateConfidence());
        
        if (confidences.isEmpty()) return 0.0;
        return confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
