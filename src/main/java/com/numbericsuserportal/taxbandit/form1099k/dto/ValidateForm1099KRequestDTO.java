package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for validating Form 1099-K before creation.
 * Uses same structure as CreateForm1099KRequestDTO.
 */
@Data
public class ValidateForm1099KRequestDTO {
    
    // Reuse CreateForm1099KRequestDTO structure
    @JsonProperty("SubmissionManifest")
    private CreateForm1099KRequestDTO.SubmissionManifestDTO submissionManifest;
    
    @JsonProperty("ReturnHeader")
    private CreateForm1099KRequestDTO.ReturnHeaderDTO returnHeader;
    
    @JsonProperty("ReturnData")
    private java.util.List<CreateForm1099KRequestDTO.ReturnDataDTO> returnData;
}

