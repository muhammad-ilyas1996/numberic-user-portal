package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.usermanagement.domain.Role;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.domain.UserRole;
import com.numbericsuserportal.usermanagement.repo.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDataScopeServiceImpl implements UserDataScopeService {

    private static final String SUPER_ADMIN_CODE = "NUMBRICS_SUPER_ADMIN";

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public UserDataScopeContext resolve(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User is required for data scope");
        }
        List<UserRole> userRoles = userRoleRepository.findActiveWithRoleByUserId(user.getUserId());
        List<String> roleCodes = userRoles.stream()
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .map(Role::getCodeName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        boolean platformWide = userRoles.stream().anyMatch(ur -> {
            Role r = ur.getRole();
            if (r == null) return false;
            if (Boolean.TRUE.equals(r.getIsSuperadmin())) return true;
            return SUPER_ADMIN_CODE.equalsIgnoreCase(r.getCodeName());
        });

        Optional<String> ownerKey = platformWide
                ? Optional.empty()
                : Optional.of(String.valueOf(user.getUserId()));

        return new UserDataScopeContext(platformWide, ownerKey, roleCodes);
    }
}
