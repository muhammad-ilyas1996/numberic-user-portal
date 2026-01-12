package com.numbericsuserportal.registration.controller;

import com.numbericsuserportal.registration.dto.*;
import com.numbericsuserportal.registration.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/register")
@CrossOrigin(origins = "*")
public class RegistrationController {
    
    @Autowired
    private RegistrationService registrationService;
    
    @PostMapping("/step1")
    public ResponseEntity<?> registerStep1(@Valid @RequestBody RegistrationStep1Dto dto) {
        try {
            var user = registrationService.registerStep1(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Account created successfully",
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "role", dto.getPreferredRole(),
                "accountType", dto.getAccountType().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/step2/{userId}")
    public ResponseEntity<?> registerStep2(@PathVariable Long userId, 
                                         @Valid @RequestBody RegistrationStep2Dto dto) {
        try {
            var profile = registrationService.registerStep2(userId, dto);
            if (profile != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Business profile saved successfully",
                    "businessProfileId", profile.getBusinessProfileId()
                ));
            } else {
                // Individual account - step 2 skipped
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Step 2 completed (not required for individual accounts)"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/step3/{userId}")
    public ResponseEntity<?> registerStep3(@PathVariable Long userId,
                                         @Valid @RequestBody RegistrationStep3Dto dto) {
        try {
            registrationService.registerStep3(userId, dto);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Addresses saved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/step4/{userId}")
    public ResponseEntity<?> registerStep4(@PathVariable Long userId,
                                         @Valid @RequestBody RegistrationStep4Dto dto) {
        try {
            var owner = registrationService.registerStep4(userId, dto);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Beneficial owner added successfully",
                "ownerId", owner.getOwnerId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/step5/{userId}")
    public ResponseEntity<?> registerStep5(@PathVariable Long userId,
                                         @Valid @RequestBody RegistrationStep5Dto dto) {
        try {
            var bankAccount = registrationService.registerStep5(userId, dto);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bank account saved successfully",
                "bankAccountId", bankAccount.getBankAccountId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/step6/{userId}")
    public ResponseEntity<?> registerStep6(@PathVariable Long userId,
                                         @Valid @RequestBody RegistrationStep6Dto dto) {
        try {
            registrationService.registerStep6(userId, dto);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Security settings saved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/step7/{userId}")
    public ResponseEntity<?> registerStep7(@PathVariable Long userId,
                                         @Valid @RequestBody RegistrationStep7Dto dto) {
        try {
            registrationService.registerStep7(userId, dto);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registration completed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getRegistrationStatus(@PathVariable Long userId) {
        try {
            var status = registrationService.getRegistrationStatus(userId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Validation failed");
        response.put("errors", errors);
        
        return ResponseEntity.badRequest().body(response);
    }
}

