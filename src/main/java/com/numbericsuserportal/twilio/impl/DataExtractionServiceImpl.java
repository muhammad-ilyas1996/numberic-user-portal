package com.numbericsuserportal.twilio.impl;

import com.numbericsuserportal.twilio.dto.*;
import com.numbericsuserportal.twilio.service.DataExtractionService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service implementation for structured data extraction using rule-based regex patterns
 *
 * NOTE: This service is now primarily used as a FALLBACK for Azure Document Intelligence.
 * Azure Document Intelligence pre-built models (prebuilt-tax.us.w2, prebuilt-idDocument)
 * provide much more accurate structured extraction. This regex-based extraction is kept for:
 * 1. Fallback when Azure pre-built models fail
 * 2. 1099 forms (no pre-built model available)
 * 3. Edge cases and custom document types
 *
 * For W-2 and Government ID documents, prefer using OCRService.extractW2DataStructured()
 * and OCRService.extractGovernmentIdDataStructured() which use Azure pre-built models.
 */
@Service
public class DataExtractionServiceImpl implements DataExtractionService {

    /* ======================================================
       W-2 EXTRACTION (REGEX FALLBACK ONLY)
       ====================================================== */
    private static final Pattern TIN_PATTERN = Pattern.compile(
            "\\bTIN[\\s:]*([\\d-]+)\\b|\\b(\\d{2}-?\\d{7})\\b"
    );
    private static final Pattern STATE_PATTERN = Pattern.compile(
            "\\b([A-Z]{2})\\b|\\b(ALABAMA|ALASKA|ARIZONA|ARKANSAS|CALIFORNIA|COLORADO|CONNECTICUT|DELAWARE|FLORIDA|GEORGIA|HAWAII|IDAHO|ILLINOIS|INDIANA|IOWA|KANSAS|KENTUCKY|LOUISIANA|MAINE|MARYLAND|MASSACHUSETTS|MICHIGAN|MINNESOTA|MISSISSIPPI|MISSOURI|MONTANA|NEBRASKA|NEVADA|NEW HAMPSHIRE|NEW JERSEY|NEW MEXICO|NEW YORK|NORTH CAROLINA|NORTH DAKOTA|OHIO|OKLAHOMA|OREGON|PENNSYLVANIA|RHODE ISLAND|SOUTH CAROLINA|SOUTH DAKOTA|TENNESSEE|TEXAS|UTAH|VERMONT|VIRGINIA|WASHINGTON|WEST VIRGINIA|WISCONSIN|WYOMING)\\b",
            Pattern.CASE_INSENSITIVE
    );
    @Override
    public W2ExtractedData extractW2Data(String extractedText) {

        // ⚠️ Azure already handled structured extraction
        // This method is ONLY fallback

        if (extractedText == null || extractedText.isBlank()) {
            return createEmptyW2Data();
        }

        W2ExtractedData data = new W2ExtractedData();

        data.setEmployeeName(extractEmployeeName(extractedText));
        data.setEmployeeNameConfidence(conf(data.getEmployeeName()));

        data.setSsn(extractSSN(extractedText));
        data.setSsnConfidence(conf(data.getSsn()));

        data.setAddress(extractEmployeeAddress(extractedText));
        data.setAddressConfidence(conf(data.getAddress()));

        data.setEmployerName(extractEmployerName(extractedText));
        data.setEmployerNameConfidence(conf(data.getEmployerName()));

        data.setEmployerAddress(extractEmployerAddress(extractedText));
        data.setEmployerAddressConfidence(conf(data.getEmployerAddress()));

        data.setControlNumber(null);
        data.setControlNumberConfidence(0.0);

        data.setEin(extractEIN(extractedText));
        data.setEinConfidence(conf(data.getEin()));

        data.setTaxYear(extractTaxYear(extractedText));
        data.setTaxYearConfidence(conf(data.getTaxYear()));

        data.setBoxValues(extractBasicBoxes(extractedText));

        data.setOverallConfidence(calculateOverallConfidence(data));
        return data;
    }

    /* ======================================================
       1099 — PRIMARY REGEX
       ====================================================== */

