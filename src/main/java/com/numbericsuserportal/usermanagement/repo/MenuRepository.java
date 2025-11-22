package com.numbericsuserportal.usermanagement.repo;

import com.numbericsuserportal.usermanagement.domain.Menu;
import com.numbericsuserportal.usermanagement.domain.MenuPermission;
import com.numbericsuserportal.usermanagement.domain.PortalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {
    
    // Derived queries for simple operations
    Optional<Menu> findByMenuNameAndPortalType(String menuName, PortalType portalType);
    
    boolean existsByMenuNameAndPortalType(String menuName, PortalType portalType);
    
    List<Menu> findByPortalTypeAndIsActiveTrueOrderByMenuOrderAsc(PortalType portalType);
    
    List<Menu> findByPortalTypeAndParentMenuIdIsNullAndIsActiveTrueOrderByMenuOrderAsc(PortalType portalType);
    
    List<Menu> findByParentMenuIdAndIsActiveTrueOrderByMenuOrderAsc(Long parentMenuId);

    List<MenuPermission> findByMenuId(Long menuId);
}
