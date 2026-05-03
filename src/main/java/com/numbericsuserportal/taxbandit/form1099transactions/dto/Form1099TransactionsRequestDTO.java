package com.numbericsuserportal.taxbandit.form1099transactions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Request body for TaxBandits POST Form1099Transactions (V1.7.3).
 * <p>
 * Official shape uses root {@code TxnData} (not SubmissionManifest/ReturnData). See:
 * <a href="https://developer.taxbandits.com/docs/Form1099Transactions/Post">Form1099Transactions Post</a>.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Form1099TransactionsRequestDTO {

    @JsonProperty("TxnData")
    private List<TxnDataBlockDTO> txnData;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TxnDataBlockDTO {
        @JsonProperty("Business")
        private BusinessRefDTO business;

        @JsonProperty("Recipients")
        private List<RecipientTxnsDTO> recipients;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BusinessRefDTO {
        @JsonProperty("PayerRef")
        private String payerRef;

        @JsonProperty("BusinessId")
        private UUID businessId;

        @JsonProperty("TINType")
        private String tinType;

        @JsonProperty("TIN")
        private String tin;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecipientTxnsDTO {
        @JsonProperty("PayeeRef")
        private String payeeRef;

        @JsonProperty("RecipientId")
        private UUID recipientId;

        @JsonProperty("TINType")
        private String tinType;

        @JsonProperty("TIN")
        private String tin;

        @JsonProperty("Txns")
        private List<TxnRowDTO> txns;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TxnRowDTO {
        @JsonProperty("SequenceId")
        private String sequenceId;

        @JsonProperty("TxnDate")
        private String txnDate;

        @JsonProperty("TxnAmt")
        private String txnAmt;

        @JsonProperty("PaymentType")
        private String paymentType;

        @JsonProperty("WHAmt")
        private String whAmt;
    }
}
