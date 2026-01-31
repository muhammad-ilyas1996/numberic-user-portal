package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

/**
 * DTO for updating Form 1099-K.
 */
@Data
public class UpdateForm1099KRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordId")
    private UUID recordId;
    
    // Reuse the same structure as CreateForm1099KRequestDTO for the update data
    @JsonProperty("ReturnHeader")
    private CreateForm1099KRequestDTO.ReturnHeaderDTO returnHeader;
    
    @JsonProperty("ReturnData")
    private java.util.List<CreateForm1099KRequestDTO.ReturnDataDTO> returnData;
}

