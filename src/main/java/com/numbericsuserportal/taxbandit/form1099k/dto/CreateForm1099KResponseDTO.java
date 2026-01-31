package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Form 1099-K creation response.
 */
@Data
public class CreateForm1099KResponseDTO {
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("StatusMessage")
    private String statusMessage;
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("Form1099Records")
    private Form1099RecordsDTO form1099Records;
    
    @JsonProperty("Errors")
    private List<ErrorDTO> errors;
    
    @Data
    public static class Form1099RecordsDTO {
        @JsonProperty("SuccessRecords")
        private List<SuccessRecordDTO> successRecords;
        
        @JsonProperty("ErrorRecords")
        private List<ErrorRecordDTO> errorRecords;
    }
    
    @Data
    public static class SuccessRecordDTO {
        @JsonProperty("RecordId")
        private UUID recordId;
        
        @JsonProperty("SequenceId")
        private String sequenceId;
    }
    
    @Data
    public static class ErrorRecordDTO {
        @JsonProperty("RecordId")
        private UUID recordId;
        
        @JsonProperty("Errors")
        private List<ErrorDTO> errors;
    }
    
    @Data
    public static class ErrorDTO {
        @JsonProperty("ErrorCode")
        private String errorCode;
        
        @JsonProperty("ErrorMessage")
        private String errorMessage;
        
        @JsonProperty("FieldName")
        private String fieldName;
    }
}

