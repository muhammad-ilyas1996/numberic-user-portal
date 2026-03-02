package com.numbericsuserportal.invoice.service;

/**
 * Send transactional emails (e.g. invoice payment link).
 */
public interface EmailService {

    /**
     * Send an email. Returns true if sent successfully.
     */
    boolean sendEmail(String toEmail, String subject, String htmlBody);
}
