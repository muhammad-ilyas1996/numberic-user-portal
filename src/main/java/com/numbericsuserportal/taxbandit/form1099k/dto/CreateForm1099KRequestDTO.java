package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating Form 1099-K.
 * Used to report payment card and third party network transactions.
 */
@Data
public class CreateForm1099KRequestDTO {
    
    @JsonProperty("SubmissionManifest")
    private SubmissionManifestDTO submissionManifest;
    
    @JsonProperty("ReturnHeader")
    private ReturnHeaderDTO returnHeader;
    
    @JsonProperty("ReturnData")
    private List<ReturnDataDTO> returnData;
    
    @Data
    public static class SubmissionManifestDTO {
        @JsonProperty("TaxYear")
        private String taxYear;
        
        @JsonProperty("IRSFilingType")
        private String irsFilingType; // FIRE or IRIS
        
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
        @JsonProperty("IsScheduleFiling")
        private Boolean isScheduleFiling;
        
        @JsonProperty("ScheduleFilingDate")
        private String scheduleFilingDate;
    }
    
    @Data
    public static class ReturnHeaderDTO {
        @JsonProperty("Business")
        private BusinessDTO business;
    }
    
    @Data
    public static class BusinessDTO {
        @JsonProperty("BusinessId")
        private String businessId;
        
        @JsonProperty("BusinessNm")
        private String businessNm;
        
        @JsonProperty("FirstNm")
        private String firstNm;
        
        @JsonProperty("LastNm")
        private String lastNm;
        
        @JsonProperty("IsEIN")
        private Boolean isEIN;
        
        @JsonProperty("EINorSSN")
        private String einOrSSN;
        
        @JsonProperty("ContactNm")
        @JsonAlias("ContactName")
        private String contactNm;
        
        @JsonProperty("Email")
        private String email;
        
        @JsonProperty("Phone")
        private String phone;
        
        @JsonProperty("Fax")
        private String fax;
        
        @JsonProperty("BusinessType")
        private String businessType;
        
        @JsonProperty("IsBusinessTerminated")
        private Boolean isBusinessTerminated;
        
        @JsonProperty("IsForeign")
        private Boolean isForeign;
        
        @JsonProperty("BusinessMemberType")
        private String businessMemberType;
        
        @JsonProperty("SigningAuthority")
        private SigningAuthorityDTO signingAuthority;
        
        @JsonProperty("USAddress")
        private USAddressDTO usAddress;
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
    public static class AddressDTO {
        @JsonProperty("Address1")
        private String address1;
        
        @JsonProperty("Address2")
        private String address2;
        
        @JsonProperty("City")
        private String city;
        
        @JsonProperty("State")
        private String state;
        
        @JsonProperty("ZipCode")
        private String zipCode;
    }
    
    @Data
    public static class ReturnDataDTO {
        @JsonProperty("RecordId")
        private UUID recordId;
        
        @JsonProperty("IsForced")
        private Boolean isForced;
        
        @JsonProperty("Recipient")
        private RecipientDTO recipient;
        
        @JsonProperty("KFormData")
        private KFormDataDTO kFormData;
    }
    
    @Data
    public static class RecipientDTO {
        @JsonProperty("RecipientId")
        private String recipientId;
        
        @JsonProperty("TINType")
        private String tinType; // EIN, SSN, ITIN
        
        @JsonProperty("TIN")
        private String tin;
        
        @JsonProperty("FirstPayeeNm")
        private String firstPayeeNm;
        
        @JsonProperty("FirstNm")
        private String firstNm;
        
        @JsonProperty("LastNm")
        private String lastNm;
        
        @JsonProperty("Email")
        private String email;
        
        @JsonProperty("Phone")
        private String phone;
        
        @JsonProperty("Fax")
        private String fax;
        
        @JsonProperty("IsForeign")
        private Boolean isForeign;
        
        @JsonProperty("USAddress")
        private USAddressDTO usAddress;
    }
    
    @Data
    public static class KFormDataDTO {
        @JsonProperty("B1aGrossAmt")
        private Double b1aGrossAmt;
        
        @JsonProperty("B1bCardNotPresentTxns")
        private Double b1bCardNotPresentTxns;
        
        @JsonProperty("B2MerchantCd")
        private String b2MerchantCd;
        
        @JsonProperty("B3NumPymtTxns")
        private Integer b3NumPymtTxns;
        
        @JsonProperty("B4FedTaxWH")
        private Double b4FedTaxWH;
        
        @JsonProperty("FederalTaxWithheld")
        private Double federalTaxWithheld;
        
        @JsonProperty("StateTaxWithheld")
        private Double stateTaxWithheld;
        
        @JsonProperty("State")
        private String state;
        
        @JsonProperty("StateIdNumber")
        private String stateIdNumber;
        
        @JsonProperty("Is2ndTINnot")
        private Boolean is2ndTINnot;

        @JsonProperty("FilerIndicator")
        private String filerIndicator; // PSE, EPF

        @JsonProperty("IndicateTxnsReported")
        private String indicateTxnsReported; // Payment_Card, Third_Party_Network
    }
}

