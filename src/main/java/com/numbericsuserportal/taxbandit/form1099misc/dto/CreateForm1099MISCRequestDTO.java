package com.numbericsuserportal.taxbandit.form1099misc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating Form 1099-MISC.
 * Used to report miscellaneous payments such as rents, prizes, medical and health care payments, etc.
 */
@Data
public class CreateForm1099MISCRequestDTO {
    
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
        
        @JsonProperty("MISCFormData")
        private MISCFormDataDTO miscFormData;
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
    public static class MISCFormDataDTO {
        // Box 1: Rents
        @JsonProperty("B1Rents")
        private Double b1Rents;
        
        // Box 2: Royalties
        @JsonProperty("B2Royalties")
        private Double b2Royalties;
        
        // Box 3: Other income
        @JsonProperty("B3OtherIncome")
        private Double b3OtherIncome;
        
        // Box 4: Federal income tax withheld
        @JsonProperty("B4FedTaxWH")
        private Double b4FedTaxWH;
        
        // Box 5: Fishing boat proceeds
        @JsonProperty("B5FishingBoatProceeds")
        private Double b5FishingBoatProceeds;
        
        // Box 6: Medical and health care payments
        @JsonProperty("B6MedicalHealthCare")
        private Double b6MedicalHealthCare;
        
        // Box 7: Nonemployee compensation (if applicable)
        @JsonProperty("B7NonemployeeComp")
        private Double b7NonemployeeComp;
        
        // Box 8: Substitute payments in lieu of dividends or interest
        @JsonProperty("B8SubstitutePayments")
        private Double b8SubstitutePayments;
        
        // Box 9: Payer made direct sales of $5,000 or more of consumer products
        @JsonProperty("B9DirectSales")
        private Boolean b9DirectSales;
        
        // Box 10: Crop insurance proceeds
        @JsonProperty("B10CropInsurance")
        private Double b10CropInsurance;
        
        // Box 11: Gross proceeds paid to an attorney
        @JsonProperty("B11GrossProceedsAttorney")
        private Double b11GrossProceedsAttorney;
        
        // Box 12: Section 409A deferrals
        @JsonProperty("B12Section409ADeferrals")
        private Double b12Section409ADeferrals;
        
        // Box 13: Excess golden parachute payments
        @JsonProperty("B13ExcessGoldenParachute")
        private Double b13ExcessGoldenParachute;
        
        // Box 14: Nonqualified deferred compensation
        @JsonProperty("B14NonqualifiedDeferredComp")
        private Double b14NonqualifiedDeferredComp;
        
        // Box 15: State tax withheld
        @JsonProperty("B15StateTaxWH")
        private Double b15StateTaxWH;
        
        // Box 16: State/Payer's state number
        @JsonProperty("B16State")
        private String b16State;
        
        // Box 17: State income
        @JsonProperty("B17StateIncome")
        private Double b17StateIncome;
        
        // Box 18: Local income tax withheld
        @JsonProperty("B18LocalTaxWH")
        private Double b18LocalTaxWH;
        
        // Box 19: Name of locality
        @JsonProperty("B19NameOfLocality")
        private String b19NameOfLocality;
        
        // Box 20: Local income
        @JsonProperty("B20LocalIncome")
        private Double b20LocalIncome;
        
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



