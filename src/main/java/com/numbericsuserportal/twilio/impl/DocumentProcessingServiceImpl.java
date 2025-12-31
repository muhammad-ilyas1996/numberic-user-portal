package com.numbericsuserportal.twilio.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.twilio.dto.*;
import com.numbericsuserportal.twilio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for complete document processing pipeline
 * Orchestrates: File Download → PDF/Image Conversion → OCR → Classification → Data Extraction
 */
@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {
    
    @Autowired
    private FileHandlerService fileHandlerService;
    
    @Autowired
    private OCRService ocrService;
    
    @Autowired
    private DocumentClassifierService documentClassifierService;
    
    @Autowired
    private DataExtractionService dataExtractionService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int DEFAULT_DPI = 300;
    
    @Override
    public OcrResult processDocument(String documentUrl, String contentType) {
        OcrResult result = new OcrResult();
        result.setSuccess(false);
        result.setPageCount(0);
        
        try {
            System.out.println("Starting document processing for URL: " + documentUrl);
            System.out.println("Content type: " + contentType);
            
            // Step 1: Detect file type
            String fileType = fileHandlerService.detectFileType(contentType, documentUrl);
            System.out.println("Detected file type: " + fileType);
            
            if ("UNKNOWN".equals(fileType)) {
                result.setErrorMessage("Unsupported file type. Only PDF, JPG, and PNG are supported.");
                return result;
            }
            
            // Step 2: Download file
            InputStream fileInputStream = fileHandlerService.downloadFile(documentUrl);
            System.out.println("File downloaded successfully");
            
            // Step 3: Convert to images (PDF → images, or load image directly)
            List<BufferedImage> images;
            if ("PDF".equals(fileType)) {
                images = fileHandlerService.convertPdfToImages(fileInputStream, DEFAULT_DPI);
                System.out.println("PDF converted to " + images.size() + " images");
            } else {
                // JPG or PNG
                BufferedImage image = fileHandlerService.loadImage(fileInputStream);
                images = List.of(image);
                System.out.println("Image loaded successfully");
            }
            
            if (images == null || images.isEmpty()) {
                result.setErrorMessage("Failed to convert document to images");
                return result;
            }
            
            result.setPageCount(images.size());
            
            // Step 4: Perform OCR on all pages
            List<String> extractedTexts = ocrService.extractTextFromImages(images);
            String combinedText = extractedTexts.stream()
                    .filter(text -> text != null && !text.trim().isEmpty())
                    .collect(Collectors.joining("\n\n--- Page Break ---\n\n"));
            
            if (combinedText == null || combinedText.trim().isEmpty()) {
                result.setErrorMessage("OCR extraction returned no text");
                return result;
            }
            
            result.setRawText(combinedText);
            System.out.println("OCR extraction completed. Text length: " + combinedText.length());
            
            // Step 5: Classify document type
            String documentType = documentClassifierService.classifyDocument(combinedText);
            result.setDocumentType(documentType);
            System.out.println("Document classified as: " + documentType);
            
            // Step 6: Extract structured data based on document type
            // Use Azure pre-built models for W-2 and Government ID (much more accurate)
            // Use regex-based extraction for 1099 forms (no pre-built model available)
            Object extractedData = null;
            Double overallConfidence = 0.0;
            
            switch (documentType) {
                case DocumentClassifierServiceImpl.DOC_TYPE_W2:
                    // Use Azure pre-built W-2 model for direct structured extraction
                    try {
                        W2ExtractedData w2Data = ocrService.extractW2DataStructured(images);
                        
                        // Check if Azure extraction got meaningful data
                        // If key fields are missing, fallback to regex
                        boolean hasKeyFields = (w2Data.getEmployeeName() != null || w2Data.getSsn() != null || 
                                               w2Data.getEmployerName() != null || w2Data.getEin() != null);
                        
                        if (!hasKeyFields || (w2Data.getOverallConfidence() != null && w2Data.getOverallConfidence() < 0.3)) {
                            System.out.println("Azure W-2 extraction returned insufficient data, falling back to regex");
                            // Fallback to regex-based extraction
                            w2Data = dataExtractionService.extractW2Data(combinedText);
                            extractedData = w2Data;
                            overallConfidence = w2Data.getOverallConfidence() != null ? w2Data.getOverallConfidence() : 0.0;
                            System.out.println("W-2 data extracted using regex fallback. Confidence: " + overallConfidence);
                        } else {
                            extractedData = w2Data;
                            overallConfidence = w2Data.getOverallConfidence() != null ? w2Data.getOverallConfidence() : 0.0;
                            System.out.println("W-2 data extracted using Azure pre-built model. Confidence: " + overallConfidence);
                        }
                    } catch (Exception e) {
                        System.err.println("Error using Azure pre-built W-2 model, falling back to regex extraction: " + e.getMessage());
                        e.printStackTrace();
                        // Fallback to regex-based extraction
                        W2ExtractedData w2Data = dataExtractionService.extractW2Data(combinedText);
                        extractedData = w2Data;
                        overallConfidence = w2Data.getOverallConfidence() != null ? w2Data.getOverallConfidence() : 0.0;
                        System.out.println("W-2 data extracted using regex fallback. Confidence: " + overallConfidence);
                    }
                    break;
                    
                case DocumentClassifierServiceImpl.DOC_TYPE_1099_NEC:
                    // No pre-built model for 1099, use regex-based extraction
                    Form1099ExtractedData necData = dataExtractionService.extract1099Data(combinedText, "1099-NEC");
                    extractedData = necData;
                    overallConfidence = necData.getOverallConfidence() != null ? necData.getOverallConfidence() : 0.0;
                    System.out.println("1099-NEC data extracted using regex. Confidence: " + overallConfidence);
                    break;
                    
                case DocumentClassifierServiceImpl.DOC_TYPE_1099_MISC:
                    // No pre-built model for 1099, use regex-based extraction
                    Form1099ExtractedData miscData = dataExtractionService.extract1099Data(combinedText, "1099-MISC");
                    extractedData = miscData;
                    overallConfidence = miscData.getOverallConfidence() != null ? miscData.getOverallConfidence() : 0.0;
                    System.out.println("1099-MISC data extracted using regex. Confidence: " + overallConfidence);
                    break;
                    
                case DocumentClassifierServiceImpl.DOC_TYPE_GOVERNMENT_ID:
                    // Use Azure pre-built ID document model for direct structured extraction
                    try {
                        GovernmentIdExtractedData idData = ocrService.extractGovernmentIdDataStructured(images);
                        extractedData = idData;
                        overallConfidence = idData.getOverallConfidence() != null ? idData.getOverallConfidence() : 0.0;
                        System.out.println("Government ID data extracted using Azure pre-built model. Confidence: " + overallConfidence);
                    } catch (Exception e) {
                        System.err.println("Error using Azure pre-built ID model, falling back to regex extraction: " + e.getMessage());
                        // Fallback to regex-based extraction
                        GovernmentIdExtractedData idData = dataExtractionService.extractGovernmentIdData(combinedText);
                        extractedData = idData;
                        overallConfidence = idData.getOverallConfidence() != null ? idData.getOverallConfidence() : 0.0;
                        System.out.println("Government ID data extracted using regex fallback. Confidence: " + overallConfidence);
                    }
                    break;
                    
                default:
                    System.out.println("Unknown document type - no structured data extraction performed");
                    overallConfidence = 0.0;
                    break;
            }
            
            result.setExtractedData(extractedData);
            result.setOverallConfidence(overallConfidence);
            result.setSuccess(true);
            
            System.out.println("Document processing completed successfully");
            
        } catch (Exception e) {
            System.err.println("Error processing document: " + e.getMessage());
            e.printStackTrace();
            result.setErrorMessage("Error processing document: " + e.getMessage());
            result.setSuccess(false);
        }
        
        return result;
    }
    
    /**
     * Convert extracted data object to JSON string for storage
     * 
     * @param extractedData Extracted data object
     * @return JSON string representation
     */
    public String convertExtractedDataToJson(Object extractedData) {
        try {
            if (extractedData == null) {
                return null;
            }
            return objectMapper.writeValueAsString(extractedData);
        } catch (Exception e) {
            System.err.println("Error converting extracted data to JSON: " + e.getMessage());
            return null;
        }
    }
}

