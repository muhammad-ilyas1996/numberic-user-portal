package com.numbericsuserportal.taxbandit.common.util;

import java.util.regex.Pattern;

/**
 * Utility class for common form validations.
 * Provides reusable validation methods for TaxBandits forms.
 */
public class FormValidationUtil {
    
    // SSN pattern: XXX-XX-XXXX
    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
    
    // EIN pattern: XX-XXXXXXX
    private static final Pattern EIN_PATTERN = Pattern.compile("^\\d{2}-\\d{7}$");
    
    // ITIN pattern: 9XX-XX-XXXX
    private static final Pattern ITIN_PATTERN = Pattern.compile("^9\\d{2}-\\d{2}-\\d{4}$");
    
    // Date pattern: MM/DD/YYYY or MM-DD-YYYY
    private static final Pattern DATE_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])[/-](0[1-9]|[12]\\d|3[01])[/-]\\d{4}$");
    
    /**
     * Validate SSN format (XXX-XX-XXXX)
     * 
     * @param ssn The SSN to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSSN(String ssn) {
        if (ssn == null || ssn.trim().isEmpty()) {
            return false;
        }
        return SSN_PATTERN.matcher(ssn.trim()).matches();
    }
    
    /**
     * Validate EIN format (XX-XXXXXXX)
     * 
     * @param ein The EIN to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEIN(String ein) {
        if (ein == null || ein.trim().isEmpty()) {
            return false;
        }
        return EIN_PATTERN.matcher(ein.trim()).matches();
    }
    
    /**
     * Validate ITIN format (9XX-XX-XXXX)
     * 
     * @param itin The ITIN to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidITIN(String itin) {
        if (itin == null || itin.trim().isEmpty()) {
            return false;
        }
        return ITIN_PATTERN.matcher(itin.trim()).matches();
    }
    
    /**
     * Validate TIN (SSN, EIN, ITIN, ATIN, or NA)
     * 
     * @param tin The TIN to validate
     * @param tinType The type of TIN (SSN, EIN, ITIN, ATIN, NA)
     * @return true if valid, false otherwise
     */
    public static boolean isValidTIN(String tin, String tinType) {
        if (tin == null || tinType == null) {
            return false;
        }
        
        switch (tinType.toUpperCase()) {
            case "SSN":
                return isValidSSN(tin);
            case "EIN":
                return isValidEIN(tin);
            case "ITIN":
                return isValidITIN(tin);
            case "ATIN":
                return isValidSSN(tin); // ATIN uses same format as SSN
            case "NA":
                return true; // Not Applicable
            default:
                return false;
        }
    }
    
    /**
     * Validate date format (MM/DD/YYYY or MM-DD-YYYY)
     * 
     * @param date The date to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        return DATE_PATTERN.matcher(date.trim()).matches();
    }
    
    /**
     * Validate tax year (4 digits, typically 2020-2099)
     * 
     * @param taxYear The tax year to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTaxYear(String taxYear) {
        if (taxYear == null || taxYear.trim().isEmpty()) {
            return false;
        }
        try {
            int year = Integer.parseInt(taxYear.trim());
            return year >= 2020 && year <= 2099;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}



