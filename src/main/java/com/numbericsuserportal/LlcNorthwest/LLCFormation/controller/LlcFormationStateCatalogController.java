package com.numbericsuserportal.LlcNorthwest.LLCFormation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.BulkSeedStateCatalogRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationStateCatalogService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/llc-formation/states")
@CrossOrigin(origins = "*")
public class LlcFormationStateCatalogController {

    @Autowired
    private LlcFormationStateCatalogService stateCatalogService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<?> listStates(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(stateCatalogService.getActiveCatalog());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{stateCode}")
    public ResponseEntity<?> getState(@AuthenticationPrincipal User currentUser,
                                      @PathVariable String stateCode) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return stateCatalogService.getByStateCode(stateCode)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bulk-seed")
    public ResponseEntity<?> bulkSeed(@AuthenticationPrincipal User currentUser,
                                      @RequestBody BulkSeedStateCatalogRequestDTO req) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(stateCatalogService.bulkSeed(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bulk-seed-defaults")
    public ResponseEntity<?> bulkSeedDefaults(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            ClassPathResource resource = new ClassPathResource("llc-formation-states-bulk-seed.json");
            try (InputStream in = resource.getInputStream()) {
                BulkSeedStateCatalogRequestDTO req = objectMapper.readValue(in, BulkSeedStateCatalogRequestDTO.class);
                return ResponseEntity.ok(stateCatalogService.bulkSeed(req));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
