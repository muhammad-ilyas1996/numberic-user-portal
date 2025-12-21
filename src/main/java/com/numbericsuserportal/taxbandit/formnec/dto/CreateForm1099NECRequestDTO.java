package com.numbericsuserportal.taxbandit.formnec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateForm1099NECRequestDTO {
    
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
        @JsonProperty("EfileDate")
        private String efileDate; // MM/DD/YYYY or MM-DD-YYYY
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
        @JsonProperty("SequenceId")
        private String sequenceId;
        
        @JsonProperty("IsPostal")
        private Boolean isPostal;
        
        @JsonProperty("IsOnlineAccess")
        private Boolean isOnlineAccess;
        
        @JsonProperty("IsForced")
        private Boolean isForced;
        
        @JsonProperty("Recipient")
        private RecipientDTO recipient;
        
        @JsonProperty("NECFormData")
        private NECFormDataDTO necFormData;
    }
    
    @Data
    public static class RecipientDTO {
        @JsonProperty("RecipientId")
        private UUID recipientId;
        
        @JsonProperty("TINType")
        private String tinType; // SSN, EIN, ITIN, ATIN, NA
        
        @JsonProperty("TIN")
        private String tin;
        
        @JsonProperty("PayeeRef")
        private String payeeRef;
        
        @JsonProperty("FirstPayeeNm")
        private String firstPayeeNm;
        
        @JsonProperty("SecondPayeeNm")
        private String secondPayeeNm;
        
        @JsonProperty("FirstNm")
        private String firstNm;
        
        @JsonProperty("MiddleNm")
        private String middleNm;
        
        @JsonProperty("LastNm")
        private String lastNm;
        
        @JsonProperty("Suffix")
        private String suffix;
        
        @JsonProperty("IsForeign")
        private Boolean isForeign;
        
        @JsonProperty("USAddress")
        private USAddressDTO usAddress;
        
        @JsonProperty("ForeignAddress")
        private ForeignAddressDTO foreignAddress;
        
        @JsonProperty("Email")
        private String email;
        
        @JsonProperty("Fax")
        private String fax;
        
        @JsonProperty("Phone")
        private String phone;
    }
    
    @Data
    public static class NECFormDataDTO {
        @JsonProperty("B1NEC")
        private Double b1NEC; // Nonemployee compensation amount
        
        @JsonProperty("B2IsDirectSales")
        private Boolean b2IsDirectSales;
        
        @JsonProperty("B3EPP")
        private Double b3EPP; // Excess golden parachute payments
        
        @JsonProperty("B4FedTaxWH")
        private Double b4FedTaxWH; // Federal Tax Withheld
        
        @JsonProperty("IsFATCA")
        private Boolean isFATCA;
        
        @JsonProperty("AccountNum")
        private String accountNum;
        
        @JsonProperty("Is2ndTINnot")
        private Boolean is2ndTINnot;
        
        @JsonProperty("States")
        private List<StateReturnDTO> states;
        
        @JsonProperty("StateReconData")
        private Object stateReconData; // State-specific recon fields
    }
    
    @Data
    public static class StateReturnDTO {
        @JsonProperty("StateCd")
        private String stateCd;
        
        @JsonProperty("StateIdNum")
        private String stateIdNum;
        
        @JsonProperty("StateWH")
        private Double stateWH; // State income tax withheld
        
        @JsonProperty("StateIncome")
        private Double stateIncome; // Amount of payment for State
    }
}

