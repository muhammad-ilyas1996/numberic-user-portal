package com.numbericsuserportal.twilio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for extracted Government ID data (Driver License / State ID)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GovernmentIdExtractedData {
    
    /**
     * Full name
     */
    private String fullName;
    private Double fullNameConfidence;
    
    /**
     * Date of birth
     */
    private String dateOfBirth;
    private Double dateOfBirthConfidence;
    
    /**
     * ID number
     */
    private String idNumber;
    private Double idNumberConfidence;
    
    /**
     * Issuing state
     */
    private String issuingState;
    private Double issuingStateConfidence;
    
    /**
     * Issue date
     */
    private String issueDate;
    private Double issueDateConfidence;
    
    /**
     * Expiration date
     */
    private String expirationDate;
    private Double expirationDateConfidence;
    
    /**
     * ID type: Driver License or State ID
     */
    private String idType;
    private Double idTypeConfidence;
    
    /**
     * Overall confidence score for the extraction
     */
    private Double overallConfidence;
}

