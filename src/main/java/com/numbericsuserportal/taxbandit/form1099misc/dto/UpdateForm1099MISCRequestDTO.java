package com.numbericsuserportal.taxbandit.form1099misc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

/**
 * DTO for updating Form 1099-MISC.
 */
@Data
public class UpdateForm1099MISCRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordId")
    private UUID recordId;
    
    // Reuse the same structure as CreateForm1099MISCRequestDTO for the update data
    @JsonProperty("ReturnHeader")
    private CreateForm1099MISCRequestDTO.ReturnHeaderDTO returnHeader;
    
    @JsonProperty("ReturnData")
    private java.util.List<CreateForm1099MISCRequestDTO.ReturnDataDTO> returnData;
}

