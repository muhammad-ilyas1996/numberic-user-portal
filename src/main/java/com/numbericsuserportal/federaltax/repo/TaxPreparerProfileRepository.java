package com.numbericsuserportal.federaltax.repo;

import com.numbericsuserportal.federaltax.entity.TaxPreparerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaxPreparerProfileRepository extends JpaRepository<TaxPreparerProfile, Long> {
    List<TaxPreparerProfile> findByActiveTrue();

    Optional<TaxPreparerProfile> findByUserId(Long userId);
}
