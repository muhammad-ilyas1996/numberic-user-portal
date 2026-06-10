package com.numbericsuserportal.LlcNorthwest.LLCFormation;

import java.util.List;

/**
 * Corporate Tools entity_type values for LLC formation.
 * POST /companies expects display names (e.g. {@link #LLC_DISPLAY}), not {@code llc}.
 *
 * @see <a href="https://docs.corporatetools.com">Corporate Tools API</a>
 */
public final class NorthwestEntityTypes {

    /** Primary value for POST /companies and GET /filing-products. */
    public static final String LLC_DISPLAY = "Limited Liability Company";

    /** Try in order when creating a company until NW accepts one. */
    public static final List<String> COMPANY_CREATE_CANDIDATES = List.of(
            LLC_DISPLAY,
            "limited_liability_company",
            "llc"
    );

    /** Try in order when resolving filing products for a jurisdiction. */
    public static final List<String> FILING_PRODUCT_CANDIDATES = List.of(
            LLC_DISPLAY,
            "limited_liability_company",
            "llc"
    );

    private NorthwestEntityTypes() {
    }
}
