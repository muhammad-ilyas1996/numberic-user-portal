package com.numbericsuserportal.LlcNorthwest.filingmethod.controller;

import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.service.FilingMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/llc-northwest/filing-methods")
@CrossOrigin(origins = "*")
public class FilingMethodController {

    @Autowired
    private FilingMethodService filingMethodService;

    /**
     * GET /api/llc-northwest/filing-methods
     * Get filing methods for a company, filing product, and jurisdiction
     */
    @GetMapping
    public ResponseEntity<?> getFilingMethods(
            @RequestParam UUID companyId,
            @RequestParam UUID filingProductId,
            @RequestParam String jurisdiction) {
        
        try {
            FilingMethodsResponseDTO response = filingMethodService.getFilingMethods(
                companyId, 
                filingProductId, 
                jurisdiction
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/llc-northwest/filing-methods/schemas
     * Get filing method schema/form structure
     */
    @GetMapping("/schemas")
    public ResponseEntity<?> getFilingMethodSchemas(
            @RequestParam UUID companyId,
            @RequestParam UUID filingMethodId) {
        
        try {
            FilingMethodSchemaResponseDTO response = filingMethodService.getFilingMethodSchemas(
                companyId, 
                filingMethodId
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

