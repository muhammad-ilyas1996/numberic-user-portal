package com.numbericsuserportal.invoice.repo;

import com.numbericsuserportal.invoice.entity.MerchantNmiConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantNmiConfigRepo extends JpaRepository<MerchantNmiConfig, Long> {

    Optional<MerchantNmiConfig> findByUserId(Long userId);
}
