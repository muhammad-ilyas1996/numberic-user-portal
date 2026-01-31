package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for requesting PDF URLs for Form 1099-K.
 * TaxBandits expects RecordIds as array of objects: [{"RecordId": "uuid"}]
 */
@Data
public class RequestPdfURLsForm1099KRequestDTO {
    
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

