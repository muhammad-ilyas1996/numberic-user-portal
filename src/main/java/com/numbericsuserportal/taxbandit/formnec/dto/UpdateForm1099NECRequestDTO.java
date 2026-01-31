package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

/**
 * DTO for updating Form 1099-NEC.
 * TaxBandits requires SubmissionManifest, ReturnHeader, ReturnData.
 */
@Data
public class UpdateForm1099NECRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordId")
    private UUID recordId;
    
    @JsonProperty("SubmissionManifest")
    private CreateForm1099NECRequestDTO.SubmissionManifestDTO submissionManifest;
    
    @JsonProperty("ReturnHeader")
    private CreateForm1099NECRequestDTO.ReturnHeaderDTO returnHeader;
    
    @JsonProperty("ReturnData")
    private java.util.List<CreateForm1099NECRequestDTO.ReturnDataDTO> returnData;
}



