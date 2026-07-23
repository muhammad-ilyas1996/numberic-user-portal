package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.InvoiceAndTaxDTO;
import com.numbericsuserportal.invoice.dto.InvoiceSearch;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;

public interface InvoiceAndTaxService {

    InvoiceAndTaxDTO createInvoiceAndTax(InvoiceAndTaxDTO dto, User currentUser);

    Page<InvoiceAndTaxEntity> searchInvoice(InvoiceSearch requestDTO, User currentUser);

    InvoiceAndTaxEntity getInvoiceDetail(Long id, User currentUser);

    InvoiceAndTaxEntity getInvoiceDetailByCustomerName(String customerName, User currentUser);

    InvoiceAndTaxEntity getInvoiceDetailByInvoiceNumber(String invoiceNum, User currentUser);

    /** Ensures invoice exists, is active, and current user may access it. */
    void requireAccessibleInvoice(Long invoiceId, User currentUser);

    InvoiceAndTaxDTO updateInvoice(Long id, InvoiceAndTaxDTO dto, User currentUser);

    /** Update only invoice status (for listing UI). */
    InvoiceAndTaxDTO updateInvoiceStatus(Long id, String invoiceStatus, User currentUser);

    void deleteInvoice(Long id, User currentUser);
}
