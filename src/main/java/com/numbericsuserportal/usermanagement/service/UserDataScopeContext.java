package com.numbericsuserportal.usermanagement.service;

import java.util.List;
import java.util.Optional;

/**
 * Resolved once per request: super-admin flag, optional {@code created_by} filter key, role codes for UI/API.
 */
public final class UserDataScopeContext {

    private final boolean platformWideDataAccess;
    /** Empty = no {@code created_by} filter; present = restrict to this value (userId string). */
    private final Optional<String> ownerCreatedByKey;
    private final List<String> roleCodesSorted;

    public UserDataScopeContext(boolean platformWideDataAccess,
                                Optional<String> ownerCreatedByKey,
                                List<String> roleCodesSorted) {
        this.platformWideDataAccess = platformWideDataAccess;
        this.ownerCreatedByKey = ownerCreatedByKey;
        this.roleCodesSorted = roleCodesSorted != null
                ? List.copyOf(roleCodesSorted)
                : List.of();
    }

    public boolean isPlatformWideDataAccess() {
        return platformWideDataAccess;
    }

    public Optional<String> ownerCreatedByKey() {
        return ownerCreatedByKey;
    }

    public List<String> getRoleCodesSorted() {
        return roleCodesSorted;
    }
}
