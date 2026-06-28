package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

/**
 * Keeps {@code llc_formation.filing_status} within DB column limits.
 */
final class FilingStatusFormatter {

    static final int MAX_LENGTH = 250;

    private FilingStatusFormatter() {
    }

    static String failure(String code, String detail) {
        if (detail == null || detail.isBlank()) {
            return limit(code);
        }
        String normalized = detail.replaceAll("\\s+", " ").trim();
        return limit(code + ": " + normalized);
    }

    static String limit(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= MAX_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LENGTH - 3) + "...";
    }
}
