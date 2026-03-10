package com.numbericsuserportal.registration.controller;

import com.numbericsuserportal.registration.dto.OnboardingRequestDto;
import com.numbericsuserportal.registration.dto.OnboardingResponseDto;
import com.numbericsuserportal.registration.service.OnboardingService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin(origins = "*")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    /**
     * GET onboarding: load current user's business profile for pre-fill.
     * Response includes completed flag (redirect to dashboard if true).
     */
    @GetMapping
    public ResponseEntity<OnboardingResponseDto> getOnboarding(
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(onboardingService.getOnboarding(currentUser));
    }

    /**
     * POST onboarding: submit questionnaire; create profile if missing, update same row otherwise.
     */
    @PostMapping
    public ResponseEntity<OnboardingResponseDto> saveOnboarding(
            @AuthenticationPrincipal User currentUser,
            @RequestBody OnboardingRequestDto dto) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(onboardingService.saveOnboarding(currentUser, dto));
    }
}
