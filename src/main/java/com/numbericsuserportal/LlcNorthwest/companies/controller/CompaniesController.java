package com.numbericsuserportal.LlcNorthwest.companies.controller;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.service.CompanyService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/companies")
@CrossOrigin(origins = "*")
public class CompaniesController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public ResponseEntity<?> getCompanies(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String[] names) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            CompaniesResponseDTO response = companyService.fetchAndSaveCompanies(currentUser, limit, offset, names);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCompanies(
            @AuthenticationPrincipal User currentUser,
            @RequestBody CreateCompanyRequestDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            CompaniesResponseDTO response = companyService.createAndSaveCompanies(currentUser, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping
    public ResponseEntity<?> updateCompanies(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateCompanyRequestDTO request) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            CompaniesResponseDTO response = companyService.updateAndSaveCompanies(currentUser, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
