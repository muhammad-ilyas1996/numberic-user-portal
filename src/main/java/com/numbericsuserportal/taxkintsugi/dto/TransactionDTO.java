package com.numbericsuserportal.taxkintsugi.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class TransactionDTO {
    private Instant date;
    private String externalId;
    private String currency;
    private String description;
    private String source;
    private Boolean marketplace;

    private List<AddressDTO> addresses;
    private List<TransactionItemDTO> transactionItems;

    private Boolean nexusMet;
    private Boolean hasActiveRegistration;
    private Double totalTaxAmountCalculated;
    private Double taxableAmount;
    private Double taxRateCalculated;

    private Object taxResponse; // <- add this line
}