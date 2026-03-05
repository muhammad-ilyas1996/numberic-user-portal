package com.numbericsuserportal.invoice.controller;

import com.numbericsuserportal.invoice.dto.MerchantNmiConfigDto;
import com.numbericsuserportal.invoice.service.MerchantNmiConfigService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Settings page: merchant configures NMI credentials (after onboarding on NMI portal).
 * GET = current config (masked), PUT = save config.
 */
@RestController
@RequestMapping("/v1/settings/nmi")
public class MerchantNmiSettingsController {

    @Autowired
    private MerchantNmiConfigService merchantNmiConfigService;

    @GetMapping
    public ResponseEntity<MerchantNmiConfigDto> getMyConfig(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(merchantNmiConfigService.getByUserId(currentUser.getUserId()));
    }

    @PutMapping
    public ResponseEntity<MerchantNmiConfigDto> saveMyConfig(
            @RequestBody MerchantNmiConfigDto dto,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(merchantNmiConfigService.save(currentUser.getUserId(), dto));
    }
}