    @Override
    public Form1099ExtractedData extract1099Data(String extractedText, String formType) {
        // ✅ Tumhara existing 1099 logic OK hai
        if (extractedText == null || extractedText.trim().isEmpty()) {
            return createEmpty1099Data(formType);
        }

        Form1099ExtractedData data = new Form1099ExtractedData();
        data.setFormType(formType);
        data.setFormTypeConfidence(0.9); // High confidence if we got here

        Map<String, Form1099ExtractedData.BoxValue> boxValues = new HashMap<>();

        // Extract recipient name
        String recipientName = extractField(extractedText,
                Pattern.compile("(?:RECIPIENT'?S?\\s+NAME|PAYEE'?S?\\s+NAME|NAME)[\\s:]*([A-Z][A-Z\\s,.-]+)", Pattern.CASE_INSENSITIVE),
                "Recipient name");
        data.setRecipientName(recipientName);
        data.setRecipientNameConfidence(calculateConfidence(recipientName));

        // Extract TIN
        String tin = extractTIN(extractedText);
        data.setTin(tin);
        data.setTinConfidence(calculateConfidence(tin));

        // Extract address
        String address = extractField(extractedText,
                Pattern.compile("(?:RECIPIENT'?S?\\s+ADDRESS|ADDRESS)[\\s:]*([A-Z0-9\\s,.-]+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE),
                "Address");
        data.setAddress(address);
        data.setAddressConfidence(calculateConfidence(address));

        // Extract payer name
        String payerName = extractField(extractedText,
                Pattern.compile("(?:PAYER'?S?\\s+NAME|PAYER)[\\s:]*([A-Z][A-Z\\s,.-]+)", Pattern.CASE_INSENSITIVE),
                "Payer name");
        data.setPayerName(payerName);
        data.setPayerNameConfidence(calculateConfidence(payerName));

        // Extract payer EIN
        String payerEin = extractEIN(extractedText);
        data.setPayerEin(payerEin);
        data.setPayerEinConfidence(calculateConfidence(payerEin));

        // Extract tax year
        String taxYear = extractTaxYear(extractedText);
        data.setTaxYear(taxYear);
        data.setTaxYearConfidence(calculateConfidence(taxYear));

        // Extract box values (common boxes: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, etc.)
        // Extract boxes 1-10 for both 1099-NEC and 1099-MISC
        for (int i = 1; i <= 10; i++) {
            String boxValue = extractBoxValue(extractedText, String.valueOf(i));
            if (boxValue != null && !boxValue.isEmpty()) {
                boxValues.put(String.valueOf(i), new Form1099ExtractedData.BoxValue(boxValue, calculateConfidence(boxValue)));
            }
        }
        data.setBoxValues(boxValues);

        // Calculate overall confidence
        data.setOverallConfidence(calculateOverallConfidence1099(data));

        return data;
    }

    /* ======================================================
       GOVERNMENT ID — FALLBACK ONLY
       ====================================================== */

    @Override
    public GovernmentIdExtractedData extractGovernmentIdData(String extractedText) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            return createEmptyGovernmentIdData();
        }

        GovernmentIdExtractedData data = new GovernmentIdExtractedData();

        // Extract full name (usually the largest name field)
        String fullName = extractField(extractedText,
                Pattern.compile("(?:NAME|FULL\\s+NAME)[\\s:]*([A-Z][A-Z\\s,.-]+)", Pattern.CASE_INSENSITIVE),
                "Full name");
        if (fullName == null || fullName.isEmpty()) {
            // Try to find name pattern (First Last or Last, First)
            fullName = extractField(extractedText,
                    Pattern.compile("\\b([A-Z][a-z]+\\s+[A-Z][a-z]+)\\b"),
                    "Full name");
        }
        data.setFullName(fullName);
        data.setFullNameConfidence(calculateConfidence(fullName));

        // Extract date of birth
        String dob = extractField(extractedText,
                Pattern.compile("(?:DOB|DATE\\s+OF\\s+BIRTH|BIRTH\\s+DATE)[\\s:]*([\\d/-]+)", Pattern.CASE_INSENSITIVE),
                "Date of birth");
        if (dob == null || dob.isEmpty()) {
            // Try generic date pattern near "DOB" or "BIRTH"
            Matcher dobMatcher = Pattern.compile("(?:DOB|BIRTH)[\\s:]*([\\d/-]+)", Pattern.CASE_INSENSITIVE).matcher(extractedText);
            if (dobMatcher.find()) {
                dob = dobMatcher.group(1);
            }
        }
        data.setDateOfBirth(dob);
        data.setDateOfBirthConfidence(calculateConfidence(dob));

