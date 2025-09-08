package com.medicalbillinguserportal.usermanagement.repo;

import com.medicalbillinguserportal.usermanagement.domain.PortalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortalTypeRepository extends JpaRepository<PortalType, Long> {
    
    Optional<PortalType> findByPortalName(String portalName);
    
    Optional<PortalType> findByPortalTypeId(Long portalTypeId);
}


