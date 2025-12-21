package com.numbericsuserportal.twilio.dto;

import com.numbericsuserportal.twilio.entity.TaxCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result object for TaxCase operations
 * Tracks whether TaxCase was newly created or if status changed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxCaseResult {
    
    /**
     * The TaxCase entity
     */
    private TaxCase taxCase;
    
    /**
     * Whether the TaxCase was newly created
     */
    private boolean isNewlyCreated;
    
    /**
     * Whether the TaxCase status has changed
     */
    private boolean statusChanged;
    
    /**
     * Previous status (if status changed)
     */
    private TaxCase.TaxCaseStatus previousStatus;
}

