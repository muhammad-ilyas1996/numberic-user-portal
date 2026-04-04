package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.usermanagement.domain.User;

/**
 * Central "whose data?" resolution: super admin sees all; others scoped to {@code created_by = userId}.
 * Call {@link #resolve(User)} once per operation and use the returned context.
 */
public interface UserDataScopeService {

    UserDataScopeContext resolve(User user);
}
