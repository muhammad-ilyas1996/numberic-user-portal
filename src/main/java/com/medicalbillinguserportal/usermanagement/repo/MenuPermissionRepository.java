package com.medicalbillinguserportal.usermanagement.repo;

import com.medicalbillinguserportal.usermanagement.domain.MenuPermission;
import com.medicalbillinguserportal.usermanagement.domain.MenuPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuPermissionRepository extends JpaRepository<MenuPermission, MenuPermissionId> {

    @Query("SELECT mp FROM MenuPermission mp WHERE mp.menu.menuId = :menuId")
    List<MenuPermission> findByMenuMenuId(@Param("menuId") Long menuId);

    @Query("SELECT mp FROM MenuPermission mp WHERE mp.permission.permissionId = :permissionId")
    List<MenuPermission> findByPermissionPermissionId(@Param("permissionId") Long permissionId);
}
