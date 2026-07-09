package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

@Data
public class TaalrLlcDraft {
    private Long formationId;
    private String jurisdiction;
    private String llcName;
    private String ownerFirstName;
    private String ownerLastName;
    private String filingSpeed;
    private Boolean addonEin;
}
