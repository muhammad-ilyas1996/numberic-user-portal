package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

/**
 * DTO for requesting distribution URL for Form 1099-NEC.
 * TaxBandits structure: TaxYear, RecordId, Business, Recipient, Customization.
 */
@Data
public class RequestDistUrlForm1099NECRequestDTO {
    
    @JsonProperty("TaxYear")
    private String taxYear;
    
    @JsonProperty("RecordId")
    private UUID recordId;
    
    @JsonProperty("Business")
    private BusinessRefDTO business;
    
    @JsonProperty("Recipient")
    private RecipientRefDTO recipient;
    
    @JsonProperty("Customization")
    private CustomizationDTO customization;
    
    @Data
    public static class BusinessRefDTO {
        @JsonProperty("BusinessId")
        private UUID businessId;
        @JsonProperty("PayerRef")
        private String payerRef;
        @JsonProperty("TINType")
        private String tinType;
        @JsonProperty("TIN")
        private String tin;
    }
    
    @Data
    public static class RecipientRefDTO {
        @JsonProperty("PayeeRef")
        private String payeeRef;
        @JsonProperty("RecipientId")
        private UUID recipientId;
        @JsonProperty("TINType")
        private String tinType;
        @JsonProperty("TIN")
        private String tin;
    }
    
    @Data
    public static class CustomizationDTO {
        @JsonProperty("BusinessLogoUrl")
        private String businessLogoUrl;
        @JsonProperty("ReturnUrl")
        private String returnUrl;
        @JsonProperty("CancelUrl")
        private String cancelUrl;
        @JsonProperty("ExpiryTime")
        private String expiryTime;
    }
}

