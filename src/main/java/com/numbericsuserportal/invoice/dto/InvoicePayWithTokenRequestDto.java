package com.numbericsuserportal.invoice.dto;

import lombok.Data;

import java.util.Map;

/** Request to pay an invoice using the link token (card details sent to NMI). */
@Data
public class InvoicePayWithTokenRequestDto {

    private String token;
    private String cardNumber;
    private String cardExpiry;   // MMYY
    private String cardCvv;
    /** Optional: firstName, lastName, email, address, city, state, zip, country, phone */
    private Map<String, String> customerInfo;
}
