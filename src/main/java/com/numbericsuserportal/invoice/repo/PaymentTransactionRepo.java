package com.numbericsuserportal.invoice.repo;

import com.numbericsuserportal.invoice.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, Long>, JpaSpecificationExecutor<PaymentTransaction> {

    Page<PaymentTransaction> findByInvoiceIdOrderByPaidAtDesc(Long invoiceId, Pageable pageable);
}
