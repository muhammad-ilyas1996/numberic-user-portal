package com.numbericsuserportal.taxbandit.form1099transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Success response for TaxBandits POST Form1099Transactions.
 */
@Data
public class Form1099TransactionsResponseDTO {

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

    @JsonProperty("Form1099TransactionsRecords")
    private Form1099TransactionsRecordsDTO form1099TransactionsRecords;

    @JsonProperty("Errors")
    private List<Object> errors;

    @Data
    public static class Form1099TransactionsRecordsDTO {
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

        @JsonProperty("TransactionId")
        private UUID transactionId;

        @JsonProperty("RecipientId")
        private UUID recipientId;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("StatusTs")
        private String statusTs;

        @JsonProperty("Info")
        private Object info;

        @JsonProperty("Errors")
        private Object errors;
    }

    @Data
    public static class ErrorRecordDTO {
        @JsonProperty("RecordId")
        private UUID recordId;

        @JsonProperty("Errors")
        private List<Object> errors;
    }
}
