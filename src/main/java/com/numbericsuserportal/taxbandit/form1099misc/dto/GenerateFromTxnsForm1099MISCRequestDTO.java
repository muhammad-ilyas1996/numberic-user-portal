package com.numbericsuserportal.taxbandit.form1099misc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Request for TaxBandits {@code Form1099MISC/GenerateFromTxns} (same envelope as NEC).
 */
@Data
public class GenerateFromTxnsForm1099MISCRequestDTO {

    @JsonProperty("TaxYear")
    private String taxYear;

    @JsonProperty("Business")
    private Business business;

    @JsonProperty("Recipients")
    private List<Recipient> recipients;

    @JsonProperty("IRSFilingType")
    private String irsFilingType;

    @JsonProperty("IsFederalFiling")
    private Boolean isFederalFiling;

    @JsonProperty("IsStateFiling")
    private Boolean isStateFiling;

    @JsonProperty("IsPostal")
    private Boolean isPostal;

    @JsonProperty("IsOnlineAccess")
    private Boolean isOnlineAccess;

    @Data
    public static class Business {
        @JsonProperty("PayerRef")
        private String payerRef;

        @JsonProperty("BusinessId")
        private UUID businessId;

        @JsonProperty("TINType")
        private String tinType;

        @JsonProperty("TIN")
        private String tin;
    }

    @Data
    public static class Recipient {
        @JsonProperty("PayeeRef")
        private String payeeRef;

        @JsonProperty("RecipientId")
        private UUID recipientId;
    }
}
