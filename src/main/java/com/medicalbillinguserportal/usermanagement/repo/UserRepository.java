package com.medicalbillinguserportal.usermanagement.repo;

import com.medicalbillinguserportal.usermanagement.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email (for login or uniqueness check)
//    @EntityGraph(attributePaths = {
//            "userRoles",
//            "userRoles.role",
//            "userRoles.role.rolePermissions",
//            "userRoles.role.rolePermissions.permission"
//    })
//    Optional<User> findByEmail(String email);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailAndUserIdIsNot(String email, Long userId);
    
    // Check if user exists by username
    boolean existsByUsername(String username);
    
    // Check if user exists by email
    boolean existsByEmail(String email);

   /* @Query("SELECT u FROM User u WHERE (:email IS NULL OR u.email LIKE %:email%) " +
            "AND (:userType IS NULL OR u.userType = :userType) AND u.isDeleted = false AND u.practice IS NULL AND u.userId != :loggedInUserId")
    List<User> findAdminsByEmailAndUserType(
            @Param("email") String email,
            @Param("userType") String userType,
            @Param("loggedInUserId") Long loggedInUserId);
*/
    // Check if user exists by ID and is not deleted
    boolean existsByUserIdAndIsDeletedFalse(Long id);

    Optional<User> findById(Long id);
    
    // Find all users that are not deleted
    List<User> findByIsDeletedFalse();

   // Optional<User> findByUserIdAndPhone(Long userId,String Phone);
   // Optional<User> findByResetToken(String resetToken);

}