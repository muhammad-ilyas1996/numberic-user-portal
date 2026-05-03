package com.numbericsuserportal.taxbandit.form1099transactions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Request body for TaxBandits POST Form1099Transactions (V1.7.3).
 */
@Data
public class Form1099TransactionsRequestDTO {

    @JsonProperty("SubmissionManifest")
    private SubmissionManifestDTO submissionManifest;

    @JsonProperty("ReturnHeader")
    private ReturnHeaderDTO returnHeader;

    @JsonProperty("ReturnData")
    private List<ReturnDataDTO> returnData;

    @Data
    public static class SubmissionManifestDTO {
        @JsonProperty("SubmissionId")
        private UUID submissionId;

        @JsonProperty("TaxYear")
        private String taxYear;

        @JsonProperty("IRSFilingType")
        private String irsFilingType;

        @JsonProperty("IsFederalFiling")
        private Boolean isFederalFiling;

        @JsonProperty("IsStateFiling")
        private Boolean isStateFiling;

        @JsonProperty("IsPostal")
        private Boolean isPostal;

        @JsonProperty("IsOnlineAccess")
        private Boolean isOnlineAccess;

        @JsonProperty("IsScheduleFiling")
        private Boolean isScheduleFiling;

        @JsonProperty("ScheduleFiling")
        private ScheduleFilingDTO scheduleFiling;
    }

    @Data
    public static class ScheduleFilingDTO {
        @JsonProperty("EfileDate")
        private String efileDate;
    }

    @Data
    public static class ReturnHeaderDTO {
        @JsonProperty("Business")
        private BusinessDTO business;
    }

    @Data
    public static class BusinessDTO {
        @JsonProperty("BusinessId")
        private UUID businessId;

        @JsonProperty("BusinessNm")
        private String businessNm;

        @JsonProperty("FirstNm")
        private String firstNm;

        @JsonProperty("MiddleNm")
        private String middleNm;

        @JsonProperty("LastNm")
        private String lastNm;

        @JsonProperty("Suffix")
        private String suffix;

        @JsonProperty("PayerRef")
        private String payerRef;

        @JsonProperty("TradeNm")
        private String tradeNm;

        @JsonProperty("IsEIN")
        private Boolean isEIN;

        @JsonProperty("EINorSSN")
        private String einOrSSN;

        @JsonProperty("Email")
        private String email;

        @JsonProperty("ContactNm")
        private String contactNm;

        @JsonProperty("Phone")
        private String phone;

        @JsonProperty("PhoneExtn")
        private String phoneExtn;

        @JsonProperty("Fax")
        private String fax;

        @JsonProperty("BusinessType")
        private String businessType;

        @JsonProperty("SigningAuthority")
        private SigningAuthorityDTO signingAuthority;

        @JsonProperty("KindOfEmployer")
        private String kindOfEmployer;

        @JsonProperty("KindOfPayer")
        private String kindOfPayer;

        @JsonProperty("IsBusinessTerminated")
        private Boolean isBusinessTerminated;

        @JsonProperty("IsForeign")
        private Boolean isForeign;

        @JsonProperty("USAddress")
        private USAddressDTO usAddress;

        @JsonProperty("ForeignAddress")
        private ForeignAddressDTO foreignAddress;
    }

    @Data
    public static class SigningAuthorityDTO {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Phone")
        private String phone;

        @JsonProperty("BusinessMemberType")
        private String businessMemberType;
    }

    @Data
    public static class USAddressDTO {
        @JsonProperty("Address1")
        private String address1;

        @JsonProperty("Address2")
        private String address2;

        @JsonProperty("City")
        private String city;

        @JsonProperty("State")
        private String state;

        @JsonProperty("ZipCd")
        private String zipCd;
    }

    @Data
    public static class ForeignAddressDTO {
        @JsonProperty("Address1")
        private String address1;

        @JsonProperty("Address2")
        private String address2;

        @JsonProperty("City")
        private String city;

        @JsonProperty("ProvinceOrStateNm")
        private String provinceOrStateNm;

        @JsonProperty("Country")
        private String country;

        @JsonProperty("PostalCd")
        private String postalCd;
    }

    @Data
    public static class ReturnDataDTO {
        @JsonProperty("RecordId")
        private UUID recordId;

        @JsonProperty("SequenceId")
        private String sequenceId;

        @JsonProperty("Transaction")
        private TransactionDTO transaction;
    }

    @Data
    public static class TransactionDTO {
        @JsonProperty("TransactionId")
        private UUID transactionId;

        @JsonProperty("RecipientId")
        private UUID recipientId;

        @JsonProperty("TransactionDate")
        private String transactionDate;

        @JsonProperty("TransactionAmount")
        private Double transactionAmount;

        @JsonProperty("TransactionType")
        private String transactionType;

        @JsonProperty("PaymentCardIndicator")
        private Boolean paymentCardIndicator;

        @JsonProperty("MerchantCategoryCode")
        private String merchantCategoryCode;

        @JsonProperty("AccountNumber")
        private String accountNumber;

        @JsonProperty("StateCd")
        private String stateCd;

        @JsonProperty("StateIdNum")
        private String stateIdNum;

        @JsonProperty("StateWH")
        private Double stateWH;
    }
}
