package com.numbericsuserportal.usermanagement.repo;

import com.numbericsuserportal.usermanagement.domain.Permission;
import com.numbericsuserportal.usermanagement.domain.PortalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    
    // Derived queries for simple operations
    Optional<Permission> findByCodeNameAndPortalType(String codeName, PortalType portalType);
    
    boolean existsByCodeNameAndPortalType(String codeName, PortalType portalType);
    
    List<Permission> findByPortalTypeAndIsActiveTrue(PortalType portalType);
    
    List<Permission> findByPortalTypeAndCategoryAndIsActiveTrue(PortalType portalType, String category);
}
