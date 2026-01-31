package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for generating Form 1099-K from transactions.
 */
@Data
public class GenerateFromTxnsForm1099KRequestDTO {
    
    @JsonProperty("BusinessId")
    private UUID businessId;
    
    @JsonProperty("RecipientIds")
    private List<UUID> recipientIds;
    
    @JsonProperty("TaxYear")
    private String taxYear;
    
    @JsonProperty("IRSFilingType")
    private String irsFilingType; // FIRE or IRIS
    
    @JsonProperty("IsFederalFiling")
    private Boolean isFederalFiling;
    
    @JsonProperty("IsStateFiling")
    private Boolean isStateFiling;
    
    @JsonProperty("IsPostal")
    private Boolean isPostal;
    
    @JsonProperty("IsOnlineAccess")
    private Boolean isOnlineAccess;
}

