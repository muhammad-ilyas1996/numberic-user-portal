package com.numbericsuserportal.ai.action.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaalrLlcDraft {
    private Long formationId;
    private String jurisdiction;
    private String llcName;
    private String altName;
    private String industry;
    private String businessPurpose;
    /** single | multi */
    private String ownershipType;
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerDob;
    private String ownerSsnLast4;
    private Integer ownerOwnershipPct;
    private String ownerTitle;
    private List<TaalrLlcMemberDraft> members = new ArrayList<>();
    /** Transient member being collected. */
    private TaalrLlcMemberDraft pendingMember;
    private Boolean askingAddAnotherMember;
    private String filingSpeed;
    private Boolean addonEin;
    private Boolean addonScorp;
    /** NUMBRICS_NW or OWN */
    private String agentType;
    private String ownAgentName;
    private String ownAgentStreet;
    private String ownAgentCity;
    private String ownAgentState;
    private String ownAgentZip;
    /** When true, waiting for YES to run name-check + prepare. */
    private boolean awaitingPrepareConfirm;
}
