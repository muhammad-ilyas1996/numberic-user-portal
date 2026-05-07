package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateStep3DetailsRequestDTO {
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerDob; // yyyy-MM-dd
    private String ownerSsnLast4; // plain input; we store as-is for now (encryption hook later)
    private Integer ownershipPct;
    private String ownerTitle;

    private String managementType; // member|manager

    private Boolean addressSameAsHome;
    private String businessStreet;
    private String businessCity;
    private String businessState;
    private String businessZip;

    private String filingSpeed; // standard|expedited|sameday
    private Boolean addonEin;
    private Boolean addonScorp;

    // Optional members payload for single/multi-member support
    private List<FormationMemberDTO> members;
}

