package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for transmitting Form 1099-NEC to IRS.
 */
@Data
public class TransmitForm1099NECRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordIds")
    private List<UUID> recordIds; // Optional: specific records to transmit
}



