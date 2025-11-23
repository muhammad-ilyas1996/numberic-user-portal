package com.numbericsuserportal.usermanagement.repo;

import com.numbericsuserportal.usermanagement.domain.PortalType;
import com.numbericsuserportal.usermanagement.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    
    // Derived queries for simple operations
    Optional<Role> findByCodeNameAndPortalType(String codeName, PortalType portalType);
    
    boolean existsByCodeNameAndPortalType(String codeName, PortalType portalType);
    
    List<Role> findByPortalTypeAndIsActiveTrue(PortalType portalType);
    
    List<Role> findByPortalTypeAndIsSuperadminTrueAndIsActiveTrue(PortalType portalType);
    
    List<Role> findByPortalTypeAndIsReadonlyFalseAndIsActiveTrue(PortalType portalType);
    
    // New methods for Role Management
    boolean existsByCodeName(String codeName);
    
    Optional<Role> findByCodeName(String codeName);
    
    Page<Role> findByPortalTypeAndIsActiveTrue(PortalType portalType, Pageable pageable);
    
    Page<Role> findByIsActiveTrue(Pageable pageable);
}
