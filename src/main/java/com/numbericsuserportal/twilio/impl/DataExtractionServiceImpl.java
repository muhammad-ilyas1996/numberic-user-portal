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


    @Override
    public Form1099ExtractedData extract1099Data(String extractedText, String formType) {

        if (extractedText == null || extractedText.trim().isEmpty()) {
            return createEmpty1099Data(formType);
        }

        Form1099ExtractedData data = new Form1099ExtractedData();
        data.setFormType(formType);
        data.setFormTypeConfidence(0.9); // High confidence if we got here

        Map<String, Form1099ExtractedData.BoxValue> boxValues = new HashMap<>();

        // ========== PAYER INFORMATION ==========
        // Payer info appears at the top of 1099 forms
        // Format: "PAYER'S name, street address..." followed by company name and address
        
        // Extract payer name - first non-header line after "PAYER'S name"
        String payerName = extract1099PayerName(extractedText);
        data.setPayerName(payerName);
        data.setPayerNameConfidence(calculateConfidence(payerName));
        
        // Extract payer address (street + city/state/zip)
        String[] payerAddress = extract1099PayerAddress(extractedText);
        data.setPayerStreetAddress(payerAddress[0]);
        data.setPayerStreetAddressConfidence(calculateConfidence(payerAddress[0]));
        data.setPayerCityStateZip(payerAddress[1]);
        data.setPayerCityStateZipConfidence(calculateConfidence(payerAddress[1]));
        
        // Extract payer TIN (appears after "PAYER'S TIN")
        String payerTin = extract1099PayerTIN(extractedText);
        data.setPayerTin(payerTin);
        data.setPayerTinConfidence(calculateConfidence(payerTin));
        // Legacy field
        data.setPayerEin(payerTin);
        data.setPayerEinConfidence(calculateConfidence(payerTin));
        
        // Extract payer phone (if present)
        String payerPhone = extract1099PayerPhone(extractedText);
        data.setPayerPhone(payerPhone);
        data.setPayerPhoneConfidence(calculateConfidence(payerPhone));

        // ========== RECIPIENT INFORMATION ==========
        
        // Extract recipient name - appears after "RECIPIENT'S name"
        String recipientName = extract1099RecipientName(extractedText);
        data.setRecipientName(recipientName);
        data.setRecipientNameConfidence(calculateConfidence(recipientName));

        // Extract recipient TIN (appears after "RECIPIENT'S TIN")
        String recipientTin = extract1099RecipientTIN(extractedText);
        data.setRecipientTin(recipientTin);
        data.setRecipientTinConfidence(calculateConfidence(recipientTin));
        // Legacy field
        data.setTin(recipientTin);
        data.setTinConfidence(calculateConfidence(recipientTin));

        // Extract recipient address
        String[] recipientAddress = extract1099RecipientAddress(extractedText);
        data.setRecipientStreetAddress(recipientAddress[0]);
        data.setRecipientStreetAddressConfidence(calculateConfidence(recipientAddress[0]));
        data.setRecipientCityStateZip(recipientAddress[1]);
        data.setRecipientCityStateZipConfidence(calculateConfidence(recipientAddress[1]));
        // Legacy field - combine both
        String fullAddress = (recipientAddress[0] != null ? recipientAddress[0] : "") + 
                            (recipientAddress[1] != null ? ", " + recipientAddress[1] : "");
        data.setAddress(fullAddress.isEmpty() ? null : fullAddress);
        data.setAddressConfidence(calculateConfidence(fullAddress.isEmpty() ? null : fullAddress));

        // ========== TAX YEAR & METADATA ==========
        
        // Extract tax year - handle "20 21" format
        String taxYear = extract1099TaxYear(extractedText);
        data.setTaxYear(taxYear);
        data.setTaxYearConfidence(calculateConfidence(taxYear));
        
        // Extract account number
        String accountNumber = extract1099AccountNumber(extractedText);
        data.setAccountNumber(accountNumber);
        data.setAccountNumberConfidence(calculateConfidence(accountNumber));
        
        // Check corrected indicator - only true if actually checked (not just "if checked" text)
        // Look for checked box pattern: [X], ☑, ✓, ✔ near CORRECTED, but NOT "if checked"
        boolean isCorrected = extractedText.matches("(?i).*\\[\\s*[X✓✔]\\s*\\]\\s*CORRECTED.*") ||
                             extractedText.matches("(?i).*CORRECTED\\s*\\[\\s*[X✓✔]\\s*\\].*") ||
                             extractedText.matches("(?i).*☑\\s*CORRECTED.*") ||
                             extractedText.matches("(?i).*CORRECTED\\s*☑.*");
        data.setCorrectedIndicator(isCorrected);
        data.setCorrectedIndicatorConfidence(isCorrected ? 0.9 : 0.8);
        
        // Check FATCA filing requirement - only for 1099-MISC (Box 13)
        boolean hasFatca = false;
        if ("1099-MISC".equals(formType)) {
            hasFatca = extractedText.matches("(?i).*FATCA.*[X✓✔☑].*") ||
                      extractedText.matches("(?i).*\\[\\s*[X✓✔]\\s*\\].*FATCA.*");
        }
        data.setFatcaFilingRequirement(hasFatca);
        data.setFatcaFilingRequirementConfidence(0.8);

        // ========== BOX VALUES ==========
        // Extract box values based on form type
        if ("1099-NEC".equals(formType)) {
            extract1099NECBoxes(extractedText, boxValues);
        } else if ("1099-MISC".equals(formType)) {
            extract1099MISCBoxes(extractedText, boxValues);
        } else {
            // Generic extraction for boxes 1-17
            for (int i = 1; i <= 17; i++) {
                String boxValue = extract1099BoxValue(extractedText, String.valueOf(i), formType);
                if (boxValue != null && !boxValue.isEmpty()) {
                    boxValues.put(String.valueOf(i), new Form1099ExtractedData.BoxValue(boxValue, calculateConfidence(boxValue)));
                }
            }
        }
        data.setBoxValues(boxValues);

        // Calculate overall confidence
        data.setOverallConfidence(calculateOverallConfidence1099(data));

        return data;
    }
    
    // ========== 1099 HELPER METHODS ==========
    
    private String extract1099PayerName(String text) {
        // Pattern: After "PAYER'S name, street address..." find the company name
        // Skip header lines (like "or foreign postal code, and telephone no.")
        // The actual company name comes after the header text
        
        // First, try to find the section between PAYER'S name and the actual data
        Matcher m = Pattern.compile(
            "PAYER'?S?\\s+name[^\\n]*(?:\\n+[^\\n]*(?:foreign|postal|telephone)[^\\n]*)?\\n+([A-Z][A-Z0-9\\s&,.'-]+?)\\n",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        ).matcher(text);
        if (m.find()) {
            String name = m.group(1).trim();
            // Skip if it looks like header text or address
            if (!name.toLowerCase().contains("or foreign") && 
                !name.toLowerCase().contains("postal code") &&
                !name.toLowerCase().contains("telephone") &&
                !name.matches(".*\\d{5}.*") && // Skip if contains zip code
                name.length() > 2) {
                return name;
            }
        }
        
        // Alternative: Look for company name pattern (all caps, no numbers at start)
        // Between PAYER section and PAYER'S TIN
        Matcher altMatcher = Pattern.compile(
            "telephone[^\\n]*\\n+([A-Z][A-Z\\s&,.'-]+?)\\n+\\d+",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        ).matcher(text);
        if (altMatcher.find()) {
            String name = altMatcher.group(1).trim();
            if (name.length() > 2 && !name.matches(".*\\d{5}.*")) {
                return name;
            }
        }
        
        return null;
    }
    
    private String[] extract1099PayerAddress(String text) {
        String[] result = new String[2]; // [street, city/state/zip]
        
        // Look for address pattern after payer name
        // Format: street address on one line, city/state/zip on next
        Matcher m = Pattern.compile(
            "PAYER'?S?\\s+name[^\\n]*\\n+[A-Z][A-Z0-9\\s&,.'-]+\\n+(\\d+[A-Z0-9\\s,.'-]+(?:ST|STREET|AVE|AVENUE|RD|ROAD|DR|DRIVE|LN|LANE|BLVD|WAY|CT|COURT|PL|PLACE)[A-Z0-9\\s,.'-]*)\\n+([A-Z]+[,\\s]+[A-Z]{2}\\s+\\d{5}(?:-\\d{4})?)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        ).matcher(text);
        if (m.find()) {
            result[0] = m.group(1).trim();
            result[1] = m.group(2).trim();
        }
        return result;
    }
    
    private String extract1099PayerTIN(String text) {
        // Pattern: "PAYER'S TIN" followed by TIN on next line
        Matcher m = Pattern.compile(
            "PAYER'?S?\\s+TIN\\s*\\n*\\s*(\\d{2}-\\d{7})",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
    
    private String extract1099PayerPhone(String text) {
        // Look for phone number pattern near payer info
        Matcher m = Pattern.compile(
            "(?:telephone|phone|tel)[\\s.:]*\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (m.find()) {
            // Extract just the number
            Matcher numMatcher = Pattern.compile("\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}").matcher(m.group());
            if (numMatcher.find()) {
                return numMatcher.group();
            }
        }
        return null;
    }
    
    private String extract1099RecipientName(String text) {
        // Pattern: "RECIPIENT'S name" followed by name on next line
        Matcher m = Pattern.compile(
            "RECIPIENT'?S?\\s+name\\s*\\n+([A-Z][A-Z\\s,.'-]+?)\\n",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        ).matcher(text);
        if (m.find()) {
            String name = m.group(1).trim();
            // Clean up - remove if it contains field labels
            if (!name.toLowerCase().contains("street address") && 
                !name.toLowerCase().contains("apt.") &&
                name.length() > 2) {
                return name;
            }
        }
        return null;
    }
    
    private String extract1099RecipientTIN(String text) {
        // Pattern: "RECIPIENT'S TIN" followed by TIN (SSN format xxx-xx-xxxx or EIN xx-xxxxxxx)
        Matcher m = Pattern.compile(
            "RECIPIENT'?S?\\s+TIN\\s*\\n*\\s*(\\d{3}-\\d{2}-\\d{4}|\\d{2}-\\d{7})",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
    
    private String[] extract1099RecipientAddress(String text) {
        String[] result = new String[2]; // [street, city/state/zip]
        
        // Look for street address after "Street address" - stop at newline
        Matcher streetMatcher = Pattern.compile(
            "Street\\s+address[^\\n]*\\n+(\\d+[A-Z0-9\\s,.'-]+(?:ST|STREET|AVE|AVENUE|RD|ROAD|DR|DRIVE|LN|LANE|BLVD|WAY|CT|COURT|PL|PLACE)(?:\\s+(?:APT|UNIT|STE|SUITE)?\\s*[A-Z0-9#-]*)?)(?=\\n|$)",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (streetMatcher.find()) {
            String addr = streetMatcher.group(1).trim();
            // Clean up - remove any trailing numbers that start a new line
            addr = addr.replaceAll("\\n.*", "").trim();
            result[0] = addr;
        }
        
        // Look for city/state/zip after "City or town, state"
        Matcher cityMatcher = Pattern.compile(
            "City\\s+or\\s+town[^\\n]*\\n+([A-Z][A-Z\\s]+[,\\s]+[A-Z]{2}\\s+\\d{5}(?:-\\d{4})?)(?=\\n|$)",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (cityMatcher.find()) {
            result[1] = cityMatcher.group(1).trim();
        }
        
        return result;
    }
    
    private String extract1099TaxYear(String text) {
        // Pattern 1: "For calendar year 20 21" or "20 22"
        Matcher m = Pattern.compile(
            "(?:For\\s+)?calendar\\s+year\\s*\\n*\\s*20\\s*(\\d{2})",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (m.find()) {
            return "20" + m.group(1);
        }
        
        // Pattern 2: Form footer with year like "(Rev. January 2022)"
        Matcher revMatcher = Pattern.compile(
            "\\(Rev\\.?[^)]*?(20\\d{2})\\)",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (revMatcher.find()) {
            // The form revision year might be different from tax year
            // Continue looking for actual tax year
        }
        
        // Pattern 3: Just find "20XX" near relevant context
        Matcher yearMatcher = Pattern.compile(
            "(?:tax\\s+year|year|20)\\s*(\\d{2})(?!\\d)",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (yearMatcher.find()) {
            String yearPart = yearMatcher.group(1);
            int year = Integer.parseInt(yearPart);
            if (year >= 18 && year <= 30) { // Valid years 2018-2030
                return "20" + yearPart;
            }
        }
        
        return null;
    }
    
    private String extract1099AccountNumber(String text) {
        // Look for account number after "Account number" label
        // Skip if it's just instructions text like "(see instructions)"
        Matcher m = Pattern.compile(
            "Account\\s+number[^\\n]*\\n+\\$?\\s*([A-Z0-9][A-Z0-9-]{2,})",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (m.find()) {
            String num = m.group(1).trim();
            // Must be at least 3 characters and not just zeros or dollar amounts
            if (num.length() >= 3 && 
                !num.equals("0") && 
                !num.matches("^0+$") &&
                !num.matches("^0\\.00$") &&
                !num.toLowerCase().equals("see") &&
                !num.toLowerCase().startsWith("instruction")) {
                return num;
            }
        }
        return null;
    }
    
    private void extract1099NECBoxes(String text, Map<String, Form1099ExtractedData.BoxValue> boxValues) {
        // Box 1 - Nonemployee compensation
        String box1 = extract1099NECBox1(text);
        if (box1 != null) {
            boxValues.put("1", new Form1099ExtractedData.BoxValue(box1, 0.9));
        }
        
        // Box 4 - Federal income tax withheld
        String box4 = extractBoxByLabel(text, "4", "Federal\\s+income\\s+tax\\s+withheld");
        if (box4 != null) {
            boxValues.put("4", new Form1099ExtractedData.BoxValue(box4, 0.85));
        }
        
        // Box 5 - State tax withheld
        String box5 = extractBoxByLabel(text, "5", "State\\s+tax\\s+withheld");
        if (box5 != null) {
            boxValues.put("5", new Form1099ExtractedData.BoxValue(box5, 0.85));
        }
        
        // Box 6 - State/Payer's state no.
        String box6 = extractBoxByLabel(text, "6", "State/Payer'?s?\\s+state\\s+no");
        if (box6 != null) {
            boxValues.put("6", new Form1099ExtractedData.BoxValue(box6, 0.85));
        }
        
        // Box 7 - State income
        String box7 = extractBoxByLabel(text, "7", "State\\s+income");
        if (box7 != null) {
            boxValues.put("7", new Form1099ExtractedData.BoxValue(box7, 0.85));
        }
    }
    
    private String extract1099NECBox1(String text) {
        // Pattern: "1 Nonemployee compensation" followed by $ amount
        Matcher m = Pattern.compile(
            "1\\s+Nonemployee\\s+compensation[\\s\\S]{0,100}?\\$\\s*([\\d,]+\\.?\\d*)",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (m.find()) {
            return m.group(1).replace(",", "");
        }
        
        // Alternative: Look for dollar amount after "Nonemployee compensation"
        Matcher altMatcher = Pattern.compile(
            "Nonemployee\\s+compensation[\\s\\S]{0,50}?([\\d,]+\\.\\d{2})",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (altMatcher.find()) {
            return altMatcher.group(1).replace(",", "");
        }
        
        return null;
    }
    
    private void extract1099MISCBoxes(String text, Map<String, Form1099ExtractedData.BoxValue> boxValues) {
        // Box 1 - Rents
        extractAndAddBox(text, boxValues, "1", "Rents");
        // Box 2 - Royalties
        extractAndAddBox(text, boxValues, "2", "Royalties");
        // Box 3 - Other income
        extractAndAddBox(text, boxValues, "3", "Other\\s+income");
        // Box 4 - Federal income tax withheld
        extractAndAddBox(text, boxValues, "4", "Federal\\s+income\\s+tax\\s+withheld");
        // Box 5 - Fishing boat proceeds
        extractAndAddBox(text, boxValues, "5", "Fishing\\s+boat\\s+proceeds");
        // Box 6 - Medical and healthcare payments
        extractAndAddBox(text, boxValues, "6", "Medical\\s+and\\s+health\\s*care\\s+payments");
        // Box 7 - Direct sales
        extractAndAddBox(text, boxValues, "7", "Direct\\s+sales");
        // Box 8 - Substitute payments
        extractAndAddBox(text, boxValues, "8", "Substitute\\s+payments");
        // Box 10 - Crop insurance proceeds
        extractAndAddBox(text, boxValues, "10", "Crop\\s+insurance\\s+proceeds");
        // Box 11 - Fish purchased for resale
        extractAndAddBox(text, boxValues, "11", "Fish\\s+purchased\\s+for\\s+resale");
        // Box 12 - Section 409A deferrals
        extractAndAddBox(text, boxValues, "12", "Section\\s+409A\\s+deferrals");
        // Box 13 - Excess golden parachute payments
        extractAndAddBox(text, boxValues, "13", "Excess\\s+golden\\s+parachute\\s+payments");
        // Box 14 - Nonqualified deferred compensation
        extractAndAddBox(text, boxValues, "14", "Nonqualified\\s+deferred\\s+compensation");
        // Box 15 - State tax withheld
        extractAndAddBox(text, boxValues, "15", "State\\s+tax\\s+withheld");
        // Box 16 - State/Payer's state no.
        extractAndAddBox(text, boxValues, "16", "State/Payer'?s?\\s+state\\s+no");
        // Box 17 - State income
        extractAndAddBox(text, boxValues, "17", "State\\s+income");
    }
    
    private void extractAndAddBox(String text, Map<String, Form1099ExtractedData.BoxValue> boxValues, 
                                  String boxNum, String labelPattern) {
        String value = extractBoxByLabel(text, boxNum, labelPattern);
        if (value != null) {
            boxValues.put(boxNum, new Form1099ExtractedData.BoxValue(value, 0.85));
        }
    }
    
    private String extractBoxByLabel(String text, String boxNum, String labelPattern) {
        // Pattern: "boxNum labelPattern" followed by $ amount
        Matcher m = Pattern.compile(
            boxNum + "\\s+" + labelPattern + "[\\s\\S]{0,100}?\\$\\s*([\\d,]+\\.?\\d*)",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (m.find()) {
            String value = m.group(1).replace(",", "");
            if (!value.isEmpty() && !value.equals("0") && !value.equals("0.00")) {
                return value;
            }
        }
        return null;
    }
    
    private String extract1099BoxValue(String text, String boxNumber, String formType) {
        // Generic box value extraction
        Matcher m = Pattern.compile(
            "\\b" + boxNumber + "\\s+[A-Za-z\\s]+[\\s\\S]{0,50}?\\$\\s*([\\d,]+\\.?\\d*)",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (m.find()) {
            String value = m.group(1).replace(",", "");
            if (!value.isEmpty() && !value.equals("0") && !value.equals("0.00")) {
                return value;
            }
        }
        return null;
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
        String issuingState = extractIssuingState(extractedText);
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
        data.setIdTypeConfidence(idType.equals("Driver License") ? 0.9 : 0.6);

        // Calculate overall confidence
        data.setOverallConfidence(calculateOverallConfidenceGovernmentId(data));

        return data;
    }
    private String extractIssuingState(String text) {
        // Valid US state codes (exclude common 2-letter words like OF, ID, NO, DR, etc.)
        java.util.Set<String> validStateCodes = new java.util.HashSet<>(java.util.Arrays.asList(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA",
            "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT",
            "VA", "WA", "WV", "WI", "WY", "DC"
        ));
        
        // Pattern 1: Look for state code followed by zip code (most reliable for addresses)
        // e.g., "NC 28376" or "NC, 28376"
        Matcher zipMatcher = Pattern.compile("\\b([A-Z]{2})[,\\s]+\\d{5}\\b", Pattern.CASE_INSENSITIVE).matcher(text);
        while (zipMatcher.find()) {
            String stateCode = zipMatcher.group(1).toUpperCase();
            if (validStateCodes.contains(stateCode)) {
                return stateCode;
            }
        }
        
        // Pattern 2: Look for full state name in header (e.g., "NORTH CAROLINA")
        Matcher fullStateMatcher = Pattern.compile(
            "\\b(ALABAMA|ALASKA|ARIZONA|ARKANSAS|CALIFORNIA|COLORADO|CONNECTICUT|DELAWARE|FLORIDA|GEORGIA|HAWAII|IDAHO|ILLINOIS|INDIANA|IOWA|KANSAS|KENTUCKY|LOUISIANA|MAINE|MARYLAND|MASSACHUSETTS|MICHIGAN|MINNESOTA|MISSISSIPPI|MISSOURI|MONTANA|NEBRASKA|NEVADA|NEW HAMPSHIRE|NEW JERSEY|NEW MEXICO|NEW YORK|NORTH CAROLINA|NORTH DAKOTA|OHIO|OKLAHOMA|OREGON|PENNSYLVANIA|RHODE ISLAND|SOUTH CAROLINA|SOUTH DAKOTA|TENNESSEE|TEXAS|UTAH|VERMONT|VIRGINIA|WASHINGTON|WEST VIRGINIA|WISCONSIN|WYOMING)\\b",
            Pattern.CASE_INSENSITIVE
        ).matcher(text);
        if (fullStateMatcher.find()) {
            String fullState = fullStateMatcher.group(1).toUpperCase();
            // Convert full state name to code
            return convertStateNameToCode(fullState);
        }
        
        return null;
    }
    
    private String convertStateNameToCode(String stateName) {
        java.util.Map<String, String> stateMap = new java.util.HashMap<>();
        stateMap.put("ALABAMA", "AL"); stateMap.put("ALASKA", "AK"); stateMap.put("ARIZONA", "AZ");
        stateMap.put("ARKANSAS", "AR"); stateMap.put("CALIFORNIA", "CA"); stateMap.put("COLORADO", "CO");
        stateMap.put("CONNECTICUT", "CT"); stateMap.put("DELAWARE", "DE"); stateMap.put("FLORIDA", "FL");
        stateMap.put("GEORGIA", "GA"); stateMap.put("HAWAII", "HI"); stateMap.put("IDAHO", "ID");
        stateMap.put("ILLINOIS", "IL"); stateMap.put("INDIANA", "IN"); stateMap.put("IOWA", "IA");
        stateMap.put("KANSAS", "KS"); stateMap.put("KENTUCKY", "KY"); stateMap.put("LOUISIANA", "LA");
        stateMap.put("MAINE", "ME"); stateMap.put("MARYLAND", "MD"); stateMap.put("MASSACHUSETTS", "MA");
        stateMap.put("MICHIGAN", "MI"); stateMap.put("MINNESOTA", "MN"); stateMap.put("MISSISSIPPI", "MS");
        stateMap.put("MISSOURI", "MO"); stateMap.put("MONTANA", "MT"); stateMap.put("NEBRASKA", "NE");
        stateMap.put("NEVADA", "NV"); stateMap.put("NEW HAMPSHIRE", "NH"); stateMap.put("NEW JERSEY", "NJ");
        stateMap.put("NEW MEXICO", "NM"); stateMap.put("NEW YORK", "NY"); stateMap.put("NORTH CAROLINA", "NC");
        stateMap.put("NORTH DAKOTA", "ND"); stateMap.put("OHIO", "OH"); stateMap.put("OKLAHOMA", "OK");
        stateMap.put("OREGON", "OR"); stateMap.put("PENNSYLVANIA", "PA"); stateMap.put("RHODE ISLAND", "RI");
        stateMap.put("SOUTH CAROLINA", "SC"); stateMap.put("SOUTH DAKOTA", "SD"); stateMap.put("TENNESSEE", "TN");
        stateMap.put("TEXAS", "TX"); stateMap.put("UTAH", "UT"); stateMap.put("VERMONT", "VT");
        stateMap.put("VIRGINIA", "VA"); stateMap.put("WASHINGTON", "WA"); stateMap.put("WEST VIRGINIA", "WV");
        stateMap.put("WISCONSIN", "WI"); stateMap.put("WYOMING", "WY");
        return stateMap.getOrDefault(stateName.toUpperCase(), stateName);
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

        String street = null;
        String cityStateZip = null;

        // Street from employer section
        Matcher streetMatcher = Pattern.compile(
                "c Employer.?s name, address, and ZIP code[\\s\\n]+" +
                        "[A-Z0-9 .,&]+[\\s\\n]+" +
                        "(\\d+\\s+[A-Z0-9 .]+)",
                Pattern.CASE_INSENSITIVE
        ).matcher(text);

        if (streetMatcher.find()) {
            street = streetMatcher.group(1).trim();
        }

        //  City / State / ZIP from control number section (OCR shift)
        Matcher cityMatcher = Pattern.compile(
                "d Control number[\\s\\n]+" +
                        "([A-Z ]+\\s+[A-Z]{2}\\s+\\d{5})",
                Pattern.CASE_INSENSITIVE
        ).matcher(text);

        if (cityMatcher.find()) {
            cityStateZip = cityMatcher.group(1).trim();
        }

        if (street != null && cityStateZip != null) {
            return normalizeAddress(street + ", " + cityStateZip);
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
        extractBox(text, "6", boxes); //  ADD THIS
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
        // Recipient info
        if (data.getRecipientNameConfidence() != null) confidences.add(data.getRecipientNameConfidence());
        if (data.getRecipientTinConfidence() != null) confidences.add(data.getRecipientTinConfidence());
        if (data.getRecipientStreetAddressConfidence() != null) confidences.add(data.getRecipientStreetAddressConfidence());
        if (data.getRecipientCityStateZipConfidence() != null) confidences.add(data.getRecipientCityStateZipConfidence());
        // Payer info
        if (data.getPayerNameConfidence() != null) confidences.add(data.getPayerNameConfidence());
        if (data.getPayerTinConfidence() != null) confidences.add(data.getPayerTinConfidence());
        if (data.getPayerStreetAddressConfidence() != null) confidences.add(data.getPayerStreetAddressConfidence());
        if (data.getPayerCityStateZipConfidence() != null) confidences.add(data.getPayerCityStateZipConfidence());
        // Metadata
        if (data.getTaxYearConfidence() != null) confidences.add(data.getTaxYearConfidence());
        // Box values
        if (data.getBoxValues() != null && !data.getBoxValues().isEmpty()) {
            for (Form1099ExtractedData.BoxValue bv : data.getBoxValues().values()) {
                if (bv.getConfidence() != null) confidences.add(bv.getConfidence());
            }
        }

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
