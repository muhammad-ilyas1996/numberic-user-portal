package com.numbericsuserportal.LlcNorthwest.registeredagent.controller;

import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.registeredagent.service.RegisteredAgentProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/registered-agent-products")
@CrossOrigin(origins = "*")
public class RegisteredAgentProductController {

    @Autowired
    private RegisteredAgentProductService registeredAgentProductService;

    /**
     * GET /api/llc-northwest/registered-agent-products
     * Get registered agent products for a website
     */
    @GetMapping
    public ResponseEntity<?> getRegisteredAgentProducts(@RequestParam String url) {
        try {
            RegisteredAgentProductsResponseDTO response = registeredAgentProductService.getRegisteredAgentProducts(url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

