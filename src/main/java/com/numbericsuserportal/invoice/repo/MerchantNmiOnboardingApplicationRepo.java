package com.numbericsuserportal.invoice.repo;

import com.numbericsuserportal.invoice.entity.MerchantNmiOnboardingApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantNmiOnboardingApplicationRepo extends JpaRepository<MerchantNmiOnboardingApplication, Long> {

    Optional<MerchantNmiOnboardingApplication> findFirstByUserIdOrderByIdDesc(Long userId);

    Optional<MerchantNmiOnboardingApplication> findFirstByNmiApplicationIdOrderByIdDesc(String nmiApplicationId);
}
