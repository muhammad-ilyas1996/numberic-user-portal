package com.numbericsuserportal.invoice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard KPIs: scoped to all users for super admin, otherwise current user's invoices only.
 */
@Data
public class DashboardSummaryDto {

    /** Sum of successful NMI invoice payments (payment_transaction). */
    private Double totalRevenueNmi;

    /** Count of active invoices (invoice_tax). */
    private Long totalInvoices;

    /** Active role code names, e.g. NUMBRICS_SUPER_ADMIN, NUMBRICS_BUSINESS_OWNER. */
    private List<String> roleCodes = new ArrayList<>();

    /** Convenience: super-admin role if present, else first role code. */
    private String primaryRoleCode;

    /** ALL = platform-wide aggregates; USER = current user's invoices / their NMI revenue only. */
    private String dataScope;
}
