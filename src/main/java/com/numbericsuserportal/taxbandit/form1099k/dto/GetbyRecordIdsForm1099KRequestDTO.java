package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for getting Form 1099-K by record IDs (without SubmissionId).
 * TaxBandits expects RecordIds as array of objects: [{"RecordId": "uuid"}]
 */
@Data
public class GetbyRecordIdsForm1099KRequestDTO {
    
    @JsonProperty("RecordIds")
    private List<RecordIdItem> recordIds;
    
    @Data
    public static class RecordIdItem {
        @JsonProperty("RecordId")
        private UUID recordId;
    }
}

