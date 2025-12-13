package com.numbericsuserportal.LlcNorthwest.signedforms.controller;

import com.numbericsuserportal.LlcNorthwest.signedforms.dto.SignedFormsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.signedforms.service.SignedFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/llc-northwest/signed-forms")
@CrossOrigin(origins = "*")
public class SignedFormController {

    @Autowired
    private SignedFormService signedFormService;

    /**
     * GET /api/llc-northwest/signed-forms
     * Get signed forms (PDF download URLs) for a filing method and website
     */
    @GetMapping
    public ResponseEntity<?> getSignedForms(
            @RequestParam UUID filingMethodId,
            @RequestParam UUID websiteId) {
        
        try {
            SignedFormsResponseDTO response = signedFormService.getSignedForms(filingMethodId, websiteId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

