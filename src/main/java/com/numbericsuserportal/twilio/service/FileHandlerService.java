package com.numbericsuserportal.twilio.service;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * Service interface for file handling operations
 * Handles PDF to image conversion and image preprocessing
 */
public interface FileHandlerService {
    
    /**
     * Download file from URL
     * 
     * @param fileUrl URL of the file to download
     * @return InputStream of the file content
     */
    InputStream downloadFile(String fileUrl);
    
    /**
     * Detect file type from content type or file extension
     * 
     * @param contentType Content type (e.g., "application/pdf", "image/jpeg")
     * @param fileUrl File URL (for extension detection)
     * @return File type: PDF, JPG, PNG, or UNKNOWN
     */
    String detectFileType(String contentType, String fileUrl);
    
    /**
     * Convert PDF to images (one image per page)
     * 
     * @param pdfInputStream PDF file input stream
     * @param dpi DPI for image conversion (default: 300)
     * @return List of BufferedImage objects (one per page)
     */
    List<BufferedImage> convertPdfToImages(InputStream pdfInputStream, int dpi);
    
    /**
     * Load image from input stream
     * 
     * @param imageInputStream Image file input stream
     * @return BufferedImage object
     */
    BufferedImage loadImage(InputStream imageInputStream);
    
    /**
     * Preprocess image for better OCR results
     * - Convert to grayscale
     * - Resize if needed
     * - Apply noise reduction
     * 
     * @param image Original image
     * @return Preprocessed image
     */
    BufferedImage preprocessImage(BufferedImage image);
}

