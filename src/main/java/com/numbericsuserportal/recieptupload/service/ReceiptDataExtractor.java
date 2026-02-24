package com.numbericsuserportal.recieptupload.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to extract structured data from OCR text
 */
@Component
@Slf4j
public class ReceiptDataExtractor {
    
    // Patterns for extracting receipt data
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
        "(?i)(?:^|\\n)\\s*([A-Z][A-Z\\s&.,'-]+(?:INC|LLC|CORP|LTD|STORE|MARKET|SHOP)?)",
        Pattern.MULTILINE
    );
    
    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(?i)(?:date|dated|dt\\.?)\\s*:?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})",
        Pattern.MULTILINE
    );
    
    private static final Pattern TOTAL_PATTERN = Pattern.compile(
        "(?i)(?:total|amount|amt|sum)\\s*:?\\s*\\$?\\s*(\\d+\\.?\\d*)",
        Pattern.MULTILINE
    );
    
    private static final Pattern TAX_PATTERN = Pattern.compile(
        "(?i)(?:tax|vat|gst|sales\\s*tax)\\s*:?\\s*\\$?\\s*(\\d+\\.?\\d*)",
        Pattern.MULTILINE
    );
    
    private static final Pattern ITEM_PATTERN = Pattern.compile(
        "(?i)([A-Z][A-Za-z0-9\\s&.,'-]+?)\\s+\\$?\\s*(\\d+\\.?\\d{0,2})",
        Pattern.MULTILINE
    );
    
    /**
     * Extract merchant name from OCR text
     */
    public String extractMerchantName(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return null;
        }
        
        // Try to find merchant name in first few lines
        String[] lines = ocrText.split("\\n");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            if (line.length() > 3 && line.length() < 50) {
                // Check if line looks like a merchant name
                if (line.matches("^[A-Z][A-Za-z0-9\\s&.,'-]+$") && 
                    !line.matches(".*(?:DATE|TOTAL|TAX|AMOUNT|RECEIPT|THANK).*")) {
                    return line;
                }
            }
        }
        
        // Try pattern matching
        Matcher matcher = MERCHANT_PATTERN.matcher(ocrText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
    /**
     * Extract date from OCR text
     */
    public LocalDate extractDate(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return null;
        }
        
        Matcher matcher = DATE_PATTERN.matcher(ocrText);
        if (matcher.find()) {
            String dateStr = matcher.group(1);
            return parseDate(dateStr);
        }
        
        // Try to find date in common formats
        String[] dateFormats = {
            "MM/dd/yyyy", "MM-dd-yyyy", "dd/MM/yyyy", "dd-MM-yyyy",
            "MM/dd/yy", "MM-dd-yy", "dd/MM/yy", "dd-MM-yy",
            "yyyy-MM-dd", "yyyy/MM/dd"
        };
        
        Pattern dateOnlyPattern = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b");
        Matcher dateMatcher = dateOnlyPattern.matcher(ocrText);
        if (dateMatcher.find()) {
            String dateStr = dateMatcher.group(1);
            return parseDate(dateStr);
        }
        
        return null;
    }
    
    /**
     * Parse date string with multiple formats
     */
    private LocalDate parseDate(String dateStr) {
        String[] formats = {
            "MM/dd/yyyy", "MM-dd-yyyy", "dd/MM/yyyy", "dd-MM-yyyy",
            "MM/dd/yy", "MM-dd-yy", "dd/MM/yy", "dd-MM-yy",
            "yyyy-MM-dd", "yyyy/MM/dd"
        };
        
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate date = LocalDate.parse(dateStr, formatter);
                
                // Handle 2-digit years
                if (format.contains("yy") && !format.contains("yyyy")) {
                    int year = date.getYear();
                    if (year < 50) {
                        date = date.plusYears(2000);
                    } else if (year < 100) {
                        date = date.plusYears(1900);
                    }
                }
                
                return date;
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        return null;
    }
    
    /**
     * Extract total amount from OCR text
     */
    public BigDecimal extractTotalAmount(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return null;
        }
        
        // Look for total in last few lines (usually at bottom)
        String[] lines = ocrText.split("\\n");
        for (int i = lines.length - 1; i >= Math.max(0, lines.length - 5); i--) {
            String line = lines[i].trim().toUpperCase();
            if (line.contains("TOTAL") || line.contains("AMOUNT")) {
                Matcher matcher = Pattern.compile("\\$?\\s*(\\d+\\.?\\d{0,2})").matcher(line);
                if (matcher.find()) {
                    try {
                        return new BigDecimal(matcher.group(1));
                    } catch (NumberFormatException e) {
                        // Continue searching
                    }
                }
            }
        }
        
        // Try pattern matching
        Matcher matcher = TOTAL_PATTERN.matcher(ocrText);
        BigDecimal maxAmount = null;
        while (matcher.find()) {
            try {
                BigDecimal amount = new BigDecimal(matcher.group(1));
                if (maxAmount == null || amount.compareTo(maxAmount) > 0) {
                    maxAmount = amount;
                }
            } catch (NumberFormatException e) {
                // Continue
            }
        }
        
        return maxAmount;
    }
    
    /**
     * Extract tax amount from OCR text
     */
    public BigDecimal extractTaxAmount(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return null;
        }
        
        Matcher matcher = TAX_PATTERN.matcher(ocrText);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
}
