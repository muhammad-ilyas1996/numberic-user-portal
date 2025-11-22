package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.InvoiceAndTaxDTO;
import com.numbericsuserportal.invoice.dto.InvoiceSearch;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;

public interface InvoiceAndTaxService {

    InvoiceAndTaxDTO createInvoiceAndTax(InvoiceAndTaxDTO dto, User currentUser);
    public Page<InvoiceAndTaxEntity> searchInvoice(InvoiceSearch requestDTO);
    public InvoiceAndTaxEntity getInvoiceDetail(Long id);
    public InvoiceAndTaxEntity getInvoiceDetailByCustomerName(String customerName);
    public InvoiceAndTaxEntity getInvoiceDetailByInvoiceNumber(String invoiceNum);

}
