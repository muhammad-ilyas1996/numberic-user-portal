package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

/**
 * DTO for uploading attachment to Form 1099-NEC.
 */
@Data
public class UploadAttachmentForm1099NECRequestDTO {
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("RecordId")
    private UUID recordId;
    
    @JsonProperty("FileName")
    private String fileName;
    
    @JsonProperty("FileContent")
    private String fileContent; // Base64 encoded file content
    
    @JsonProperty("FileType")
    private String fileType; // e.g., "PDF", "JPG", "PNG"
}

