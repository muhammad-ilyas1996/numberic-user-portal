package com.numbericsuserportal.taxintake.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Tax software connectors supported by intake (CSV / PDF OCR).
 * CCH Axcess also has a future live API path (vendor OIK) — Phase-1 is file-based like the rest.
 */
public enum TaxSoftwareType {
    PROCONNECT(
            "ProConnect",
            "Intuit ProConnect Tax",
            true,
            true,
            "FILE_BASED",
            "READY",
            "CSV/Excel sheet + PDF/image OCR. Official spreadsheet templates also exist in ProConnect.",
            List.of("client_id", "first_name", "last_name", "ssn", "dob", "filing_status",
                    "email", "phone", "address", "city", "state", "zip",
                    "w2_employer", "w2_wages", "w2_federal_withholding",
                    "form1099_type", "form1099_payer", "form1099_amount")),

    DRAKE(
            "Drake Tax",
            "Drake Tax",
            true,
            true,
            "FILE_BASED",
            "READY",
            "CSV + PDF documented built-in features. No vendor permission required.",
            List.of("client_id", "first_name", "last_name", "ssn", "dob", "filing_status",
                    "email", "phone", "address", "city", "state", "zip",
                    "w2_employer", "w2_wages", "w2_federal_withholding",
                    "form1099_type", "form1099_payer", "form1099_amount")),

    PROSERIES(
            "ProSeries",
            "Intuit ProSeries",
            true,
            true,
            "FILE_BASED",
            "READY",
            "CSV export from Client Manager + PDF. No public API partnership required for file intake.",
            List.of("client_id", "first_name", "last_name", "ssn", "dob", "filing_status",
                    "email", "phone", "address", "city", "state", "zip",
                    "w2_employer", "w2_wages", "w2_federal_withholding",
                    "form1099_type", "form1099_payer", "form1099_amount")),

    TAXWISE(
            "TaxWise",
            "TaxWise",
            true,
            true,
            "FILE_BASED",
            "READY",
            "CSV + PDF intake. Lower priority after Drake/ProConnect in product roadmap.",
            List.of("client_id", "first_name", "last_name", "ssn", "dob", "filing_status",
                    "email", "phone", "address", "city", "state", "zip",
                    "w2_employer", "w2_wages", "w2_federal_withholding",
                    "form1099_type", "form1099_payer", "form1099_amount")),

    CCH_AXCESS(
            "CCH Axcess",
            "Wolters Kluwer CCH Axcess",
            true,
            true,
            "FILE_BASED",
            "PENDING_VENDOR_KEY",
            "Phase-1: CSV/PDF file intake (same pattern). Live OIK API needs vendor registration at "
                    + "wolterskluwer.com/cch-axcess/open-integration — wire later when integrator key is available.",
            List.of("Client ID", "Client Sub-ID", "NameLine1", "NameLine2", "SortName",
                    "Federal ID", "ClientType", "email", "phone",
                    "address", "city", "state", "zip",
                    "w2_employer", "w2_wages", "form1099_type", "form1099_amount")),

    LACERTE(
            "Lacerte",
            "Intuit Lacerte",
            true,
            true,
            "FILE_BASED",
            "PHASE_3",
            "Phase-3 platform. PDF safer than RPA; CSV also accepted for intake storage.",
            List.of("client_id", "first_name", "last_name", "ssn", "dob", "filing_status",
                    "email", "phone", "address", "city", "state", "zip",
                    "w2_employer", "w2_wages", "w2_federal_withholding",
                    "form1099_type", "form1099_payer", "form1099_amount"));

    private final String displayName;
    private final String vendorName;
    private final boolean supportsCsv;
    private final boolean supportsPdf;
    private final String integrationMode;
    private final String apiStatus;
    private final String notes;
    private final List<String> templateHeaders;

    TaxSoftwareType(String displayName, String vendorName, boolean supportsCsv, boolean supportsPdf,
                    String integrationMode, String apiStatus, String notes, List<String> templateHeaders) {
        this.displayName = displayName;
        this.vendorName = vendorName;
        this.supportsCsv = supportsCsv;
        this.supportsPdf = supportsPdf;
        this.integrationMode = integrationMode;
        this.apiStatus = apiStatus;
        this.notes = notes;
        this.templateHeaders = templateHeaders;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVendorName() {
        return vendorName;
    }

    public boolean isSupportsCsv() {
        return supportsCsv;
    }

    public boolean isSupportsPdf() {
        return supportsPdf;
    }

    public String getIntegrationMode() {
        return integrationMode;
    }

    public String getApiStatus() {
        return apiStatus;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getTemplateHeaders() {
        return templateHeaders;
    }

    public static TaxSoftwareType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("software code is required");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return Arrays.stream(values())
                .filter(s -> s.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown software: " + code + ". Use one of: "
                                + Arrays.stream(values()).map(Enum::name).reduce((a, b) -> a + ", " + b).orElse("")));
    }
}
