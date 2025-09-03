package com.medicalbillinguserportal.usermanagement.repo;

import com.medicalbillinguserportal.usermanagement.domain.UserRole;
import com.medicalbillinguserportal.usermanagement.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.userId = :userId AND ur.isActive = true")
    List<UserRole> findByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.role.roleId = :roleId AND ur.isActive = true")
    List<UserRole> findByRoleId(@Param("roleId") Long roleId);
}
