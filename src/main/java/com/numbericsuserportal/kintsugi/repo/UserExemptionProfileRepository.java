package com.numbericsuserportal.kintsugi.repo;

import com.numbericsuserportal.kintsugi.domain.SalesTaxBusinessType;
import com.numbericsuserportal.kintsugi.entity.UserExemptionProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserExemptionProfileRepository extends JpaRepository<UserExemptionProfileEntity, String> {

    Optional<UserExemptionProfileEntity> findByUserIdAndStateCodeAndBusinessType(
            Long userId, String stateCode, SalesTaxBusinessType businessType);

    List<UserExemptionProfileEntity> findByUserId(Long userId);
}
