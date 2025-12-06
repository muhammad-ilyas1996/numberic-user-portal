package com.numbericsuserportal.LlcNorthwest.controller;

import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.service.FilingProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/filing-products")
@CrossOrigin(origins = "*")
public class FilingProductsController {

    @Autowired
    private FilingProductService filingProductService;

    /**
     * Fetch and save filing products from API
     * GET /api/llc-northwest/filing-products?url={websiteUrl}&jurisdiction={jurisdiction}&entityType={entityType}
     */
    @GetMapping
    public ResponseEntity<?> getFilingProducts(
            @RequestParam String url,
            @RequestParam(required = false) String jurisdiction,
            @RequestParam(required = false) String entityType) {
        
        try {
            FilingProductsResponseDTO response = filingProductService.fetchAndSaveFilingProducts(
                url,
                jurisdiction,
                entityType
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get filing products from database
     * GET /api/llc-northwest/filing-products/database?url={websiteUrl}
     */
    @GetMapping("/database")
    public ResponseEntity<?> getFilingProductsFromDatabase(
            @RequestParam(required = false) String url) {
        
        try {
            FilingProductsResponseDTO response = filingProductService.getFilingProductsFromDatabase(url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

