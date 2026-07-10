package com.numbericsuserportal.ai.util;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared validation for Taalr automation inputs (invoice / OCR / LLC).
 */
public final class TaalrInputValidation {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9\\-\\s().]{8,20}$");
    private static final Pattern CONFUSION = Pattern.compile(
            "(?i)^(matlab\\s+mai\\s+samajh\\s+nahi|samajh\\s*(nahi|ni)|nahi\\s*samajha|"
                    + "i\\s*don'?t\\s*understand|dont\\s*understand|what\\??|huh\\??|"
                    + "idk|no\\s*idea|confused|ky[a]?\\s*matlab|explain|help\\??|"
                    + "not\\s*sure|unsure)[.!?\\s]*$");
    private static final Pattern SKIP = Pattern.compile(
            "(?i)^(skip|default|use\\s*default|later|n\\/?a|none)[.!?\\s]*$");
    private static final Pattern GUIDE = Pattern.compile(
            "(?i).*(guide\\s*me|manual\\s*on\\s*dashboard|just\\s*guide|"
                    + "don'?t\\s*automate|no\\s*automation|dashboard\\s*guide|"
                    + "explain\\s*(how|invoice|llc|receipt)).*");

    private static final Set<String> US_STATES = Set.of(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA",
            "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT",
            "VA", "WA", "WV", "WI", "WY", "DC");

    private TaalrInputValidation() {
    }

    public static boolean isConfusion(String text) {
        return text != null && CONFUSION.matcher(text.trim()).matches();
    }

    public static boolean isSkip(String text) {
        return text != null && SKIP.matcher(text.trim()).matches();
    }

    public static boolean wantsGuidanceOnly(String text) {
        return text != null && GUIDE.matcher(text.trim()).matches();
    }

    public static boolean isValidEmail(String text) {
        return text != null && EMAIL.matcher(text.trim()).matches();
    }

    public static boolean isValidPhone(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String trimmed = text.trim();
        if (!PHONE.matcher(trimmed).matches()) {
            return false;
        }
        String digits = trimmed.replaceAll("\\D", "");
        return digits.length() >= 10 && digits.length() <= 15;
    }

    public static boolean isValidRecipient(String text) {
        return isValidEmail(text) || isValidPhone(text);
    }

    public static String normalizePhone(String text) {
        if (text == null) {
            return null;
        }
        String digits = text.trim().replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "+1" + digits;
        }
        if (digits.startsWith("00")) {
            return "+" + digits.substring(2);
        }
        return text.trim().startsWith("+") ? "+" + digits : digits;
    }

    public static boolean isValidPersonName(String text) {
        if (text == null || text.isBlank() || isConfusion(text)) {
            return false;
        }
        String t = text.trim();
        if (t.length() < 2 || t.length() > 80) {
            return false;
        }
        if (isValidEmail(t) || isValidPhone(t)) {
            return false;
        }
        return t.matches("(?i)^[A-Za-z][A-Za-z .'\\-]{1,79}$");
    }

    public static boolean isValidBusinessName(String text) {
        if (text == null || text.isBlank() || isConfusion(text)) {
            return false;
        }
        String t = text.trim();
        return t.length() >= 3 && t.length() <= 120;
    }

    public static boolean isValidDescription(String text) {
        if (text == null || text.isBlank() || isConfusion(text)) {
            return false;
        }
        String t = text.trim();
        return t.length() >= 2 && t.length() <= 200;
    }

    public static Double parseAmount(String text) {
        if (text == null || text.isBlank() || isConfusion(text)) {
            return null;
        }
        String normalized = text.replaceAll("[^0-9.]", "");
        if (normalized.isBlank() || normalized.chars().filter(ch -> ch == '.').count() > 1) {
            return null;
        }
        try {
            double value = Double.parseDouble(normalized);
            return value > 0 && value < 1_000_000_000 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseDueDays(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        if (isSkip(text)) {
            return 30;
        }
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            int days = Integer.parseInt(digits);
            return days >= 1 && days <= 365 ? days : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String parseChannel(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.trim().toLowerCase(Locale.ROOT);
        if (lower.contains("email") || lower.contains("mail")) {
            return "EMAIL";
        }
        if (lower.contains("whatsapp") || lower.contains("whats app") || lower.equals("wa")
                || lower.contains("phone") || lower.contains("sms") || lower.contains("text")) {
            return "WHATSAPP";
        }
        return null;
    }

    public static String parseUsState(String text) {
        if (text == null || text.isBlank() || isConfusion(text)) {
            return null;
        }
        String trimmed = text.trim().toUpperCase(Locale.ROOT);
        if (US_STATES.contains(trimmed)) {
            return trimmed;
        }
        java.util.regex.Matcher m = Pattern.compile("\\b([A-Z]{2})\\b").matcher(trimmed);
        while (m.find()) {
            String code = m.group(1);
            if (US_STATES.contains(code)) {
                return code;
            }
        }
        return null;
    }

    public static Boolean parseYesNo(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.trim().toLowerCase(Locale.ROOT);
        if (lower.matches("^(yes|y|yeah|yep|true|1|haan|han)\\b.*")) {
            return Boolean.TRUE;
        }
        if (lower.matches("^(no|n|nope|false|0|nahin|nahi)\\b.*")) {
            return Boolean.FALSE;
        }
        return null;
    }

    public static String parseFilingSpeed(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("same") || lower.contains("sameday")) {
            return "sameday";
        }
        if (lower.contains("exped")) {
            return "expedited";
        }
        if (lower.contains("standard") || isSkip(text)) {
            return "standard";
        }
        return null;
    }

    public static boolean isValidDob(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        try {
            java.time.LocalDate dob = java.time.LocalDate.parse(text.trim());
            java.time.LocalDate now = java.time.LocalDate.now();
            return !dob.isAfter(now.minusYears(18)) && !dob.isBefore(now.minusYears(100));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidSsnLast4(String text) {
        return text != null && text.trim().matches("\\d{4}");
    }

    public static Integer parseOwnershipPct(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            int pct = Integer.parseInt(digits);
            return pct >= 1 && pct <= 100 ? pct : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String parseOwnershipType(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("multi") || lower.contains("multiple") || lower.equals("2")) {
            return "multi";
        }
        if (lower.contains("single") || lower.contains("one") || lower.equals("1")) {
            return "single";
        }
        return null;
    }

    public static Double parseQuantity(String text) {
        Double amt = parseAmount(text);
        return amt != null && amt > 0 ? amt : null;
    }

    public static String parseAgentType(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("numbrics") || lower.contains("northwest") || lower.contains("nw")
                || lower.contains("provided") || lower.equals("1")) {
            return "NUMBRICS_NW";
        }
        if (lower.contains("own") || lower.contains("myself") || lower.contains("my agent")
                || lower.equals("2")) {
            return "OWN";
        }
        return null;
    }
}
