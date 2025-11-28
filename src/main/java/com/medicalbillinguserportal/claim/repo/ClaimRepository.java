package com.medicalbillinguserportal.claim.repo;

import com.medicalbillinguserportal.claim.entity.ClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<ClaimEntity, Long> {
}
