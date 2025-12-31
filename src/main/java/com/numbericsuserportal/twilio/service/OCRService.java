package com.numbericsuserportal.twilio.service;

import com.numbericsuserportal.twilio.dto.GovernmentIdExtractedData;
import com.numbericsuserportal.twilio.dto.W2ExtractedData;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Service interface for OCR operations using Azure Document Intelligence
 */
public interface OCRService {
    
    /**
     * Extract text from a single image using OCR
     * 
     * @param image BufferedImage to process
     * @return Extracted text
     */
    String extractText(BufferedImage image);
    
    /**
     * Extract text from multiple images (e.g., PDF pages)
     * 
     * @param images List of BufferedImage objects
     * @return List of extracted text (one per image)
     */
    List<String> extractTextFromImages(List<BufferedImage> images);
    
    /**
     * Extract structured W-2 data directly using Azure pre-built model
     * This is much more accurate than regex-based extraction
     * 
     * @param images List of BufferedImage objects (usually single page for W-2)
     * @return W2ExtractedData with structured fields
     */
    W2ExtractedData extractW2DataStructured(List<BufferedImage> images);
    
    /**
     * Extract structured Government ID data directly using Azure pre-built model
     * This is much more accurate than regex-based extraction
     * 
     * @param images List of BufferedImage objects (usually single page for ID)
     * @return GovernmentIdExtractedData with structured fields
     */
    GovernmentIdExtractedData extractGovernmentIdDataStructured(List<BufferedImage> images);
    
    /**
     * Get confidence score for OCR extraction
     * 
     * @param image BufferedImage to process
     * @return Average confidence score (0.0 to 1.0)
     */
    Double getConfidenceScore(BufferedImage image);
}

