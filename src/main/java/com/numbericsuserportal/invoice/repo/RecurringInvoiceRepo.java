package com.numbericsuserportal.invoice.repo;

import com.numbericsuserportal.invoice.entity.RecurringInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringInvoiceRepo extends JpaRepository<RecurringInvoice, Long> {

    Page<RecurringInvoice> findByStatusOrderByCreatedOnDesc(String status, Pageable pageable);

    Page<RecurringInvoice> findAllByOrderByCreatedOnDesc(Pageable pageable);

    Page<RecurringInvoice> findByCreatedByOrderByCreatedOnDesc(String createdBy, Pageable pageable);

    Page<RecurringInvoice> findByStatusAndCreatedByOrderByCreatedOnDesc(String status, String createdBy, Pageable pageable);

    List<RecurringInvoice> findByStatusAndNextRunOnLessThanEqualOrderByNextRunOnAsc(String status, LocalDate date);
}
