package com.numbericsuserportal.ai.action.dto;

import com.numbericsuserportal.ai.action.TaalrIntent;
import lombok.Data;

@Data
public class TaalrIntentParseResult {

    private TaalrIntent intent = TaalrIntent.CHAT;
    private TaalrInvoiceDraft invoice = new TaalrInvoiceDraft();
    /** When intent is INVOICE but a required field is missing — e.g. customerName, amount */
    private String missingField;
    /** Suggested follow-up question for the user */
    private String question;
    /** For INVOICE_LIST: ALL, UNPAID, PAID, DRAFT */
    private String statusFilter;
}
