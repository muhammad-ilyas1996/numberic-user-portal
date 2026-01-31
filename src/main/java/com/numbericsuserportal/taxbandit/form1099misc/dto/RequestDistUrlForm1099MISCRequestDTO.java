package com.numbericsuserportal.taxbandit.form1099misc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for requesting distribution URL for Form 1099-MISC.
 */
@Data
public class RequestDistUrlForm1099MISCRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordIds")
    private List<UUID> recordIds; // Optional: specific records
}

