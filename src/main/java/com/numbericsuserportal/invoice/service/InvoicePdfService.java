package com.numbericsuserportal.invoice.service;

/** Generate invoice as PDF for download. */
public interface InvoicePdfService {

    /** Generate PDF for the given invoice id. Returns null if invoice not found or inactive. */
    byte[] generatePdf(Long invoiceId);
}
