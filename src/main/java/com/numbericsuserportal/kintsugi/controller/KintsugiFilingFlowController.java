package com.numbericsuserportal.kintsugi.controller;

import com.numbericsuserportal.kintsugi.domain.SalesTaxBusinessType;
import com.numbericsuserportal.kintsugi.dto.*;
import com.numbericsuserportal.kintsugi.service.ExemptionService;
import com.numbericsuserportal.kintsugi.service.KintsugiFilingFlowService;
import com.numbericsuserportal.kintsugi.service.KintsugiFilingSubmissionService;
import com.numbericsuserportal.kintsugi.service.KintsugiNexusService;
import com.numbericsuserportal.kintsugi.service.KintsugiProductService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Filing Flow v2 — full wizard backed by Kintsugi APIs.
 * <p>
 * Screen 0A/0B: business type + exemption profile
 * Screen 1–3: categories, subcategories, states (Kintsugi catalog)
 * Screen 4: enter sales (taxable + exempt split)
 * Screen 5: review & pay
 * Screen 6: filed success + schedule
 */
@RestController
@RequestMapping("/api/kintsugi/filing-flow")
@CrossOrigin(origins = "*")
public class KintsugiFilingFlowController {

    private final KintsugiFilingFlowService filingFlowService;
    private final KintsugiProductService productService;
    private final KintsugiNexusService nexusService;
    private final ExemptionService exemptionService;
    private final KintsugiFilingSubmissionService submissionService;

    public KintsugiFilingFlowController(
            KintsugiFilingFlowService filingFlowService,
            KintsugiProductService productService,
            KintsugiNexusService nexusService,
            ExemptionService exemptionService,
            KintsugiFilingSubmissionService submissionService) {
        this.filingFlowService = filingFlowService;
        this.productService = productService;
        this.nexusService = nexusService;
        this.exemptionService = exemptionService;
        this.submissionService = submissionService;
    }

    // ─── Screen 0A — business type ───────────────────────────────────────────

    @GetMapping("/step0a/setup")
    public ResponseEntity<?> step0aSetup(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", "0A",
                    "setup", exemptionService.getBusinessTypeSetup(currentUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/step0a/business-type")
    public ResponseEntity<?> step0aSaveBusinessType(
            @AuthenticationPrincipal User currentUser,
            @RequestBody SaveBusinessTypeRequestDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            if (request.getBusinessType() == null || request.getBusinessType().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "businessType is required"));
            }
            SalesTaxBusinessType type = SalesTaxBusinessType.valueOf(request.getBusinessType().trim());
            return ResponseEntity.ok(Map.of(
                    "step", "0A",
                    "result", exemptionService.saveBusinessType(currentUser, type, request.getDescription())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid businessType: " + request.getBusinessType()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Screen 0B — exemption profile ───────────────────────────────────────

    @GetMapping("/step0b/exemptions")
    public ResponseEntity<?> step0bExemptions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String stateCode) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", "0B",
                    "profile", exemptionService.getExemptionProfile(currentUser, stateCode)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/step0b/exemptions/confirm")
    public ResponseEntity<?> step0bConfirmExemptions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String stateCode) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", "0B",
                    "profile", exemptionService.confirmExemptionProfile(currentUser, stateCode)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/step0b/exemptions")
    public ResponseEntity<?> step0bUpdateExemptions(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateExemptionProfileRequestDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", "0B",
                    "profile", exemptionService.updateExemptionProfile(currentUser, request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Screens 1–3 — Kintsugi catalog flow ───────────────────────────────────

    @GetMapping("/step1/categories")
    public ResponseEntity<?> step1Categories(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", 1,
                    "categories", filingFlowService.step1Categories()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/step2/categories/{categoryName}/subcategories")
    public ResponseEntity<?> step2Subcategories(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String categoryName) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", 2,
                    "category", productService.getCategory(categoryName).getName(),
                    "subcategories", filingFlowService.step2Subcategories(categoryName)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Add State screen — active states with nexus + registration branch */
    @GetMapping("/step3/states")
    public ResponseEntity<?> step3States(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", 3,
                    "states", filingFlowService.step3States(),
                    "filingStates", submissionService.activeStatesForFiling(currentUser)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/step3/states/{stateCode}")
    public ResponseEntity<?> step3StateDetail(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String stateCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) Double sampleAmount) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            Map<String, Object> detail = new java.util.LinkedHashMap<>();
            detail.put("state", filingFlowService.step3StateDetail(
                    stateCode, category, subcategory, sampleAmount));
            detail.put("filingContext", submissionService.stateFilingContext(currentUser, stateCode));
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/step3/save-selection")
    public ResponseEntity<?> step3SaveSelection(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FilingFlowSelectionDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", 3,
                    "selection", submissionService.saveFlowSelection(currentUser, request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/step3/states/{stateCode}/filing-context")
    public ResponseEntity<?> step3FilingContext(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String stateCode) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(submissionService.stateFilingContext(currentUser, stateCode));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Screen 4 — enter sales ────────────────────────────────────────────────

    @PostMapping("/step4/estimate-sales")
    public ResponseEntity<?> step4EstimateSales(
            @AuthenticationPrincipal User currentUser,
            @RequestBody EnterSalesEstimateRequestDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", 4,
                    "estimate", submissionService.estimateAndSaveSales(currentUser, request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Screen 5 — review & pay ───────────────────────────────────────────────

    @PostMapping("/step5/review")
    public ResponseEntity<?> step5Review(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FilingDraftRequestDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", 5,
                    "review", submissionService.buildReview(currentUser, request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/step5/payment-intent")
    public ResponseEntity<?> step5PaymentIntent(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> body) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String filingId = body.get("filingId");
            if (filingId == null || filingId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "filingId is required"));
            }
            return ResponseEntity.ok(Map.of(
                    "step", 5,
                    "payment", submissionService.createPaymentIntent(currentUser, filingId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/step5/file")
    public ResponseEntity<?> step5File(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> body) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String filingId = body.get("filingId");
            if (filingId == null || filingId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "filingId is required"));
            }
            return ResponseEntity.ok(Map.of(
                    "step", 6,
                    "filed", submissionService.submitFiling(currentUser, filingId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Screen 6 — filed + schedule ───────────────────────────────────────────

    @GetMapping("/step6/filed/{filingId}")
    public ResponseEntity<?> step6Filed(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String filingId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(Map.of(
                    "step", 6,
                    "filed", submissionService.filedResult(currentUser, filingId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/filings/{filingId}")
    public ResponseEntity<?> getFiling(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String filingId) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(submissionService.getFilingRecord(currentUser, filingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/schedule")
    public ResponseEntity<?> filingSchedule(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(submissionService.filingSchedule(currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> dashboardSummary(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(submissionService.dashboardSummary(currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Shared utilities ──────────────────────────────────────────────────────

    @PostMapping("/estimate-tax")
    public ResponseEntity<?> estimateTax(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FilingFlowEstimateRequestDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(filingFlowService.estimateTax(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/nexus/physical")
    public ResponseEntity<?> createPhysicalNexus(
            @AuthenticationPrincipal User currentUser,
            @RequestBody PhysicalNexusRequestDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(nexusService.createPhysicalNexus(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
