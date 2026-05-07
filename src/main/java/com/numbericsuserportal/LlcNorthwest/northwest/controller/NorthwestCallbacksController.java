package com.numbericsuserportal.LlcNorthwest.northwest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/callbacks")
@CrossOrigin(origins = "*")
public class NorthwestCallbacksController {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @GetMapping
    public ResponseEntity<?> getCallbacks(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            JsonNode response = corporateToolsApiService.getCallbacks();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> postCallbacks(@AuthenticationPrincipal User currentUser,
                                           @RequestBody(required = false) JsonNode body) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String raw = body == null || body.isNull() ? "" : body.toString();
            JsonNode response = corporateToolsApiService.postCallbacks(raw);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
