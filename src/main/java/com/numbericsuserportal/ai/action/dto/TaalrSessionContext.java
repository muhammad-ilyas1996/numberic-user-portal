package com.numbericsuserportal.ai.action.dto;

import com.numbericsuserportal.ai.action.TaalrPendingAction;
import lombok.Data;

@Data
public class TaalrSessionContext {

    private TaalrPendingAction pendingAction;
    private TaalrReceiptDraft receiptDraft = new TaalrReceiptDraft();
    private TaalrInvoiceDraft invoiceDraft = new TaalrInvoiceDraft();
    private TaalrLlcDraft llcDraft = new TaalrLlcDraft();
    private TaalrSalesTaxDraft salesTaxDraft = new TaalrSalesTaxDraft();
    private Long createdInvoiceId;
}
