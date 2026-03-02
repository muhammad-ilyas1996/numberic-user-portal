package com.numbericsuserportal.invoice.scheduler;

import com.numbericsuserportal.invoice.service.RecurringInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs daily to generate invoices for recurring profiles that are due (nextRunOn <= today).
 */
@Component
public class RecurringInvoiceScheduler {

    @Autowired
    private RecurringInvoiceService recurringInvoiceService;

    /** Run every day at 9:00 AM server time */
    @Scheduled(cron = "${app.recurring-invoice.cron:0 0 9 * * ?}")
    public void generateDueRecurringInvoices() {
        recurringInvoiceService.runScheduledGeneration();
    }
}
