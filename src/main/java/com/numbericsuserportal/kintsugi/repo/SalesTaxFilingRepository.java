package com.numbericsuserportal.kintsugi.repo;

import com.numbericsuserportal.kintsugi.entity.SalesTaxFilingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesTaxFilingRepository extends JpaRepository<SalesTaxFilingEntity, String> {

    List<SalesTaxFilingEntity> findByUserIdOrderByPeriodEndDesc(Long userId);

    Optional<SalesTaxFilingEntity> findByIdAndUserId(String id, Long userId);
}
