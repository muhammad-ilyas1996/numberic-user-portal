package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.registration.entity.BusinessProfile;
import com.numbericsuserportal.registration.repo.BusinessProfileRepository;
import com.numbericsuserportal.usermanagement.domain.PortalType;
import com.numbericsuserportal.usermanagement.domain.Role;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.domain.UserRole;
import com.numbericsuserportal.usermanagement.domain.UserRoleId;
import com.numbericsuserportal.usermanagement.dto.OAuthAuthRequest;
import com.numbericsuserportal.usermanagement.dto.OAuthAuthResponse;
import com.numbericsuserportal.usermanagement.dto.OAuthProviderInfo;
import com.numbericsuserportal.usermanagement.dto.OAuthProvidersResponse;
import com.numbericsuserportal.usermanagement.jwt.JwtUtil;
import com.numbericsuserportal.usermanagement.oauth.GoogleOAuthClient;
import com.numbericsuserportal.usermanagement.oauth.LinkedInOAuthClient;
import com.numbericsuserportal.usermanagement.oauth.OAuthProvider;
import com.numbericsuserportal.usermanagement.oauth.OAuthUserProfile;
import com.numbericsuserportal.usermanagement.repo.PortalTypeRepository;
import com.numbericsuserportal.usermanagement.repo.RoleRepository;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import com.numbericsuserportal.usermanagement.repo.UserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuthAuthService {

    private final GoogleOAuthClient googleOAuthClient;
    private final LinkedInOAuthClient linkedInOAuthClient;
    private final UserRepository userRepository;
    private final PortalTypeRepository portalTypeRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.oauth.default-role:Founder}")
    private String defaultRole;

    public OAuthAuthService(GoogleOAuthClient googleOAuthClient,
                            LinkedInOAuthClient linkedInOAuthClient,
                            UserRepository userRepository,
                            PortalTypeRepository portalTypeRepository,
                            RoleRepository roleRepository,
                            UserRoleRepository userRoleRepository,
                            BusinessProfileRepository businessProfileRepository,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil) {
        this.googleOAuthClient = googleOAuthClient;
        this.linkedInOAuthClient = linkedInOAuthClient;
        this.userRepository = userRepository;
        this.portalTypeRepository = portalTypeRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public OAuthProvidersResponse getProviders() {
        return new OAuthProvidersResponse(
                new OAuthProviderInfo(googleOAuthClient.isEnabled(), nullToEmpty(googleOAuthClient.getClientId())),
                new OAuthProviderInfo(linkedInOAuthClient.isEnabled(), nullToEmpty(linkedInOAuthClient.getClientId()))
        );
    }

    @Transactional
    public OAuthAuthResponse authenticate(OAuthProvider provider, OAuthAuthRequest request) {
        OAuthUserProfile profile = resolveProfile(provider, request);
        String intent = request.getIntent() == null ? "LOGIN" : request.getIntent().trim().toUpperCase();

        Optional<User> existing = findExistingUser(provider, profile);
        if (existing.isPresent()) {
            User user = existing.get();
            assertActive(user);
            linkProviderIds(user, provider, profile.getProviderUserId());
            user.setLastLogin(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return toResponse(user, provider, false, "Signed in successfully");
        }

        if ("LOGIN".equals(intent)) {
            throw new IllegalArgumentException("No account found for this " + provider.name().toLowerCase()
                    + " account. Please register first.");
        }

        // REGISTER — create new user
        validateRegisterFields(request);
        User created = createOAuthUser(provider, profile, request);
        return toResponse(created, provider, true, "Account created successfully");
    }

    private OAuthUserProfile resolveProfile(OAuthProvider provider, OAuthAuthRequest request) {
        return switch (provider) {
            case GOOGLE -> googleOAuthClient.verify(request.getIdToken(), request.getAccessToken());
            case LINKEDIN -> linkedInOAuthClient.verify(
                    request.getAccessToken(),
                    request.getAuthorizationCode(),
                    request.getRedirectUri()
            );
        };
    }

    private Optional<User> findExistingUser(OAuthProvider provider, OAuthUserProfile profile) {
        Optional<User> byProvider = switch (provider) {
            case GOOGLE -> userRepository.findByGoogleId(profile.getProviderUserId());
            case LINKEDIN -> userRepository.findByLinkedinId(profile.getProviderUserId());
        };
        if (byProvider.isPresent()) {
            return byProvider;
        }
        return userRepository.findByEmail(profile.getEmail());
    }

    private void linkProviderIds(User user, OAuthProvider provider, String providerUserId) {
        switch (provider) {
            case GOOGLE -> {
                if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
                    user.setGoogleId(providerUserId);
                } else if (!user.getGoogleId().equals(providerUserId)) {
                    throw new IllegalArgumentException("This Google account is already linked to a different user");
                }
            }
            case LINKEDIN -> {
                if (user.getLinkedinId() == null || user.getLinkedinId().isBlank()) {
                    user.setLinkedinId(providerUserId);
                } else if (!user.getLinkedinId().equals(providerUserId)) {
                    throw new IllegalArgumentException("This LinkedIn account is already linked to a different user");
                }
            }
        }
    }

    private void validateRegisterFields(OAuthAuthRequest request) {
        if (!Boolean.TRUE.equals(request.getTermsAccepted())) {
            throw new IllegalArgumentException("You must accept the terms and conditions");
        }
        String role = request.getPreferredRole();
        if (role == null || role.isBlank()) {
            role = defaultRole;
            request.setPreferredRole(role);
        }
        String accountType = request.getAccountType();
        if (accountType == null || accountType.isBlank()) {
            throw new IllegalArgumentException("accountType is required for registration (individual or business)");
        }
        try {
            User.AccountType.valueOf(accountType.trim().toLowerCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("accountType must be individual or business");
        }
    }

    private User createOAuthUser(OAuthProvider provider, OAuthUserProfile profile, OAuthAuthRequest request) {
        if (userRepository.existsByEmail(profile.getEmail()) || userRepository.existsByUsername(profile.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists. Please sign in instead.");
        }

        PortalType portalType = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
                .orElseThrow(() -> new IllegalStateException("NUMBRICS Portal not found"));

        User.AccountType accountType = User.AccountType.valueOf(request.getAccountType().trim().toLowerCase());

        User user = new User();
        user.setEmail(profile.getEmail());
        user.setUsername(profile.getEmail());
        // Unusable random password — SSO users sign in via provider
        user.setPassword(passwordEncoder.encode("OAUTH|" + UUID.randomUUID()));
        user.setAuthProvider(provider.name());
        if (provider == OAuthProvider.GOOGLE) {
            user.setGoogleId(profile.getProviderUserId());
        } else {
            user.setLinkedinId(profile.getProviderUserId());
        }
        user.setFirstName(truncate(profile.getFirstName(), 50));
        user.setLastName(truncate(profile.getLastName(), 50));
        user.setPhone(request.getPhone());
        user.setAccountType(accountType);
        user.setTermsAccepted(true);
        user.setMarketingOptIn(Boolean.TRUE.equals(request.getMarketingOptIn()));
        user.setRegistrationStatus(User.RegistrationStatus.STEP1_COMPLETED);
        user.setIsActive(true);
        user.setIsDeleted(false);
        user.setIsForceChangePassword(false);
        user.setPortalType(portalType);
        user.setUserType(User.UserType.practice);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        User saved = userRepository.save(user);

        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()
                && accountType == User.AccountType.business) {
            BusinessProfile bp = new BusinessProfile();
            bp.setUserId(saved.getUserId());
            bp.setCompanyName(request.getCompanyName().trim());
            businessProfileRepository.save(bp);
        }

        assignRole(saved, request.getPreferredRole());
        return saved;
    }

    private void assignRole(User user, String roleCode) {
        String mapped = mapRoleCodeToDatabase(roleCode == null || roleCode.isBlank() ? defaultRole : roleCode.trim());
        Role role = roleRepository.findByCodeName(mapped)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Role '" + roleCode + "' not found. Available: Founder, Business_Owner, Accountant_Pro"));

        UserRole userRole = new UserRole();
        UserRoleId id = new UserRoleId();
        id.setUserId(user.getUserId());
        id.setRoleId(role.getRoleId());
        userRole.setId(id);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setIsActive(true);
        userRole.setCreatedAt(LocalDateTime.now());
        userRole.setAddedBy(user.getUserId());
        userRoleRepository.save(userRole);
    }

    private String mapRoleCodeToDatabase(String roleCode) {
        return switch (roleCode) {
            case "Founder" -> "NUMBRICS_FOUNDER";
            case "Business_Owner" -> "NUMBRICS_BUSINESS_OWNER";
            case "Accountant_Pro" -> "NUMBRICS_ACCOUNTANT_PRO";
            case "NUMBRICS_FOUNDER", "NUMBRICS_BUSINESS_OWNER", "NUMBRICS_ACCOUNTANT_PRO" -> roleCode;
            default -> roleCode;
        };
    }

    private void assertActive(User user) {
        if (Boolean.TRUE.equals(user.getIsDeleted()) || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("User inactive or deleted");
        }
    }

    private OAuthAuthResponse toResponse(User user, OAuthProvider provider, boolean isNew, String message) {
        String token = jwtUtil.generateToken(user);
        return OAuthAuthResponse.builder()
                .success(true)
                .message(message)
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .provider(provider.name())
                .newUser(isNew)
                .registrationStatus(user.getRegistrationStatus() != null ? user.getRegistrationStatus().name() : null)
                .accountType(user.getAccountType() != null ? user.getAccountType().name() : null)
                .build();
    }

    private static String truncate(String s, int max) {
        if (s == null || s.isBlank()) return "User";
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
