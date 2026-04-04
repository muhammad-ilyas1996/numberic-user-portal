package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.DashboardSummaryDto;
import com.numbericsuserportal.usermanagement.domain.User;

public interface DashboardSummaryService {

    DashboardSummaryDto getSummary(User currentUser);
}
