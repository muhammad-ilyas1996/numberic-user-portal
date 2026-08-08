package com.numbericsuserportal.taxintake.repo;

import com.numbericsuserportal.taxintake.entity.ProconnectTaxIntakeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxIntakeRecordRepository extends JpaRepository<ProconnectTaxIntakeRecord, Long> {

    List<ProconnectTaxIntakeRecord> findByUserIdAndSoftwareNameIgnoreCaseAndIsActiveTrueOrderByCreatedOnDesc(
            Long userId, String softwareName);

    List<ProconnectTaxIntakeRecord> findByUserIdAndIsActiveTrueOrderByCreatedOnDesc(Long userId);

    Optional<ProconnectTaxIntakeRecord> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    Optional<ProconnectTaxIntakeRecord> findByIdAndUserIdAndSoftwareNameIgnoreCaseAndIsActiveTrue(
            Long id, Long userId, String softwareName);
}
