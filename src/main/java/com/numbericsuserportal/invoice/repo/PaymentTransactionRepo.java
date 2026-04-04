package com.numbericsuserportal.invoice.repo;

import com.numbericsuserportal.invoice.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, Long>, JpaSpecificationExecutor<PaymentTransaction> {

    Page<PaymentTransaction> findByInvoiceIdOrderByPaidAtDesc(Long invoiceId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.gateway = 'NMI' AND p.status = 'SUCCESS'")
    Double sumNmiSuccessAmountAll();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.gateway = 'NMI' AND p.status = 'SUCCESS' "
            + "AND p.invoiceId IN (SELECT i.id FROM InvoiceAndTaxEntity i WHERE i.createdBy = :ownerUserId AND i.isActive = true)")
    Double sumNmiSuccessAmountForInvoiceOwner(@Param("ownerUserId") String ownerUserId);
}
