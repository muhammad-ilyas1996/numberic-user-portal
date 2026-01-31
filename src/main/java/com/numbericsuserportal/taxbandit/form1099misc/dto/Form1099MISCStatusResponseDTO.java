package com.numbericsuserportal.taxbandit.form1099misc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Form 1099-MISC status response.
 */
@Data
public class Form1099MISCStatusResponseDTO {
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("StatusMessage")
    private String statusMessage;
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("Form1099Records")
    private CreateForm1099MISCResponseDTO.Form1099RecordsDTO form1099Records;
    
    @JsonProperty("Errors")
    private List<CreateForm1099MISCResponseDTO.ErrorDTO> errors;
}

