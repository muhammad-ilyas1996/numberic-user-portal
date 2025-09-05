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


    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.permission WHERE rp.role.roleId = :roleId")
    List<RolePermission> findByRoleRoleId(@Param("roleId") Long roleId);
    
    // Alternative method for testing
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleId = :roleId")
    List<RolePermission> findByRoleRoleIdSimple(@Param("roleId") Long roleId);
}


