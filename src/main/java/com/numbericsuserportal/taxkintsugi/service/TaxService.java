package com.numbericsuserportal.taxkintsugi.service;

import com.numbericsuserportal.taxkintsugi.dto.TransactionDTO;

public interface TaxService {
    TransactionDTO calculateTax(TransactionDTO requestDto);
}
