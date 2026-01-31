package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

/**
 * DTO for requesting draft PDF URL for Form 1099-K.
 * TaxBandits expects RecordId (singular) - one record per request.
 */
@Data
public class RequestDraftPdfUrlForm1099KRequestDTO {
    
    @JsonProperty("RecordId")
    private UUID recordId;
}