        // Extract ID number (usually "DLN" or "ID NO" or "LICENSE NO")
        String idNumber = extractField(extractedText,
                Pattern.compile("(?:DLN|ID\\s+NO|LICENSE\\s+NO|LIC\\s+NO|ID\\s+NUMBER)[\\s:]*([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE),
                "ID number");
        data.setIdNumber(idNumber);
        data.setIdNumberConfidence(calculateConfidence(idNumber));

        // Extract issuing state
        String issuingState = extractState(extractedText);
        data.setIssuingState(issuingState);
        data.setIssuingStateConfidence(calculateConfidence(issuingState));

        // Extract issue date
        String issueDate = extractField(extractedText,
                Pattern.compile("(?:ISSUE\\s+DATE|ISSUED|ISS)[\\s:]*([\\d/-]+)", Pattern.CASE_INSENSITIVE),
                "Issue date");
        data.setIssueDate(issueDate);
        data.setIssueDateConfidence(calculateConfidence(issueDate));

        // Extract expiration date
        String expirationDate = extractField(extractedText,
                Pattern.compile("(?:EXP|EXPIRATION|EXPIRES|EXP\\s+DATE)[\\s:]*([\\d/-]+)", Pattern.CASE_INSENSITIVE),
                "Expiration date");
        data.setExpirationDate(expirationDate);
        data.setExpirationDateConfidence(calculateConfidence(expirationDate));

        // Determine ID type
        String idType = "UNKNOWN";
        if (extractedText.toUpperCase().contains("DRIVER") || extractedText.toUpperCase().contains("DRIVING")) {
            idType = "Driver License";
        } else if (extractedText.toUpperCase().contains("STATE ID") || extractedText.toUpperCase().contains("IDENTIFICATION")) {
            idType = "State ID";
        }
        data.setIdType(idType);
        data.setIdTypeConfidence(idType.equals("UNKNOWN") ? 0.0 : 0.8);

        // Calculate overall confidence
        data.setOverallConfidence(calculateOverallConfidenceGovernmentId(data));

        return data;
    }


    /* ======================================================
       HELPERS (MINIMAL)
       ====================================================== */

    private String extractEmployeeName(String text) {
        String first = match(text, "Employee.?s first name and initial[\\s\\n]+([A-Z]+)");
        String last  = match(text, "Last name[\\s\\n]+([A-Z]+)");

        if (first != null && last != null) {
            return first + " " + last;
        }
        return first != null ? first : last;
    }

    private String extractEmployerName(String text) {
        return match(text, "(Employer.?s name.*?\\n)([A-Z ]+)");
    }

    private String extractEmployeeAddress(String text) {

        Matcher m = Pattern.compile(
                "Employee.?s first name and initial[\\s\\S]{0,200}?" +     // name + noise
                        "(\\d+\\s+[A-Z0-9 .]+)\\n" +                              // street
                        "(?:Last name[\\s\\S]{0,40}?Suff\\.\\n)?" +               // OPTIONAL noise
                        "([A-Z ]+\\s+[A-Z]{2}\\s+\\d{5})",                        // city state zip
                Pattern.CASE_INSENSITIVE
        ).matcher(text);

        if (m.find()) {
            return normalizeAddress(m.group(1) + ", " + m.group(2));
        }
        return null;
    }

