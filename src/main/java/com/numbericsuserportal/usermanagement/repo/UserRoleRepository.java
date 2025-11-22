package com.numbericsuserportal.usermanagement.repo;

import com.numbericsuserportal.usermanagement.domain.UserRole;
import com.numbericsuserportal.usermanagement.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    // Fix method - use correct field names
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.userId = :userId AND ur.isActive = true")
    List<UserRole> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    // Alternative method using derived query
    List<UserRole> findByUserUserIdAndIsActiveTrue(Long userId);

    // Keep existing methods
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.userId = :userId")
    List<UserRole> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.role.roleId = :roleId")
    List<UserRole> findByRoleId(@Param("roleId") Long roleId);

}
