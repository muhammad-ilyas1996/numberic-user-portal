package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.dto.*;
import com.numbericsuserportal.usermanagement.service.AuthService;
import com.numbericsuserportal.usermanagement.jwt.JwtUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = authService.authenticate(req.getUsername(), req.getPassword());

            // Optional: patient portal me sirf patient ko allow karna ho:
            // if (!user.getUserType().equals(User.UserType.patient)) { return 403; }

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * POST /auth/forgot-password
     * Initiate password reset process
     * Always returns generic success message for security
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(new AuthResponse(true, 
                "If the email exists, a password reset link has been sent."));
        } catch (Exception e) {
            // Still return generic success to prevent email enumeration
            return ResponseEntity.ok(new AuthResponse(true, 
                "If the email exists, a password reset link has been sent."));
        }
    }

    /**
     * POST /auth/reset-password
     * Reset password using reset token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new AuthResponse(true, 
                "Password has been reset successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(false, "Failed to reset password. Please try again."));
        }
    }

    /**
     * POST /auth/forgot-username
     * Send username reminder email
     * Always returns generic success message for security
     */
    @PostMapping("/forgot-username")
    public ResponseEntity<AuthResponse> forgotUsername(@Valid @RequestBody ForgotUsernameRequest request) {
        try {
            authService.sendUsernameReminder(request.getEmail());
            return ResponseEntity.ok(new AuthResponse(true, 
                "If the email exists, username reminder has been sent."));
        } catch (Exception e) {
            // Still return generic success to prevent email enumeration
            return ResponseEntity.ok(new AuthResponse(true, 
                "If the email exists, username reminder has been sent."));
        }
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginRequest {
    private String username;
    private String password;
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class TokenResponse {
    private String token;
}

