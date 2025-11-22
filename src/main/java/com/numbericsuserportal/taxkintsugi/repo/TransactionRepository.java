package com.numbericsuserportal.taxkintsugi.repo;

import com.numbericsuserportal.taxkintsugi.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    // you can add findByExternalId if needed
    TransactionEntity findByExternalId(String externalId);
}