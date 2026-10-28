package com.numbericsuserportal.federaltax.domain;

/**
 * Client-visible statuses only (no preparer names / internal notes).
 */
public enum ClientTaxStatus {
    // Filing route
    INTAKE,
    DOCS_PENDING,
    ORGANIZER_COMPLETE,
    WITH_PREPARER,
    IN_REVIEW,
    SIGNED,
    FILED,
    ACCEPTED,
    ACTION_REQUIRED,
    // Export route
    EXPORTED_TO_TAX_PRO
}
