package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.RecurringInvoiceCreateRequestDto;
import com.numbericsuserportal.invoice.dto.RecurringInvoiceDto;
import com.numbericsuserportal.invoice.dto.RecurringInvoiceSearch;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.data.domain.Page;

public interface RecurringInvoiceService {

    RecurringInvoiceDto create(RecurringInvoiceCreateRequestDto request, User currentUser);

    RecurringInvoiceDto update(Long id, RecurringInvoiceCreateRequestDto request, User currentUser);

    Page<RecurringInvoiceDto> list(RecurringInvoiceSearch search);

    RecurringInvoiceDto getById(Long id);

    void pause(Long id, User currentUser);

    void resume(Long id, User currentUser);

    void stop(Long id, User currentUser);

    /** Called by scheduler: generate invoices for profiles due today. */
    void runScheduledGeneration();
}
