package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.Data;

@Data
public class FormationMemberDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String dob; // yyyy-MM-dd
    private String ssnLast4;
    private Integer ownershipPct;
    private String title;
    private Boolean primaryMember;
}

