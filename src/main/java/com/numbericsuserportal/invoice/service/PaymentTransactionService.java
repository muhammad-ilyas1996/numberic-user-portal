package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.PaymentTransactionDto;
import com.numbericsuserportal.invoice.dto.PaymentTransactionSearch;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;

public interface PaymentTransactionService {

    Page<PaymentTransactionDto> list(PaymentTransactionSearch search, User currentUser);

    /** Save a successful payment record (called when invoice is paid). */
    void saveSuccess(Long invoiceId, String invoiceNum, Double amount, String currency,
                     String gateway, String gatewayTransactionId, String authCode,
                     String payerEmail, String description);

    /** Save a failed/declined attempt for audit and reconciliation. */
    void saveFailure(Long invoiceId, String invoiceNum, Double amount, String currency,
                     String gateway, String gatewayTransactionId, String payerEmail,
                     String description);

    /** Update an existing gateway transaction from webhook/reconciliation events. */
    boolean updateStatusByGatewayTransactionId(String gateway, String gatewayTransactionId,
                                               String status, String description);
}
