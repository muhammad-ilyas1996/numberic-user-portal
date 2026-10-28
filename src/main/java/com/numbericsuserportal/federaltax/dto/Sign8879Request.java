package com.numbericsuserportal.federaltax.dto;

import lombok.Data;

@Data
public class Sign8879Request {
    /** TAXPAYER or SPOUSE */
    private String signerRole = "TAXPAYER";
    private String signerName;
    private Boolean consent = Boolean.TRUE;
    private String signatureMethod = "CLICK_TO_SIGN";
}
