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
 * Registered agent activation (Step 8) — payloads are forwarded as JSON to Corporate Tools.
 */
@RestController
@RequestMapping("/api/llc-northwest/services")
@CrossOrigin(origins = "*")
public class NorthwestServicesController {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @PostMapping
    public ResponseEntity<?> createService(@AuthenticationPrincipal User currentUser,
                                           @RequestBody(required = false) JsonNode body) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String raw = body == null || body.isNull() ? "{}" : body.toString();
            JsonNode response = corporateToolsApiService.postServices(raw);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{serviceId}/info")
    public ResponseEntity<?> postServiceInfo(@AuthenticationPrincipal User currentUser,
                                             @PathVariable UUID serviceId,
                                             @RequestBody(required = false) JsonNode body) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String raw = body == null || body.isNull() ? "{}" : body.toString();
            JsonNode response = corporateToolsApiService.postServiceInfo(serviceId, raw);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
