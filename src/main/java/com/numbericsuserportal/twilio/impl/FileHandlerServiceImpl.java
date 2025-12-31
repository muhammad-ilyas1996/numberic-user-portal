package com.numbericsuserportal.twilio.impl;

import com.numbericsuserportal.twilio.config.TwilioConfig;
import com.numbericsuserportal.twilio.service.FileHandlerService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for file handling operations
 */
@Service
public class FileHandlerServiceImpl implements FileHandlerService {
    
    @Autowired
    private TwilioConfig twilioConfig;
    
    private static final int DEFAULT_DPI = 300;
    private static final int MAX_IMAGE_WIDTH = 2000;
    private static final int MAX_IMAGE_HEIGHT = 2000;
    
    @Override
    public InputStream downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(30000); // 30 seconds
            
            // Add Basic Authentication for Twilio media URLs
            if (fileUrl != null && fileUrl.contains("api.twilio.com")) {
                String accountSid = twilioConfig.getAccountSid();
                String authToken = twilioConfig.getAuthToken();
                
                if (accountSid != null && authToken != null && !accountSid.isEmpty() && !authToken.isEmpty()) {
                    // Create Basic Auth header: Base64(AccountSid:AuthToken)
                    String credentials = accountSid + ":" + authToken;
                    String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                    connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
                    System.out.println("Added Twilio authentication for media download");
                }
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return connection.getInputStream();
            } else {
                // Read error response for debugging
                String errorMessage = "Failed to download file. HTTP response code: " + responseCode;
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getErrorStream()))) {
                    String line;
                    StringBuilder errorResponse = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    if (errorResponse.length() > 0) {
                        errorMessage += " - " + errorResponse.toString();
                    }
                } catch (Exception ignored) {
                    // Ignore if error stream is not available
                }
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from URL: " + fileUrl, e);
        }
    }
    
    @Override
    public String detectFileType(String contentType, String fileUrl) {
        if (contentType != null) {
            contentType = contentType.toLowerCase();
            if (contentType.contains("pdf")) {
                return "PDF";
            } else if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                return "JPG";
            } else if (contentType.contains("png")) {
                return "PNG";
            }
        }
        
        // Fallback to file extension
        if (fileUrl != null) {
            String lowerUrl = fileUrl.toLowerCase();
            if (lowerUrl.endsWith(".pdf")) {
                return "PDF";
            } else if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
                return "JPG";
            } else if (lowerUrl.endsWith(".png")) {
                return "PNG";
            }
        }
        
        return "UNKNOWN";
    }
    
    @Override
    public List<BufferedImage> convertPdfToImages(InputStream pdfInputStream, int dpi) {
        List<BufferedImage> images = new ArrayList<>();
        
        try {
            // PDFBox 3.x uses Loader.loadPDF() instead of PDDocument.load()
            // Read all bytes from InputStream
            byte[] pdfBytes = pdfInputStream.readAllBytes();
            PDDocument document = org.apache.pdfbox.Loader.loadPDF(pdfBytes);
            
            try {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();
                
                for (int page = 0; page < pageCount; page++) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi);
                    // Preprocess each page image
                    image = preprocessImage(image);
                    images.add(image);
                }
                
                System.out.println("Converted PDF to " + images.size() + " images at " + dpi + " DPI");
            } finally {
                document.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error converting PDF to images", e);
        }
        
        return images;
    }
    
    @Override
    public BufferedImage loadImage(InputStream imageInputStream) {
        try {
            BufferedImage image = ImageIO.read(imageInputStream);
            if (image == null) {
                throw new RuntimeException("Failed to load image from input stream");
            }
            return preprocessImage(image);
        } catch (Exception e) {
            throw new RuntimeException("Error loading image", e);
        }
    }
    
    @Override
    public BufferedImage preprocessImage(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        // Step 1: Convert to grayscale if needed
        BufferedImage grayscaleImage = convertToGrayscale(image);
        
        // Step 2: Resize if too large (to improve OCR speed and reduce memory)
        BufferedImage resizedImage = resizeIfNeeded(grayscaleImage);
        
        // Step 3: Apply basic noise reduction (simple contrast enhancement)
        BufferedImage enhancedImage = enhanceContrast(resizedImage);
        
        return enhancedImage;
    }
    
    /**
     * Convert image to grayscale
     */
    private BufferedImage convertToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayscale.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayscale;
    }
    
    /**
     * Resize image if it exceeds maximum dimensions
     */
    private BufferedImage resizeIfNeeded(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (width <= MAX_IMAGE_WIDTH && height <= MAX_IMAGE_HEIGHT) {
            return image;
        }
        
        // Calculate scaling factor to fit within max dimensions
        double scaleX = (double) MAX_IMAGE_WIDTH / width;
        double scaleY = (double) MAX_IMAGE_HEIGHT / height;
        double scale = Math.min(scaleX, scaleY);
        
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        
        BufferedImage resized = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        System.out.println("Resized image from " + width + "x" + height + " to " + newWidth + "x" + newHeight);
        return resized;
    }
    
    /**
     * Enhance contrast for better OCR results
     */
    private BufferedImage enhanceContrast(BufferedImage image) {
        // Simple contrast enhancement using lookup table
        BufferedImage enhanced = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // Extract gray value
                
                // Apply contrast enhancement (simple linear transformation)
                int enhancedGray = Math.min(255, Math.max(0, (int) (gray * 1.2)));
                int newRgb = (enhancedGray << 16) | (enhancedGray << 8) | enhancedGray;
                enhanced.setRGB(x, y, newRgb);
            }
        }
        
        return enhanced;
    }
}

