package com.numbericsuserportal.taxbandit.form1099misc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for validating Form 1099-MISC before creation.
 * Uses same structure as CreateForm1099MISCRequestDTO.
 */
@Data
public class ValidateForm1099MISCRequestDTO {
    
    // Reuse CreateForm1099MISCRequestDTO structure
    @JsonProperty("SubmissionManifest")
    private CreateForm1099MISCRequestDTO.SubmissionManifestDTO submissionManifest;
    
    @JsonProperty("ReturnHeader")
    private CreateForm1099MISCRequestDTO.ReturnHeaderDTO returnHeader;
    
    @JsonProperty("ReturnData")
    private java.util.List<CreateForm1099MISCRequestDTO.ReturnDataDTO> returnData;
}

