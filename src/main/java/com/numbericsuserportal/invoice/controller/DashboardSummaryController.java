package com.numbericsuserportal.invoice.controller;

import com.numbericsuserportal.invoice.dto.DashboardSummaryDto;
import com.numbericsuserportal.invoice.service.DashboardSummaryService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
public class DashboardSummaryController {

    @Autowired
    private DashboardSummaryService dashboardSummaryService;

    /**
     * Dashboard KPIs: NMI revenue sum, invoice count, roles.
     * Super admin / NUMBRICS_SUPER_ADMIN: ALL scope. Others: USER scope (created_by = current userId).
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(dashboardSummaryService.getSummary(currentUser));
    }
}
