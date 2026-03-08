package com.numbericsuserportal.usermanagement.service;

/**
 * Email service for authentication-related emails (password reset, username reminder)
 */
public interface AuthEmailService {
    
    /**
     * Send password reset email with reset link
     * @param toEmail Recipient email address
     * @param resetToken Reset token to include in link
     * @return true if email sent successfully, false otherwise
     */
    boolean sendPasswordResetEmail(String toEmail, String resetToken);
    
    /**
     * Send username reminder email
     * @param toEmail Recipient email address
     * @param username Username to send
     * @return true if email sent successfully, false otherwise
     */
    boolean sendUsernameReminderEmail(String toEmail, String username);
}
