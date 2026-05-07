package com.numbericsuserportal.LlcNorthwest.northwest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Filing attention queue (Step 7). Scheduled jobs call the service layer directly; these routes are for admin/debug.
 */
@RestController
@RequestMapping("/api/llc-northwest/order-items/requiring-attention")
@CrossOrigin(origins = "*")
public class NorthwestOrderItemsController {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @GetMapping
    public ResponseEntity<?> get(@AuthenticationPrincipal User currentUser,
                                 @RequestParam("company_id") UUID companyId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            JsonNode response = corporateToolsApiService.getOrderItemsRequiringAttention(companyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> post(@AuthenticationPrincipal User currentUser,
                                  @RequestBody(required = false) JsonNode body) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String raw = body == null || body.isNull() ? "{}" : body.toString();
            JsonNode response = corporateToolsApiService.postOrderItemsRequiringAttention(raw);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
