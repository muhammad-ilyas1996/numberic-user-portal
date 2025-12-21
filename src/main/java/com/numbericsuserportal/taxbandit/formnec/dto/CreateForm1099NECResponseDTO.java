package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateForm1099NECResponseDTO {
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("StatusMessage")
    private String statusMessage;
    
    @JsonProperty("SubmissionId")
    private UUID submissionId;
    
    @JsonProperty("BusinessId")
    private UUID businessId;
    
    @JsonProperty("PayerRef")
    private String payerRef;
    
    @JsonProperty("Form1099Type")
    private String form1099Type;
    
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
        @JsonProperty("SequenceId")
        private String sequenceId;
        
        @JsonProperty("RecordId")
        private UUID recordId;
        
        @JsonProperty("RecipientId")
        private UUID recipientId;
        
        @JsonProperty("PayeeRef")
        private String payeeRef;
        
        @JsonProperty("AccountNum")
        private String accountNum;
        
        @JsonProperty("FederalReturn")
        private FederalReturnDTO federalReturn;
        
        @JsonProperty("StateReturns")
        private List<StateReturnResponseDTO> stateReturns;
        
        @JsonProperty("Postal")
        private PostalDTO postal;
        
        @JsonProperty("OnlineAccess")
        private OnlineAccessDTO onlineAccess;
        
        @JsonProperty("ScheduleFiling")
        private ScheduleFilingResponseDTO scheduleFiling;
    }
    
    @Data
    public static class FederalReturnDTO {
        @JsonProperty("Status")
        private String status;
        
        @JsonProperty("StatusTs")
        private String statusTs;
        
        @JsonProperty("Info")
        private String info;
        
        @JsonProperty("Errors")
        private List<ErrorDTO> errors;
    }
    
    @Data
    public static class StateReturnResponseDTO {
        @JsonProperty("StateCd")
        private String stateCd;
        
        @JsonProperty("Status")
        private String status;
        
        @JsonProperty("StatusTs")
        private String statusTs;
        
        @JsonProperty("Info")
        private String info;
        
        @JsonProperty("Errors")
        private List<ErrorDTO> errors;
    }
    
    @Data
    public static class PostalDTO {
        @JsonProperty("Status")
        private String status;
        
        @JsonProperty("StatusTs")
        private String statusTs;
        
        @JsonProperty("Info")
        private String info;
    }
    
    @Data
    public static class OnlineAccessDTO {
        @JsonProperty("Status")
        private String status;
        
        @JsonProperty("Email")
        private String email;
        
        @JsonProperty("Info")
        private String info;
    }
    
    @Data
    public static class ScheduleFilingResponseDTO {
        @JsonProperty("ScheduledOn")
        private String scheduledOn;
        
        @JsonProperty("Info")
        private String info;
    }
    
    @Data
    public static class ErrorRecordDTO {
        @JsonProperty("SequenceId")
        private String sequenceId;
        
        @JsonProperty("RecordId")
        private UUID recordId;
        
        @JsonProperty("Errors")
        private List<ErrorDTO> errors;
    }
    
    @Data
    public static class ErrorDTO {
        @JsonProperty("Id")
        private String id;
        
        @JsonProperty("Name")
        private String name;
        
        @JsonProperty("Message")
        private String message;
    }
}

