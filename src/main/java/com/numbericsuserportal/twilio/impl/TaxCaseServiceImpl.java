package com.numbericsuserportal.twilio.impl;

import com.numbericsuserportal.twilio.dto.TaxCaseResult;
import com.numbericsuserportal.twilio.entity.TaxCase;
import com.numbericsuserportal.twilio.repository.TaxCaseRepository;
import com.numbericsuserportal.twilio.service.TaxCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service implementation for TaxCase operations
 */
@Service
public class TaxCaseServiceImpl implements TaxCaseService {

    @Autowired
    private TaxCaseRepository taxCaseRepository;

    /**
     * Active statuses - cases with these statuses are considered "active"
     */
    private static final List<TaxCase.TaxCaseStatus> ACTIVE_STATUSES = Arrays.asList(
            TaxCase.TaxCaseStatus.NEW,
            TaxCase.TaxCaseStatus.NEEDS_DOCUMENTS,
            TaxCase.TaxCaseStatus.IN_PROGRESS
    );

    @Override
    @Transactional
    public TaxCase getOrCreateActiveTaxCase(String phoneNumber, Long userId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Check if active tax case exists for this phone number
        TaxCase existingCase = findActiveTaxCase(phoneNumber);

        if (existingCase != null) {
            // Update user_id if it was null and we now have a user_id
            if (existingCase.getUserId() == null && userId != null) {
                existingCase.setUserId(userId);
                existingCase = taxCaseRepository.save(existingCase);
                System.out.println("Updated existing TaxCase with user_id: " + userId);
            }
            System.out.println("Found existing active TaxCase: " + existingCase.getId());
            return existingCase;
        }

        // No active case exists, create a new one
        System.out.println("Creating new TaxCase for phone: " + phoneNumber);
        return createTaxCase(phoneNumber, userId);
    }

    @Override
    @Transactional
    public TaxCaseResult getOrCreateActiveTaxCaseWithResult(String phoneNumber, Long userId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Check if active tax case exists for this phone number
        TaxCase existingCase = findActiveTaxCase(phoneNumber);
        TaxCase.TaxCaseStatus previousStatus = null;

        if (existingCase != null) {
            // Store previous status before any updates
            previousStatus = existingCase.getStatus();
            
            // Update user_id if it was null and we now have a user_id
            boolean userUpdated = false;
            if (existingCase.getUserId() == null && userId != null) {
                existingCase.setUserId(userId);
                existingCase = taxCaseRepository.save(existingCase);
                userUpdated = true;
                System.out.println("Updated existing TaxCase with user_id: " + userId);
            }
            
            // Check if status changed (for now, status doesn't change in this method,
            // but we track it for future use when status updates are implemented)
            boolean statusChanged = false;
            if (previousStatus != null && existingCase.getStatus() != previousStatus) {
                statusChanged = true;
                System.out.println("TaxCase status changed from " + previousStatus + 
                                 " to " + existingCase.getStatus());
            }
            
            System.out.println("Found existing active TaxCase: " + existingCase.getId());
            return new TaxCaseResult(existingCase, false, statusChanged, previousStatus);
        }

        // No active case exists, create a new one
        System.out.println("Creating new TaxCase for phone: " + phoneNumber);
        TaxCase newCase = createTaxCase(phoneNumber, userId);
        return new TaxCaseResult(newCase, true, false, null);
    }

    @Override
    public TaxCase findActiveTaxCase(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        return taxCaseRepository.findByPhoneNumberAndStatusIn(phoneNumber, ACTIVE_STATUSES)
                .orElse(null);
    }

    @Override
    @Transactional
    public TaxCase createTaxCase(String phoneNumber, Long userId) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        TaxCase taxCase = new TaxCase();
        taxCase.setPhoneNumber(phoneNumber);
        taxCase.setUserId(userId);
        taxCase.setStatus(TaxCase.TaxCaseStatus.NEW);
        taxCase.setCreatedAt(LocalDateTime.now());

        TaxCase savedCase = taxCaseRepository.save(taxCase);
        System.out.println("Created new TaxCase with ID: " + savedCase.getId() + 
                          " for phone: " + phoneNumber + 
                          (userId != null ? " (user_id: " + userId + ")" : " (no user)"));

        return savedCase;
    }

    @Override
    @Transactional
    public TaxCase updateTaxCaseStatus(Long taxCaseId, TaxCase.TaxCaseStatus newStatus) {
        if (taxCaseId == null) {
            throw new IllegalArgumentException("TaxCase ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        TaxCase taxCase = taxCaseRepository.findById(taxCaseId)
                .orElseThrow(() -> new RuntimeException("TaxCase not found with ID: " + taxCaseId));

        TaxCase.TaxCaseStatus oldStatus = taxCase.getStatus();
        taxCase.setStatus(newStatus);
        
        TaxCase updatedCase = taxCaseRepository.save(taxCase);
        System.out.println("Updated TaxCase ID: " + taxCaseId + 
                         " status from " + oldStatus + " to " + newStatus);

        return updatedCase;
    }
}

