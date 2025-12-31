package com.numbericsuserportal.twilio.impl;

import com.numbericsuserportal.twilio.service.DocumentClassifierService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service implementation for document classification using keyword-based detection
 */
@Service
public class DocumentClassifierServiceImpl implements DocumentClassifierService {
    
    // Document type constants
    public static final String DOC_TYPE_W2 = "W-2";
    public static final String DOC_TYPE_1099_NEC = "1099-NEC";
    public static final String DOC_TYPE_1099_MISC = "1099-MISC";
    public static final String DOC_TYPE_GOVERNMENT_ID = "GOVERNMENT_ID";
    public static final String DOC_TYPE_UNKNOWN = "UNKNOWN";
    
    // Patterns for document classification
    private static final Pattern W2_PATTERN = Pattern.compile(
        "\\bW-?2\\b|\\bWAGE\\s+AND\\s+TAX\\s+STATEMENT\\b|\\bFORM\\s+W-?2\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern FORM1099_NEC_PATTERN = Pattern.compile(
        "\\b1099-?NEC\\b|\\bNONEMPLOYEE\\s+COMPENSATION\\b|\\bFORM\\s+1099-?NEC\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern FORM1099_MISC_PATTERN = Pattern.compile(
        "\\b1099-?MISC\\b|\\bMISCELLANEOUS\\s+INCOME\\b|\\bFORM\\s+1099-?MISC\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern GOVERNMENT_ID_PATTERN = Pattern.compile(
        "\\bDRIVER\\s+LICENSE\\b|\\bDRIVER\\'?S\\s+LICENSE\\b|\\bDRIVING\\s+LICENSE\\b|" +
        "\\bSTATE\\s+ID\\b|\\bSTATE\\s+IDENTIFICATION\\b|\\bID\\s+CARD\\b|" +
        "\\bDEPARTMENT\\s+OF\\s+MOTOR\\s+VEHICLES\\b|\\bDMV\\b|" +
        "\\bLICENSE\\s+NUMBER\\b|\\bID\\s+NUMBER\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public String classifyDocument(String extractedText) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            return DOC_TYPE_UNKNOWN;
        }
        
        String normalizedText = extractedText.toUpperCase();
        
        // Priority order: Check for most specific patterns first
        
        // Check for W-2 (high priority)
        if (W2_PATTERN.matcher(extractedText).find()) {
            System.out.println("Document classified as: W-2");
            return DOC_TYPE_W2;
        }
        
        // Check for 1099-NEC
        if (FORM1099_NEC_PATTERN.matcher(extractedText).find()) {
            System.out.println("Document classified as: 1099-NEC");
            return DOC_TYPE_1099_NEC;
        }
        
        // Check for 1099-MISC
        if (FORM1099_MISC_PATTERN.matcher(extractedText).find()) {
            System.out.println("Document classified as: 1099-MISC");
            return DOC_TYPE_1099_MISC;
        }
        
        // Check for Government ID
        if (GOVERNMENT_ID_PATTERN.matcher(extractedText).find()) {
            System.out.println("Document classified as: GOVERNMENT_ID");
            return DOC_TYPE_GOVERNMENT_ID;
        }
        
        System.out.println("Document classified as: UNKNOWN");
        return DOC_TYPE_UNKNOWN;
    }
    
    @Override
    public Double getClassificationConfidence(String extractedText, String documentType) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            return 0.0;
        }
        
        if (DOC_TYPE_UNKNOWN.equals(documentType)) {
            return 0.0;
        }
        
        // Count matches for the document type pattern
        int matchCount = 0;
        Pattern pattern = null;
        
        switch (documentType) {
            case DOC_TYPE_W2:
                pattern = W2_PATTERN;
                break;
            case DOC_TYPE_1099_NEC:
                pattern = FORM1099_NEC_PATTERN;
                break;
            case DOC_TYPE_1099_MISC:
                pattern = FORM1099_MISC_PATTERN;
                break;
            case DOC_TYPE_GOVERNMENT_ID:
                pattern = GOVERNMENT_ID_PATTERN;
                break;
            default:
                return 0.0;
        }
        
        if (pattern != null) {
            java.util.regex.Matcher matcher = pattern.matcher(extractedText);
            while (matcher.find()) {
                matchCount++;
            }
        }
        
        // Confidence based on number of matches and text length
        // More matches = higher confidence
        // Longer text with matches = higher confidence
        double baseConfidence = Math.min(1.0, matchCount * 0.3);
        double lengthBonus = extractedText.length() > 100 ? 0.2 : 0.0;
        
        return Math.min(1.0, baseConfidence + lengthBonus);
    }
}

