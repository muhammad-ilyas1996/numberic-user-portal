package com.numbericsuserportal.usermanagement.service;

import com.numbericsuserportal.registration.validation.PasswordValidator;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthEmailService authEmailService;

    @Autowired
    private PasswordValidator passwordValidator;

    @Value("${app.auth.reset-token-expiry-hours:24}")
    private int resetTokenExpiryHours;

    public User authenticate(String username, String rawPassword) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getIsDeleted()) || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("User inactive or deleted");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return user;
    }

    /**
     * Initiate password reset process
     * Generates reset token, saves it to user, and sends email
     * Always returns success to prevent email enumeration
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepo.findByEmail(email)
                .orElse(null);

        // Only process if user exists, is active, and not deleted
        if (user != null && 
            Boolean.TRUE.equals(user.getIsActive()) && 
            !Boolean.TRUE.equals(user.getIsDeleted())) {
            
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(resetTokenExpiryHours);
            
            // Save token and expiry
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(expiryTime);
            userRepo.save(user);
            
            // Send email
            authEmailService.sendPasswordResetEmail(email, resetToken);
        }
        // Always return success (don't reveal if email exists)
    }

    /**
     * Reset password using reset token
     * Validates token, checks expiry, and updates password
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find user by reset token
        User user = userRepo.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        // Validate token expiry
        if (user.getResetTokenExpiry() == null || 
            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired token");
        }

        // Validate password strength
        if (!passwordValidator.isValid(newPassword, null)) {
            throw new RuntimeException("Password must be at least 10 characters with 1 uppercase, 1 lowercase, 1 number, and 1 special character");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Clear reset token and expiry
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        
        userRepo.save(user);
    }

    /**
     * Send username reminder email
     * Always returns success to prevent email enumeration
     */
    @Transactional
    public void sendUsernameReminder(String email) {
        User user = userRepo.findByEmail(email)
                .orElse(null);

        // Only process if user exists, is active, and not deleted
        if (user != null && 
            Boolean.TRUE.equals(user.getIsActive()) && 
            !Boolean.TRUE.equals(user.getIsDeleted())) {
            
            // Send email with username
            authEmailService.sendUsernameReminderEmail(email, user.getUsername());
        }
        // Always return success (don't reveal if email exists)
    }
}

