package com.numbericsuserportal.twilio.repository;

import com.numbericsuserportal.twilio.entity.TaxCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TaxCase entity
 */
@Repository
public interface TaxCaseRepository extends JpaRepository<TaxCase, Long> {

    /**
     * Find active tax case by phone number
     * Active cases are those with status NEW, NEEDS_DOCUMENTS, or IN_PROGRESS
     */
    Optional<TaxCase> findByPhoneNumberAndStatusIn(
            String phoneNumber,
            List<TaxCase.TaxCaseStatus> statuses
    );

    /**
     * Find all tax cases for a specific user
     */
    List<TaxCase> findByUserId(Long userId);

    /**
     * Find all tax cases for a specific phone number
     */
    List<TaxCase> findByPhoneNumber(String phoneNumber);

    /**
     * Find all tax cases with a specific status
     */
    List<TaxCase> findByStatus(TaxCase.TaxCaseStatus status);
}

