package com.numbericsuserportal.invoice.dto;

import lombok.Data;

import java.util.Map;

/** Request to pay an invoice using the link token (card details sent to NMI). */
@Data
public class InvoicePayWithTokenRequestDto {

    private String token;
    /** Preferred: single-use NMI Collect.js payment_token so raw card data does not hit this backend. */
    private String paymentToken;
    /** Legacy fallback. Prefer paymentToken for lower PCI exposure. */
    private String cardNumber;
    private String cardExpiry;   // MMYY
    private String cardCvv;
    /** Optional: firstName, lastName, email, address, city, state, zip, country, phone */
    private Map<String, String> customerInfo;
}
