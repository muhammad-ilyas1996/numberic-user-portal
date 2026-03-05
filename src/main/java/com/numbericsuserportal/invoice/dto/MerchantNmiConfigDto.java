package com.numbericsuserportal.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * For Settings page: get (masked) and save NMI config.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantNmiConfigDto {

    private Long userId;
    /** api_key or username_password */
    private String authMethod;
    /** Masked for GET: e.g. "****xxxx"; full for PUT */
    private String securityKey;
    private String nmiUsername;
    /** Masked for GET; full for PUT */
    private String nmiPassword;
    private String transactionUrl;
    /** True if merchant has configured NMI (any credential set) */
    private Boolean configured;
}