    private String normalizeAddress(String addr) {
        return addr
                .replaceAll("\\n+", ", ")
                .replaceAll("\\b(Last name|Suff\\.|Employee|Employer).*", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private String extractEmployerAddress(String text) {
        Matcher m = Pattern.compile(
                "c Employer.?s name, address, and ZIP code[\\s\\n]+" +
                        "[A-Z0-9 .,&]+[\\s\\n]+" +               // employer name
                        "(\\d+\\s+[A-Z0-9 .]+)\\n" +             // street
                        "([A-Z ]+\\s+[A-Z]{2}\\s+\\d{5})",       // city state zip
                Pattern.CASE_INSENSITIVE
        ).matcher(text);

        if (m.find()) {
            return normalizeAddress(m.group(1) + ", " + m.group(2));
        }
        return null;
    }

    private String extractSSN(String text) {
        return match(text, "(XXX-XX-\\d{4})");
    }

    private String extractEIN(String text) {
        return match(text, "(\\d{2}-\\d{7})");
    }

    private String extractTaxYear(String text) {
        return match(text, "(20\\d{2})");
    }

    private Map<String, W2ExtractedData.BoxValue> extractBasicBoxes(String text) {
        Map<String, W2ExtractedData.BoxValue> boxes = new HashMap<>();
        extractBox(text, "1", boxes);
        extractBox(text, "3", boxes);
        extractBox(text, "4", boxes);
        extractBox(text, "5", boxes);
        extractBox(text, "6", boxes); // ✅ ADD THIS
        return boxes;
    }

    private void extractBox(String text, String box, Map<String, W2ExtractedData.BoxValue> boxes) {
        String val = match(text, "\\b" + box + "\\s+[A-Za-z ]+\\s+(\\d+[.]?\\d{0,2})");
        if (val != null) {
            boxes.put(box, new W2ExtractedData.BoxValue(val, 0.9));
        }
    }

    private String match(String text, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
        return m.find() ? m.group(m.groupCount()) : null;
    }

    private double conf(String v) {
        if (v == null || v.isBlank()) return 0.0;
        if (v.matches(".*\\b(State|Employer)\\b.*")) return 0.0;
        return 0.85;
    }
    private Form1099ExtractedData createEmpty1099Data(String formType) {
        Form1099ExtractedData data = new Form1099ExtractedData();
        data.setFormType(formType);
        data.setBoxValues(new HashMap<>());
        data.setOverallConfidence(0.0);
        return data;
    }
    private W2ExtractedData createEmptyW2Data() {
        W2ExtractedData data = new W2ExtractedData();
        data.setBoxValues(new HashMap<>());
        data.setOverallConfidence(0.0);
        return data;
    }
    private Double calculateOverallConfidence(W2ExtractedData data) {
        List<Double> confidences = new ArrayList<>();
        if (data.getEmployeeNameConfidence() != null) confidences.add(data.getEmployeeNameConfidence());
        if (data.getSsnConfidence() != null) confidences.add(data.getSsnConfidence());
        if (data.getAddressConfidence() != null) confidences.add(data.getAddressConfidence());
        if (data.getEmployerNameConfidence() != null) confidences.add(data.getEmployerNameConfidence());
        if (data.getEinConfidence() != null) confidences.add(data.getEinConfidence());
        if (data.getTaxYearConfidence() != null) confidences.add(data.getTaxYearConfidence());

        if (confidences.isEmpty()) return 0.0;
        return confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private Double calculateOverallConfidence1099(Form1099ExtractedData data) {
        List<Double> confidences = new ArrayList<>();
        if (data.getRecipientNameConfidence() != null) confidences.add(data.getRecipientNameConfidence());
        if (data.getTinConfidence() != null) confidences.add(data.getTinConfidence());
        if (data.getAddressConfidence() != null) confidences.add(data.getAddressConfidence());
        if (data.getPayerNameConfidence() != null) confidences.add(data.getPayerNameConfidence());
        if (data.getPayerEinConfidence() != null) confidences.add(data.getPayerEinConfidence());
        if (data.getTaxYearConfidence() != null) confidences.add(data.getTaxYearConfidence());

        if (confidences.isEmpty()) return 0.0;
        return confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private Double calculateOverallConfidenceGovernmentId(GovernmentIdExtractedData data) {
        List<Double> confidences = new ArrayList<>();
        if (data.getFullNameConfidence() != null) confidences.add(data.getFullNameConfidence());
        if (data.getDateOfBirthConfidence() != null) confidences.add(data.getDateOfBirthConfidence());
        if (data.getIdNumberConfidence() != null) confidences.add(data.getIdNumberConfidence());
        if (data.getIssuingStateConfidence() != null) confidences.add(data.getIssuingStateConfidence());
        if (data.getIssueDateConfidence() != null) confidences.add(data.getIssueDateConfidence());
        if (data.getExpirationDateConfidence() != null) confidences.add(data.getExpirationDateConfidence());

        if (confidences.isEmpty()) return 0.0;
        return confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    private String extractField(String text, Pattern pattern, String fieldName) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1);
            if (value != null) {
                value = value.trim();
                System.out.println("Extracted " + fieldName + ": " + value);
                return value;
            }
        }
        return null;
    }
    private Double calculateConfidence(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        // Basic confidence: longer values with alphanumeric content = higher confidence
        int length = value.length();
        int alphanumericCount = 0;
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                alphanumericCount++;
            }
        }
        double ratio = length > 0 ? (double) alphanumericCount / length : 0.0;
        return Math.min(1.0, ratio * 0.8 + (length > 5 ? 0.2 : 0.0));
    }
    private String extractTIN(String text) {
        Matcher matcher = TIN_PATTERN.matcher(text);
        if (matcher.find()) {
            String tin = matcher.group(1);
            if (tin == null) {
                tin = matcher.group(2);
            }
            return tin != null ? tin : null;
        }
        return null;
    }
    private String extractState(String text) {
        Matcher matcher = STATE_PATTERN.matcher(text);
        if (matcher.find()) {
            String state = matcher.group(1);
            if (state == null) {
                state = matcher.group(2);
            }
            return state != null ? state.toUpperCase() : null;
        }
        return null;
    }
    private GovernmentIdExtractedData createEmptyGovernmentIdData() {
        GovernmentIdExtractedData data = new GovernmentIdExtractedData();
        data.setOverallConfidence(0.0);
        return data;
    }
    private String extractBoxValue(String text, String boxNumber) {
        // Special handling for Box 1 - value appears after EIN on next line
        // Format: "1 Wages, tips, other compensation | 2 Federal income tax withheld\n92-0442194 18000.00"
        // Handle fragmented: "18000.\n00" -> "18000.00"
        if ("1".equals(boxNumber)) {
            // Pattern: Find "1 Wages, tips, other compensation" then look for EIN followed by amount
            // Handle fragmented numbers across newlines
            Pattern box1Pattern = Pattern.compile(
                    "\\b1\\s+Wages,?\\s+tips,?\\s+other\\s+compensation[\\s|\\n\\d\\sA-Za-z]{0,200}?[\\d]{2}-[\\d]{7}[\\s\\n]{0,20}?([\\d]{4,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box1Matcher = box1Pattern.matcher(text);
            if (box1Matcher.find()) {
                String wholePart = box1Matcher.group(1).trim();
                // Look for decimal part after the whole number (might be on next line)
                int wholeEnd = box1Matcher.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00"; // Default to .00 if decimal not found
            }
            // Fallback: Look for number after EIN (handle fragmented)
            Pattern box1Fallback = Pattern.compile(
                    "[\\d]{2}-[\\d]{7}[\\s\\n]{0,50}?([\\d]{4,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box1FallbackMatcher = box1Fallback.matcher(text);
            if (box1FallbackMatcher.find()) {
                String wholePart = box1FallbackMatcher.group(1).trim();
                int wholeEnd = box1FallbackMatcher.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
        }

        // Pattern 1: Box number followed by description and then value on same line
        // Example: "3 Social security wages 18000.00"
        // But NOT for Box 1, 5 or 6 which have special handling
        if (!"1".equals(boxNumber) && !"5".equals(boxNumber) && !"6".equals(boxNumber)) {
            Pattern boxPattern1 = Pattern.compile(
                    "\\b" + boxNumber + "\\s+[A-Za-z\\s]{5,60}?\\s+([\\d]{4,6}[\\d,.]*\\d{0,2})(?!\\s+[\\d]\\s+[A-Za-z])(?!\\s+[\\d]{1,2}\\s+[A-Za-z])",
                    Pattern.CASE_INSENSITIVE
            );
            Matcher matcher1 = boxPattern1.matcher(text);
            if (matcher1.find()) {
                String value = matcher1.group(1).trim();
                // Validate: should be at least 4 digits for amounts (avoid single digits like "4" or "6")
                if (value.length() >= 4) {
                    return value;
                }
            }
        }

        // Special handling for Box 3: "3 Social security wages [amount]"
        // Format: "3 Social security wages 4 Social security tax withheld\n[employer name] [amount1]\n[amount2]"
        // Handle fragmented: "[amount].\n[decimal]" -> "[amount].[decimal]"
        if ("3".equals(boxNumber)) {
            // Try same line first
            Pattern box3Pattern1 = Pattern.compile(
                    "\\b3\\s+Social\\s+security\\s+wages\\s+([\\d]{4,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})(?!\\s+[\\d]\\s)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box3Matcher1 = box3Pattern1.matcher(text);
            if (box3Matcher1.find()) {
                String wholePart = box3Matcher1.group(1).trim();
                int wholeEnd = box3Matcher1.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
            // Try next line - "3 Social security wages" then find first number after employer name (generic)
            Pattern box3Pattern2 = Pattern.compile(
                    "\\b3\\s+Social\\s+security\\s+wages[\\s\\n\\d\\sA-Za-z=]{0,200}?[A-Z\\s]{5,80}?[\\s\\n]{0,50}?([\\d]{4,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box3Matcher2 = box3Pattern2.matcher(text);
            if (box3Matcher2.find()) {
                String wholePart = box3Matcher2.group(1).trim();
                int wholeEnd = box3Matcher2.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
        }

        // Special handling for Box 5: "5 Medicare wages and tips 6 Medicare tax withheld\n[amount1] [amount2]"
        // Format: Generic - "5 Medicare wages and tips 6 Medicare tax withheld\n[amount1] [amount2]"
        // Box 5 value is the first number (amount1), Box 6 is the second number (amount2)
        // Numbers can be on same line separated by spaces
        if ("5".equals(boxNumber)) {
            // Pattern 1: Look for "5 Medicare wages and tips 6 Medicare tax withheld" then find first number (two numbers on same line)
            Pattern box5Pattern = Pattern.compile(
                    "\\b5\\s+Medicare\\s+wages\\s+and\\s+tips[\\s\\n]{0,50}?\\b6\\s+Medicare\\s+tax\\s+withheld[\\s\\n]{1,100}?([\\d]{4,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})[\\s]{1,20}?[\\d]{3,6}(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box5Matcher = box5Pattern.matcher(text);
            if (box5Matcher.find()) {
                String wholePart = box5Matcher.group(1).trim();
                int wholeEnd = box5Matcher.end(1);
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
            // Pattern 2: Look for "5 Medicare wages and tips" then find first number
            Pattern box5Pattern2 = Pattern.compile(
                    "\\b5\\s+Medicare\\s+wages\\s+and\\s+tips[\\s\\n\\d\\sA-Za-z]{0,200}?([\\d]{4,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2}|[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box5Matcher2 = box5Pattern2.matcher(text);
            if (box5Matcher2.find()) {
                String wholePart = box5Matcher2.group(1).trim();
                int wholeEnd = box5Matcher2.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
            // Pattern 3: Fallback - look for "5 Medicare" then find number
            Pattern box5Pattern3 = Pattern.compile(
                    "\\b5\\s+Medicare[\\s\\n\\d\\sA-Za-z]{0,300}?([\\d]{4,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2}|[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box5Matcher3 = box5Pattern3.matcher(text);
            if (box5Matcher3.find()) {
                String wholePart = box5Matcher3.group(1).trim();
                int wholeEnd = box5Matcher3.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
        }

        // Pattern 2: Box number, description, then value on next line or after other text
        // Special case for Box 4: "4 Social security tax withheld" - value is second number after employer name
        // Format: "3 Social security wages 4 Social security tax withheld\n[employer name]\n[amount1] [amount2]"
        // Box 4 is the second amount (after Box 3 amount) - numbers can be on same line separated by spaces
        if ("4".equals(boxNumber)) {
            // Pattern 1: Look for "3 Social security wages 4 Social security tax withheld" then employer name, then two numbers on same line
            Pattern box4Pattern1 = Pattern.compile(
                    "\\b3\\s+Social\\s+security\\s+wages[\\s\\n]{0,50}?\\b4\\s+Social\\s+security\\s+tax\\s+withheld[\\s\\n]{1,200}?[A-Z\\s\\n]{5,100}?[\\s\\n]{0,100}?([\\d]{4,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})[\\s]{1,20}?([\\d]{3,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box4Matcher1 = box4Pattern1.matcher(text);
            if (box4Matcher1.find()) {
                // Group 1 is Box 3, Group 2 is Box 4 - we want Group 2
                String wholePart = box4Matcher1.group(2).trim();
                int wholeEnd = box4Matcher1.end(2);
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
            // Pattern 2: Look for "4 Social security tax withheld" then find second number after employer name
            // Match: "4 Social security tax withheld" -> employer name -> first number (Box 3) -> second number (Box 4)
            Pattern box4Pattern2 = Pattern.compile(
                    "\\b4\\s+Social\\s+security\\s+tax\\s+withheld[\\s\\n]{1,200}?[A-Z\\s\\n]{5,100}?[\\s\\n]{0,100}?[\\d]{4,6}(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})[\\s\\n]{0,50}?([\\d]{3,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box4Matcher2 = box4Pattern2.matcher(text);
            if (box4Matcher2.find()) {
                String wholePart = box4Matcher2.group(1).trim();
                int wholeEnd = box4Matcher2.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
            // Pattern 3: Fallback - look for "4 Social security tax withheld" then find any number after
            Pattern box4Pattern3 = Pattern.compile(
                    "\\b4\\s+Social\\s+security\\s+tax\\s+withheld[\\s\\n]{1,300}?([\\d]{3,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box4Matcher3 = box4Pattern3.matcher(text);
            if (box4Matcher3.find()) {
                String wholePart = box4Matcher3.group(1).trim();
                int wholeEnd = box4Matcher3.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
        }

        // Special case for Box 6: "6 Medicare tax withheld\n18000.00 261.00" - need second value
        // The format is: "5 Medicare wages and tips 6 Medicare tax withheld\n[amount1] [amount2]"
        // Box 6 value is the SECOND number (261.00), not the first (18000.00)
        // Numbers can be on same line separated by spaces
        if ("6".equals(boxNumber)) {
            // Pattern 1: Look for "5 Medicare wages and tips 6 Medicare tax withheld" then find second number (two numbers on same line)
            Pattern box6Pattern = Pattern.compile(
                    "\\b5\\s+Medicare\\s+wages\\s+and\\s+tips[\\s\\n]{0,50}?\\b6\\s+Medicare\\s+tax\\s+withheld[\\s\\n]{1,200}?[\\d]{4,6}(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})[\\s]{1,20}?([\\d]{3,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box6Matcher = box6Pattern.matcher(text);
            if (box6Matcher.find()) {
                String wholePart = box6Matcher.group(1).trim();
                int wholeEnd = box6Matcher.end(1);
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
            // Pattern 2: Look for "6 Medicare tax withheld" followed by two numbers, get the second
            Pattern box6Pattern2 = Pattern.compile(
                    "\\b6\\s+Medicare\\s+tax\\s+withheld[\\s\\n]{1,200}?[\\d]{4,6}(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2}|[\\s\\n]{0,10}?\\d{2})[\\s\\n]{0,50}?([\\d]{3,6})(?:[\\s\\n]{0,10}?(?:\\.\\d{2})?|\\.[\\s\\n]{0,10}?\\d{2})",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher box6Matcher2 = box6Pattern2.matcher(text);
            if (box6Matcher2.find()) {
                String wholePart = box6Matcher2.group(1).trim();
                int wholeEnd = box6Matcher2.end();
                String afterWhole = text.substring(wholeEnd, Math.min(text.length(), wholeEnd + 20));
                Matcher decimalMatcher = Pattern.compile("(?:\\.[\\s\\n]{0,5}?)?(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(afterWhole);
                if (decimalMatcher.find()) {
                    return wholePart + "." + decimalMatcher.group(1);
                }
                return wholePart + ".00";
            }
        }

        // General pattern for other boxes
        Pattern boxPattern2 = Pattern.compile(
                "\\b" + boxNumber + "\\s+[A-Za-z\\s]{5,60}?[\\s\\n]{1,100}?([\\d]{4,6}[\\d,.]*\\d{0,2})(?!\\s*[\\d]{2}-[\\d]{7})",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher2 = boxPattern2.matcher(text);
        if (matcher2.find()) {
            String value = matcher2.group(1).trim();
            if (value.length() >= 4) {
                return value;
            }
        }

        // Pattern 3: "Box 1" format
        Pattern boxPattern3 = Pattern.compile(
                "\\bBOX\\s+" + boxNumber + "[\\s:]*([\\d]{4,6}[\\d,.]*\\d{0,2})",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher3 = boxPattern3.matcher(text);
        if (matcher3.find()) {
            return matcher3.group(1).trim();
        }

        return null;
    }

}
