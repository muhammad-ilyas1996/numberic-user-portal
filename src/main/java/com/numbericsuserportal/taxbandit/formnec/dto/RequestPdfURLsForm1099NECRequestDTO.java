package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for requesting PDF URLs for Form 1099-NEC.
 * TaxBandits expects RecordIds as array of objects: [{"RecordId": "uuid"}]
 */
@Data
public class RequestPdfURLsForm1099NECRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordIds")
    private List<RecordIdItem> recordIds;
    
    @Data
    public static class RecordIdItem {
        @JsonProperty("RecordId")
        private UUID recordId;
    }
}

