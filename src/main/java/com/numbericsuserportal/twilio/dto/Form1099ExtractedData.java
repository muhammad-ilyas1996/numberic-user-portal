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
    
    // ========== RECIPIENT INFORMATION ==========
    
    /**
     * Recipient name (full name or business name)
     */
    private String recipientName;
    private Double recipientNameConfidence;
    
    /**
     * Recipient's Taxpayer Identification Number (SSN or EIN)
     */
    private String recipientTin;
    private Double recipientTinConfidence;
    
    /**
     * Recipient street address
     */
    private String recipientStreetAddress;
    private Double recipientStreetAddressConfidence;
    
    /**
     * Recipient city, state, zip
     */
    private String recipientCityStateZip;
    private Double recipientCityStateZipConfidence;
    
    // ========== PAYER INFORMATION ==========
    
    /**
     * Payer name
     */
    private String payerName;
    private Double payerNameConfidence;
    
    /**
     * Payer street address
     */
    private String payerStreetAddress;
    private Double payerStreetAddressConfidence;
    
    /**
     * Payer city, state, zip
     */
    private String payerCityStateZip;
    private Double payerCityStateZipConfidence;
    
    /**
     * Payer EIN (Employer Identification Number)
     */
    private String payerTin;
    private Double payerTinConfidence;
    
    /**
     * Payer phone number (if listed)
     */
    private String payerPhone;
    private Double payerPhoneConfidence;
    
    // ========== TAX YEAR & METADATA ==========
    
    /**
     * Tax year
     */
    private String taxYear;
    private Double taxYearConfidence;
    
    /**
     * Account number (if present)
     */
    private String accountNumber;
    private Double accountNumberConfidence;
    
    /**
     * Corrected form indicator (true if "CORRECTED" is checked)
     */
    private Boolean correctedIndicator;
    private Double correctedIndicatorConfidence;
    
    /**
     * FATCA filing requirement (if checked)
     */
    private Boolean fatcaFilingRequirement;
    private Double fatcaFilingRequirementConfidence;
    
    // ========== BOX VALUES ==========
    
    /**
     * Box values according to form type
     * Key: box number (e.g., "1", "2", "7", "15", "16", "17")
     * Value: box value with confidence
     * 
     * 1099-NEC Boxes:
     * - Box 1: Nonemployee compensation
     * - Box 4: Federal income tax withheld
     * - Box 5: State tax withheld
     * - Box 6: State
     * - Box 7: State income
     * 
     * 1099-MISC Boxes:
     * - Box 1: Rents
     * - Box 2: Royalties
     * - Box 3: Other income
     * - Box 4: Federal income tax withheld
     * - Box 5: Fishing boat proceeds
     * - Box 6: Medical and healthcare payments
     * - Box 7: Direct sales ≥ $5,000
     * - Box 8: Substitute payments
     * - Box 10: Crop insurance proceeds
     * - Box 11: Fish purchased for resale
     * - Box 12: Section 409A deferrals
     * - Box 13: Excess golden parachute payments
     * - Box 14: Nonqualified deferred compensation
     * - Boxes 15-17: State tax info
     */
    private Map<String, BoxValue> boxValues;
    
    /**
     * Overall confidence score for the extraction
     */
    private Double overallConfidence;
    
    // ========== LEGACY FIELDS (for backward compatibility) ==========
    
    /**
     * @deprecated Use recipientTin instead
     */
    @Deprecated
    private String tin;
    private Double tinConfidence;
    
    /**
     * @deprecated Use recipientStreetAddress + recipientCityStateZip instead
     */
    @Deprecated
    private String address;
    private Double addressConfidence;
    
    /**
     * @deprecated Use payerTin instead
     */
    @Deprecated
    private String payerEin;
    private Double payerEinConfidence;
    
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
