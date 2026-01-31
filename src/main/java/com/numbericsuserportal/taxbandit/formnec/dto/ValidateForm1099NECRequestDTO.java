package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for validating Form 1099-NEC before creation.
 * Uses same structure as CreateForm1099NECRequestDTO.
 */
@Data
public class ValidateForm1099NECRequestDTO {
    
    // Reuse CreateForm1099NECRequestDTO structure
    @JsonProperty("SubmissionManifest")
    private CreateForm1099NECRequestDTO.SubmissionManifestDTO submissionManifest;
    
    @JsonProperty("ReturnHeader")
    private CreateForm1099NECRequestDTO.ReturnHeaderDTO returnHeader;
    
    @JsonProperty("ReturnData")
    private java.util.List<CreateForm1099NECRequestDTO.ReturnDataDTO> returnData;
}



