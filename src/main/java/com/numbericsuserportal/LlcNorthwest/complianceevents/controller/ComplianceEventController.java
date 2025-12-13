package com.numbericsuserportal.LlcNorthwest.complianceevents.controller;

import com.numbericsuserportal.LlcNorthwest.complianceevents.dto.ComplianceEventsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.complianceevents.service.ComplianceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/llc-northwest/compliance-events")
@CrossOrigin(origins = "*")
public class ComplianceEventController {

    @Autowired
    private ComplianceEventService complianceEventService;

    /**
     * GET /api/llc-northwest/compliance-events
     * Get compliance events with optional filters
     */
    @GetMapping
    public ResponseEntity<?> getComplianceEvents(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) UUID companyId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String[] jurisdictions,
            @RequestParam(required = false) UUID[] jurisdictionIds) {
        
        try {
            ComplianceEventsResponseDTO response = complianceEventService.getComplianceEvents(
                limit,
                offset,
                company,
                companyId,
                startDate,
                endDate,
                jurisdictions,
                jurisdictionIds
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

