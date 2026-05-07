package com.numbericsuserportal.LlcNorthwest.LLCFormation.catalog;

import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Formation journey helpers: cached Step 0 reference data and jurisdiction-filtered RA list (Step 4).
 */
@RestController
@RequestMapping("/api/llc-northwest/llc-formation/catalog")
@CrossOrigin(origins = "*")
public class LlcFormationNorthwestCatalogController {

    @Value("${llc.northwest.website-url:https://www.numbrics.ai}")
    private String websiteUrl;

    @Autowired
    private NorthwestCatalogCache catalogCache;

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @GetMapping("/warmup-status")
    public ResponseEntity<?> warmupStatus() {
        return ResponseEntity.ok(Map.of(
                "warmupError", catalogCache.getLastWarmupError() != null ? catalogCache.getLastWarmupError() : "",
                "hasWebsitesCache", catalogCache.getWebsites() != null,
                "hasFilingProductsCache", catalogCache.getFilingProductsAllStates() != null,
                "hasRegisteredAgentProductsCache", catalogCache.getRegisteredAgentProductsAllStates() != null
        ));
    }

    /**
     * Registered agent products for the selected jurisdiction — from Step 0 cache when available,
     * otherwise fetched live from Corporate Tools.
     */
    @GetMapping("/registered-agent-products")
    public ResponseEntity<?> registeredAgentProductsForJurisdiction(@RequestParam String jurisdiction) {
        try {
            if (catalogCache.getRegisteredAgentProductsAllStates() != null) {
                RegisteredAgentProductsResponseDTO filtered =
                        catalogCache.filterRegisteredAgentProductsByJurisdiction(jurisdiction);
                return ResponseEntity.ok(filtered);
            }
            RegisteredAgentProductsResponseDTO live = corporateToolsApiService.getRegisteredAgentProducts(websiteUrl);
            RegisteredAgentProductsResponseDTO copy = new RegisteredAgentProductsResponseDTO();
            copy.setSuccess(live.getSuccess());
            copy.setTimestamp(live.getTimestamp());
            if (live.getResult() == null) {
                copy.setResult(java.util.List.of());
            } else {
                copy.setResult(live.getResult().stream()
                        .filter(p -> p.getJurisdiction() != null
                                && p.getJurisdiction().equalsIgnoreCase(jurisdiction.trim()))
                        .collect(java.util.stream.Collectors.toList()));
            }
            return ResponseEntity.ok(copy);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
