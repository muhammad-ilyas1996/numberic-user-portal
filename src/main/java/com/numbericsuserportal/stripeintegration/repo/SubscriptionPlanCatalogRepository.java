package com.numbericsuserportal.stripeintegration.repo;

import com.numbericsuserportal.stripeintegration.entity.SubscriptionPlanCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanCatalogRepository extends JpaRepository<SubscriptionPlanCatalogEntity, Long> {

    Optional<SubscriptionPlanCatalogEntity> findByPlanCodeIgnoreCase(String planCode);

    List<SubscriptionPlanCatalogEntity> findByActiveTrueOrderBySortOrderAsc();

    List<SubscriptionPlanCatalogEntity> findAllByOrderBySortOrderAsc();

    boolean existsByPlanCodeIgnoreCase(String planCode);
}
