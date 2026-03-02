package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.PaymentTransactionDto;
import com.numbericsuserportal.invoice.dto.PaymentTransactionSearch;
import org.springframework.data.domain.Page;

public interface PaymentTransactionService {

    Page<PaymentTransactionDto> list(PaymentTransactionSearch search);

    /** Save a successful payment record (called when invoice is paid). */
    void saveSuccess(Long invoiceId, String invoiceNum, Double amount, String currency,
                     String gateway, String gatewayTransactionId, String authCode,
                     String payerEmail, String description);
}
