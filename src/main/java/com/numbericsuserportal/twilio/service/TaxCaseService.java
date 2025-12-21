package com.numbericsuserportal.twilio.service;

import com.numbericsuserportal.twilio.dto.TaxCaseResult;
import com.numbericsuserportal.twilio.entity.TaxCase;

/**
 * Service interface for TaxCase operations
 */
public interface TaxCaseService {

    /**
     * Get or create active tax case for a phone number
     * 
     * If an active tax case exists for the phone number, return it.
     * Otherwise, create a new tax case with status NEW.
     * 
     * @param phoneNumber Phone number (normalized, without "whatsapp:" prefix)
     * @param userId User ID if available (can be null)
     * @return TaxCase entity (existing or newly created)
     */
    TaxCase getOrCreateActiveTaxCase(String phoneNumber, Long userId);

    /**
     * Get or create active tax case for a phone number with result tracking
     * 
     * Returns a TaxCaseResult that indicates:
     * - Whether the TaxCase was newly created
     * - Whether the status has changed
     * 
     * @param phoneNumber Phone number (normalized, without "whatsapp:" prefix)
     * @param userId User ID if available (can be null)
     * @return TaxCaseResult containing TaxCase and tracking information
     */
    TaxCaseResult getOrCreateActiveTaxCaseWithResult(String phoneNumber, Long userId);

    /**
     * Find active tax case by phone number
     * 
     * @param phoneNumber Phone number (normalized)
     * @return TaxCase if found, null otherwise
     */
    TaxCase findActiveTaxCase(String phoneNumber);

    /**
     * Create a new tax case
     * 
     * @param phoneNumber Phone number (normalized)
     * @param userId User ID if available (can be null)
     * @return Newly created TaxCase
     */
    TaxCase createTaxCase(String phoneNumber, Long userId);

    /**
     * Update TaxCase status
     * 
     * @param taxCaseId TaxCase ID
     * @param newStatus New status to set
     * @return Updated TaxCase
     */
    TaxCase updateTaxCaseStatus(Long taxCaseId, TaxCase.TaxCaseStatus newStatus);
}

