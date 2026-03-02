package com.numbericsuserportal.invoice.repo;

import com.numbericsuserportal.invoice.entity.InvoiceSendLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceSendLogRepo extends JpaRepository<InvoiceSendLog, Long> {

    Page<InvoiceSendLog> findByInvoiceIdOrderBySentAtDesc(Long invoiceId, Pageable pageable);

    Optional<InvoiceSendLog> findByToken(String token);
}
