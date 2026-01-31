package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

/**
 * DTO for Form 1099-K update response.
 */
@Data
public class UpdateForm1099KResponseDTO {
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("StatusMessage")
    private String statusMessage;
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordId")
    private UUID recordId;
    
    @JsonProperty("Errors")
    private java.util.List<CreateForm1099KResponseDTO.ErrorDTO> errors;
}

