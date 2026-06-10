package com.numbericsuserportal.LlcNorthwest.LLCFormation.controller;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.CreateFormationResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateNorthwestShoppingCartJsonRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep1StateRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep2NameRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep3DetailsRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep4RegisteredAgentRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationRegisteredAgentService;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/llc-formation")
@CrossOrigin(origins = "*")
public class LlcFormationController {

    @Autowired
    private LlcFormationService formationService;

    @Autowired
    private LlcFormationRegisteredAgentService registeredAgentService;

    @PostMapping
    public ResponseEntity<?> createDraft(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            LlcFormation f = formationService.createDraft(currentUser);
            return ResponseEntity.ok(new CreateFormationResponseDTO(f.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping
    public ResponseEntity<?> getAllFormations(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(formationService.getFormationsForUser(currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/{formationId}")
    public ResponseEntity<?> getFormation(@AuthenticationPrincipal User currentUser, @PathVariable Long formationId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(formationService.getForUserOrThrow(formationId, currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{formationId}/step1/state")
    public ResponseEntity<?> updateStep1(@AuthenticationPrincipal User currentUser,
                                         @PathVariable Long formationId,
                                         @RequestBody UpdateStep1StateRequestDTO req) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(formationService.updateStep1(formationId, currentUser, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{formationId}/step2/name")
    public ResponseEntity<?> updateStep2(@AuthenticationPrincipal User currentUser,
                                         @PathVariable Long formationId,
                                         @RequestBody UpdateStep2NameRequestDTO req) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(formationService.updateStep2(formationId, currentUser, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{formationId}/step3/details")
    public ResponseEntity<?> updateStep3(@AuthenticationPrincipal User currentUser,
                                         @PathVariable Long formationId,
                                         @RequestBody UpdateStep3DetailsRequestDTO req) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(formationService.updateStep3(formationId, currentUser, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{formationId}/step4/registered-agent")
    public ResponseEntity<?> updateStep4(@AuthenticationPrincipal User currentUser,
                                         @PathVariable Long formationId,
                                         @RequestBody UpdateStep4RegisteredAgentRequestDTO req) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            LlcFormation formation = formationService.getForUserOrThrow(formationId, currentUser);
            return ResponseEntity.ok(registeredAgentService.upsert(formationId, formation, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Stores the exact JSON body for Corporate Tools POST /shopping-cart before payment (Step 6 prep).
     */
    @PatchMapping("/{formationId}/northwest/shopping-cart-payload")
    public ResponseEntity<?> updateNorthwestShoppingCartPayload(@AuthenticationPrincipal User currentUser,
                                                                @PathVariable Long formationId,
                                                                @RequestBody UpdateNorthwestShoppingCartJsonRequestDTO req) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(formationService.updateNorthwestShoppingCartJson(formationId, currentUser, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

