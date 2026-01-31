package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

/**
 * DTO for requesting draft PDF URL for Form 1099-NEC.
 * TaxBandits expects RecordId (singular) - one record per request.
 */
@Data
public class RequestDraftPdfUrlForm1099NECRequestDTO {
    
    @JsonProperty("RecordId")
    private UUID recordId;
}

