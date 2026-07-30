package com.numbericsuserportal.taxintake.repo;

import com.numbericsuserportal.taxintake.entity.ProconnectTaxIntakeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProconnectTaxIntakeRepository extends JpaRepository<ProconnectTaxIntakeRecord, Long> {
    List<ProconnectTaxIntakeRecord> findByUserIdAndIsActiveTrueOrderByCreatedOnDesc(Long userId);

    Optional<ProconnectTaxIntakeRecord> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);
}
