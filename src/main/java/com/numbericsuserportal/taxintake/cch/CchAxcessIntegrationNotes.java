package com.numbericsuserportal.taxintake.cch;

/**
 * CCH Axcess Phase-1 is file-based (CSV/PDF) via {@code /api/tax/intake/CCH_AXCESS/...}.
 * <p>
 * Live API (OIK) requires vendor onboarding:
 * <ul>
 *   <li>Register at Wolters Kluwer CCH Axcess Open Integration</li>
 *   <li>Obtain integrator key + complete mandatory onboarding</li>
 *   <li>Then add authenticated REST client here (not implemented until key is available)</li>
 * </ul>
 * Do not block product delivery on OIK — file intake covers Ateeq's "same format, CSV or PDF" requirement.
 */
public final class CchAxcessIntegrationNotes {

    public static final String OPEN_INTEGRATION_URL =
            "https://www.wolterskluwer.com/en/solutions/cch-axcess/open-integration";

    private CchAxcessIntegrationNotes() {
    }
}
