package com.numbericsuserportal.LlcNorthwest.LLCFormation.controller;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateRateRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRate;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationRateService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.service.UserDataScopeContext;
import com.numbericsuserportal.usermanagement.service.UserDataScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/llc-formation/admin/rates")
@CrossOrigin(origins = "*")
public class LlcFormationRateAdminController {

    @Autowired
    private LlcFormationRateService rateService;

    @Autowired
    private UserDataScopeService userDataScopeService;

    @GetMapping
    public ResponseEntity<?> getRates(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            ensurePlatformWide(currentUser);
            return ResponseEntity.ok(rateService.getAllRates());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> upsertRate(@AuthenticationPrincipal User currentUser,
                                        @RequestBody UpdateRateRequestDTO req) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            ensurePlatformWide(currentUser);
            LlcFormationRate updated = rateService.upsert(req);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/seed-defaults")
    public ResponseEntity<?> seedDefaults(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            ensurePlatformWide(currentUser);
            rateService.seedDefaultsIfEmpty();
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void ensurePlatformWide(User user) {
        UserDataScopeContext scope = userDataScopeService.resolve(user);
        if (!scope.isPlatformWideDataAccess()) {
            throw new IllegalArgumentException("Only super admin can view/update rates");
        }
    }
}

