package com.numbericsuserportal.invoice.repo;

import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceAndTaxRepo extends JpaRepository<InvoiceAndTaxEntity, Long >, JpaSpecificationExecutor<InvoiceAndTaxEntity> {
    Optional<InvoiceAndTaxEntity> findByIdAndIsActiveTrue(long id);
    Optional<InvoiceAndTaxEntity> findByInvoiceNum(String invoiceNum);
    Optional<InvoiceAndTaxEntity> findByCustomerName(String CustomerName);
}
