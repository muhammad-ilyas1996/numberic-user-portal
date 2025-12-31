package com.numbericsuserportal.twilio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for extracted 1099-NEC / 1099-MISC form data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Form1099ExtractedData {
    
    /**
     * Form type: 1099-NEC or 1099-MISC
     */
    private String formType;
    private Double formTypeConfidence;
    
    /**
     * Recipient name
     */
    private String recipientName;
    private Double recipientNameConfidence;
    
    /**
     * Taxpayer Identification Number (TIN)
     */
    private String tin;
    private Double tinConfidence;
    
    /**
     * Recipient address
     */
    private String address;
    private Double addressConfidence;
    
    /**
     * Payer name
     */
    private String payerName;
    private Double payerNameConfidence;
    
    /**
     * Payer EIN
     */
    private String payerEin;
    private Double payerEinConfidence;
    
    /**
     * Tax year
     */
    private String taxYear;
    private Double taxYearConfidence;
    
    /**
     * Box values according to form type
     * Key: box number (e.g., "1", "2", "7")
     * Value: box value with confidence
     */
    private Map<String, BoxValue> boxValues;
    
    /**
     * Overall confidence score for the extraction
     */
    private Double overallConfidence;
    
    /**
     * Inner class for box value with confidence
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoxValue {
        private String value;
        private Double confidence;
    }
}

