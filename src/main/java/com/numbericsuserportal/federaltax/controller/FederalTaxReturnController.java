package com.numbericsuserportal.federaltax.controller;

import com.numbericsuserportal.federaltax.dto.*;
import com.numbericsuserportal.federaltax.service.FederalTaxReturnService;
import com.numbericsuserportal.usermanagement.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Federal Income Tax Phase 1 APIs — intake, organizer, assign/export, 8879.
 * Client responses never include preparer names.
 */
@RestController
@RequestMapping("/api/tax/federal")
@CrossOrigin(origins = "*")
public class FederalTaxReturnController {

    @Autowired
    private FederalTaxReturnService federalTaxReturnService;

    @GetMapping("/menu")
    public ResponseEntity<?> menu() {
        return ResponseEntity.ok(Map.of("items", federalTaxReturnService.taxModuleMenu()));
    }

    @GetMapping("/export-format-notes")
    public ResponseEntity<?> exportFormatNotes() {
        return ResponseEntity.ok(federalTaxReturnService.exportFormatNotes());
    }

    @PostMapping("/cases")
    public ResponseEntity<?> createCase(@AuthenticationPrincipal User user,
            @RequestBody CreateFederalTaxCaseRequest request) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.createCase(user, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cases")
    public ResponseEntity<?> listCases(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(Map.of("cases", federalTaxReturnService.listMyCases(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cases/{publicCaseId}")
    public ResponseEntity<?> getCase(@AuthenticationPrincipal User user, @PathVariable String publicCaseId) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.getCaseForTaxpayer(user, publicCaseId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/cases/{publicCaseId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDoc(@AuthenticationPrincipal User user,
            @PathVariable String publicCaseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "via", required = false, defaultValue = "WEB") String via) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.uploadDocument(user, publicCaseId, file, via));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/cases/{publicCaseId}/organizer")
    public ResponseEntity<?> updateOrganizer(@AuthenticationPrincipal User user,
            @PathVariable String publicCaseId,
            @RequestBody UpdateOrganizerRequest request) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.updateOrganizer(user, publicCaseId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cases/{publicCaseId}/service-option")
    public ResponseEntity<?> serviceOption(@AuthenticationPrincipal User user,
            @PathVariable String publicCaseId,
            @RequestBody SelectServiceOptionRequest request) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.selectServiceOption(user, publicCaseId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cases/{publicCaseId}/clarification-response")
    public ResponseEntity<?> clarificationResponse(@AuthenticationPrincipal User user,
            @PathVariable String publicCaseId,
            @RequestBody ClarificationRequest request) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.respondClarification(user, publicCaseId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cases/{publicCaseId}/sign-8879")
    public ResponseEntity<?> sign8879(@AuthenticationPrincipal User user,
            @PathVariable String publicCaseId,
            @RequestBody Sign8879Request request,
            HttpServletRequest http) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.sign8879(
                    user, publicCaseId, request, http.getRemoteAddr(), http.getHeader("User-Agent")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Preparer / reviewer ---

    @PostMapping("/preparers")
    public ResponseEntity<?> upsertPreparer(@AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {
        try {
            Long preparerUserId = Long.valueOf(String.valueOf(body.get("preparerUserId")));
            String name = body.get("displayName") != null ? String.valueOf(body.get("displayName")) : "Preparer";
            int max = body.get("maxOpenCases") != null ? Integer.parseInt(String.valueOf(body.get("maxOpenCases"))) : 25;
            String quals = body.get("qualifications") != null ? String.valueOf(body.get("qualifications")) : null;
            return ResponseEntity.ok(federalTaxReturnService.upsertPreparer(user, preparerUserId, name, max, quals));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/preparer/cases")
    public ResponseEntity<?> preparerCases(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(Map.of("cases", federalTaxReturnService.listAssignedToPreparer(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/preparer/cases/{publicCaseId}/request-clarification")
    public ResponseEntity<?> requestClarification(@AuthenticationPrincipal User user,
            @PathVariable String publicCaseId,
            @RequestBody ClarificationRequest request) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.requestClarification(user, publicCaseId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/preparer/cases/{publicCaseId}/in-review")
    public ResponseEntity<?> inReview(@AuthenticationPrincipal User user, @PathVariable String publicCaseId) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.markInReview(user, publicCaseId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reviewer/cases/{publicCaseId}/approve")
    public ResponseEntity<?> approve(@AuthenticationPrincipal User user, @PathVariable String publicCaseId) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.reviewerApprove(user, publicCaseId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/ops/cases/{publicCaseId}/mark-filed")
    public ResponseEntity<?> markFiled(@AuthenticationPrincipal User user,
            @PathVariable String publicCaseId,
            @RequestParam(defaultValue = "false") boolean accepted) {
        try {
            return ResponseEntity.ok(federalTaxReturnService.markFiled(user, publicCaseId, accepted));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
