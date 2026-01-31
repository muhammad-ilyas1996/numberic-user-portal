package com.numbericsuserportal.taxbandit.form1099misc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for transmitting Form 1099-MISC to IRS.
 */
@Data
public class TransmitForm1099MISCRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordIds")
    private List<UUID> recordIds; // Optional: specific records to transmit
}

