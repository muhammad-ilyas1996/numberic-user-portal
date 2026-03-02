package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.InvoicePayByTokenDto;
import com.numbericsuserportal.invoice.dto.InvoicePayWithTokenRequestDto;
import com.numbericsuserportal.invoice.dto.InvoiceSendHistoryItemDto;
import com.numbericsuserportal.invoice.dto.InvoiceSendHistorySearch;
import com.numbericsuserportal.invoice.dto.SendInvoiceRequestDto;
import com.numbericsuserportal.invoice.dto.SendInvoiceResponseDto;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface InvoiceSendService {

    /**
     * Send invoice via WhatsApp (or email when implemented).
     * Generates payment link with token and logs the send.
     */
    SendInvoiceResponseDto sendInvoice(SendInvoiceRequestDto request, User currentUser);

    /**
     * Get send history for an invoice (paginated).
     */
    Page<InvoiceSendHistoryItemDto> getSendHistory(InvoiceSendHistorySearch search);

    /**
     * Get invoice summary by payment link token (public, for pay page).
     * Returns null or invalid DTO if token not found or invoice not payable.
     */
    InvoicePayByTokenDto getInvoiceByToken(String token);

    /**
     * Process payment for invoice using token (public). Validates token, charges via NMI, marks invoice PAID.
     * Returns map with success, message, transactionId (or error).
     */
    Map<String, Object> payWithToken(InvoicePayWithTokenRequestDto request);
}
