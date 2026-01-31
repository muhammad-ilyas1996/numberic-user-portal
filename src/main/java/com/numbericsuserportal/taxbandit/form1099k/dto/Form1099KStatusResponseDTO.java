package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Form 1099-K status response.
 */
@Data
public class Form1099KStatusResponseDTO {
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("StatusMessage")
    private String statusMessage;
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("Form1099Records")
    private CreateForm1099KResponseDTO.Form1099RecordsDTO form1099Records;
    
    @JsonProperty("Errors")
    private List<CreateForm1099KResponseDTO.ErrorDTO> errors;
}

