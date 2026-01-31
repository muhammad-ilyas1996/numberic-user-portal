package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Form 1099-NEC status response.
 */
@Data
public class Form1099NECStatusResponseDTO {
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("StatusMessage")
    private String statusMessage;
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("Form1099Records")
    private CreateForm1099NECResponseDTO.Form1099RecordsDTO form1099Records;
    
    @JsonProperty("Errors")
    private List<CreateForm1099NECResponseDTO.ErrorDTO> errors;
}



