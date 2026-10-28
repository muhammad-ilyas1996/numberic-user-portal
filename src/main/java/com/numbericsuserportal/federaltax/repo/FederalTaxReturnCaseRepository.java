package com.numbericsuserportal.federaltax.repo;

import com.numbericsuserportal.federaltax.domain.ClientTaxStatus;
import com.numbericsuserportal.federaltax.entity.FederalTaxReturnCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FederalTaxReturnCaseRepository extends JpaRepository<FederalTaxReturnCase, Long> {
    Optional<FederalTaxReturnCase> findByPublicCaseId(String publicCaseId);

    List<FederalTaxReturnCase> findByTaxpayerUserIdOrderByUpdatedAtDesc(Long taxpayerUserId);

    List<FederalTaxReturnCase> findByAssignedPreparerUserIdAndClientStatusInOrderByUpdatedAtDesc(
            Long preparerUserId, List<ClientTaxStatus> statuses);

    long countByAssignedPreparerUserIdAndClientStatusIn(Long preparerUserId, List<ClientTaxStatus> statuses);
}
