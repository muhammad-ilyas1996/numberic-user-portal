package com.medicalbillinguserportal.usermanagement.repo;

import com.medicalbillinguserportal.usermanagement.domain.RolePermission;
import com.medicalbillinguserportal.usermanagement.domain.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.permission WHERE rp.role.roleId = :roleId")
    List<RolePermission> findByRoleRoleId(@Param("roleId") Long roleId);
    
    // Alternative method for testing
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    List<RolePermission> findByRoleRoleIdSimple(@Param("roleId") Long roleId);
    
    // Delete all permissions for a role
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
    
    // Delete all permissions for a permission
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.permission.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") Long permissionId);
}


