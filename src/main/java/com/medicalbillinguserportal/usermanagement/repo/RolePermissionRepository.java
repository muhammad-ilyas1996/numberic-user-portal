package com.medicalbillinguserportal.usermanagement.repo;

import com.medicalbillinguserportal.usermanagement.domain.RolePermission;
import com.medicalbillinguserportal.usermanagement.domain.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.permissionId = :permissionId")
    List<RolePermission> findByPermissionId(@Param("permissionId") Long permissionId);
}


