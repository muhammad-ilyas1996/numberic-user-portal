package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for getting Form 1099-NEC by record IDs (without SubmissionId).
 * TaxBandits expects RecordIds as array of objects: [{"RecordId": "uuid"}]
 */
@Data
public class GetbyRecordIdsForm1099NECRequestDTO {
    
    @JsonProperty("RecordIds")
    private List<RecordIdItem> recordIds;
    
    @Data
    public static class RecordIdItem {
        @JsonProperty("RecordId")
        private UUID recordId;
    }
}

