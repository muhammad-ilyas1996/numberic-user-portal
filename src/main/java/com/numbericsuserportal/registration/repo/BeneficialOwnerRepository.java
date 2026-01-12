package com.numbericsuserportal.registration.repo;

import com.numbericsuserportal.registration.entity.BeneficialOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficialOwnerRepository extends JpaRepository<BeneficialOwner, Long> {
    List<BeneficialOwner> findByUserId(Long userId);
}

