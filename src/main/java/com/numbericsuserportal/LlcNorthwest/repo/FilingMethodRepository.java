package com.numbericsuserportal.LlcNorthwest.repo;

import com.numbericsuserportal.LlcNorthwest.entity.FilingMethodEntity;
import com.numbericsuserportal.LlcNorthwest.entity.FilingProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FilingMethodRepository extends JpaRepository<FilingMethodEntity, Long> {
    Optional<FilingMethodEntity> findByExternalId(UUID externalId);
    boolean existsByExternalId(UUID externalId);

    @Modifying
    @Query("DELETE FROM FilingMethodEntity fm WHERE fm.filingProduct = :filingProduct")
    void deleteByFilingProduct(@Param("filingProduct") FilingProductEntity filingProduct);
    
    @Modifying
    @Query("DELETE FROM FilingMethodEntity fm WHERE fm.filingProduct.filingProductId = :productId")
    void deleteByFilingProductId(@Param("productId") Long productId);
}

