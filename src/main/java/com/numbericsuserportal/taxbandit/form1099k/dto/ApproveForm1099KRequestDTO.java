package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for approving Form 1099-K generated from transactions.
 */
@Data
public class ApproveForm1099KRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordIds")
    private List<UUID> recordIds; // Optional: specific records to approve
}

