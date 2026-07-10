package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

@Data
public class TaalrLlcMemberDraft {
    private String firstName;
    private String lastName;
    private String dob;
    private String ssnLast4;
    private Integer ownershipPct;
    private String title;
    private Boolean primaryMember;
}
