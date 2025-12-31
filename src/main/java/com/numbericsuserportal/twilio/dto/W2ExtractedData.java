package com.numbericsuserportal.twilio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for extracted W-2 form data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class W2ExtractedData {
    
    /**
     * Employee name
     */
    private String employeeName;
    private Double employeeNameConfidence;
    
    /**
     * Social Security Number
     */
    private String ssn;
    private Double ssnConfidence;
    
    /**
     * Employee address
     */
    private String address;
    private Double addressConfidence;
    
    /**
     * Employer name
     */
    private String employerName;
    private Double employerNameConfidence;
    
    /**
     * Employer address
     */
    private String employerAddress;
    private Double employerAddressConfidence;
    
    /**
     * Employer Identification Number
     */
    private String ein;
    private Double einConfidence;
    
    /**
     * Control number (if present)
     */
    private String controlNumber;
    private Double controlNumberConfidence;
    
    /**
     * Box values: 1, 2, 3, 4, 5, 6, 10, 12, 13, 14, 15, 16, 17, 18, 19
     * Key: box number (e.g., "1", "2")
     * Value: box value with confidence
     */
    private Map<String, BoxValue> boxValues;
    
    /**
     * Tax year
     */
    private String taxYear;
    private Double taxYearConfidence;
    
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

