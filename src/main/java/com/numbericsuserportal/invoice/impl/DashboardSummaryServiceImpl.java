package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.invoice.dto.DashboardSummaryDto;
import com.numbericsuserportal.invoice.repo.InvoiceAndTaxRepo;
import com.numbericsuserportal.invoice.repo.PaymentTransactionRepo;
import com.numbericsuserportal.invoice.service.DashboardSummaryService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.service.UserDataScopeContext;
import com.numbericsuserportal.usermanagement.service.UserDataScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardSummaryServiceImpl implements DashboardSummaryService {

    private static final String SUPER_ADMIN_CODE = "NUMBRICS_SUPER_ADMIN";

    @Autowired
    private UserDataScopeService userDataScopeService;

    @Autowired
    private InvoiceAndTaxRepo invoiceAndTaxRepo;

    @Autowired
    private PaymentTransactionRepo paymentTransactionRepo;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        String ownerKey = String.valueOf(currentUser.getUserId());

        DashboardSummaryDto dto = new DashboardSummaryDto();
        List<String> roleCodes = scope.getRoleCodesSorted();
        dto.setRoleCodes(roleCodes);
        dto.setPrimaryRoleCode(resolvePrimaryRoleCode(roleCodes, scope.isPlatformWideDataAccess()));

        if (scope.isPlatformWideDataAccess()) {
            dto.setDataScope("ALL");
            dto.setTotalInvoices(invoiceAndTaxRepo.countByIsActiveTrue());
            dto.setTotalRevenueNmi(nullToZero(paymentTransactionRepo.sumNmiSuccessAmountAll()));
        } else {
            dto.setDataScope("USER");
            dto.setTotalInvoices(invoiceAndTaxRepo.countByCreatedByAndIsActiveTrue(ownerKey));
            dto.setTotalRevenueNmi(nullToZero(paymentTransactionRepo.sumNmiSuccessAmountForInvoiceOwner(ownerKey)));
        }

        return dto;
    }

    private static String resolvePrimaryRoleCode(List<String> roleCodes, boolean superAdmin) {
        if (roleCodes.isEmpty()) {
            return null;
        }
        if (superAdmin) {
            return roleCodes.stream()
                    .filter(c -> SUPER_ADMIN_CODE.equalsIgnoreCase(c))
                    .findFirst()
                    .orElse(roleCodes.get(0));
        }
        return roleCodes.get(0);
    }

    private static double nullToZero(Double v) {
        return v != null ? v : 0.0;
    }
}
