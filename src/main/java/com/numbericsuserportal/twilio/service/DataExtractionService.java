package com.numbericsuserportal.twilio.service;

import com.numbericsuserportal.twilio.dto.*;

/**
 * Service interface for structured data extraction from OCR text
 */
public interface DataExtractionService {

    W2ExtractedData extractW2Data(String extractedText);

    Form1099ExtractedData extract1099Data(String extractedText, String formType);
    

    GovernmentIdExtractedData extractGovernmentIdData(String extractedText);
}

