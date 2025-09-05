package com.medicalbillinguserportal.usermanagement.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "USERNAME", unique = true)
    @NotNull
    private String username;

    @Column(name = "email", nullable = false, length = 128)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "npi", length = 10)
    private String npi;

    @Column(name = "license_number", length = 20)
    private String licenseNumber;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = true)
    private Long createdBy;

    @Column(name = "updated_by", nullable = true)
    private Long updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_practice_admin", nullable = false)
    private Boolean isPracticeAdmin;

    @Column(name = "reset_token", length = 100)
    private String resetToken;

    @Column(name="reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

//    @Column(name = "must_change_password", nullable = false)
//    private Boolean mustChangePassword = false;

    @Column(name = "is_force_change_password", nullable = false)
    private Boolean isForceChangePassword = false;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "taxonomy_id", nullable = false)
    private Long taxonomyId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "portal_type_id", nullable = false)
    private PortalType portalType;

/*    @ManyToOne
    @JoinColumn(name = "provider_id")
    private ProviderEntity providerId;
*/

  /*  @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )*/
  /*  @JsonManagedReference
    private Set<Role> roles = new HashSet<>();
*/
    public enum UserType {
        admin, practice, provider, staff, patient
    }

 /*   @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<UserRoles> userRoles = new HashSet<>();
*/
    public enum SuspendReasonType {
        suspended_by_admin,wrong_password_attempts,other
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "suspend_reason_type")
    private SuspendReasonType suspendReasonType;

    @Column(name = "suspend_reason_description")
    private String suspendReasonDescription;

  /*  @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "practice_id")
    private Practice practice;
*/
    @Transient // Not persisted in DB, used as cache
    private Collection<? extends GrantedAuthority> authorities;

  /*  @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = userRoles.stream()
                    .flatMap(userRole -> userRole.getRole().getRolePermissions().stream())
                    .map(rolePermission -> new SimpleGrantedAuthority(rolePermission.getPermission().getCodeName()))
                    .collect(Collectors.toSet());
        }
        return authorities;
    }
*/
 /*   @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive && !isDeleted;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive && !isDeleted;
    }*/
}
