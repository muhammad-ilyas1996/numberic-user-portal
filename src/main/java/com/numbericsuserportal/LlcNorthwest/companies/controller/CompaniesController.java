package com.numbericsuserportal.LlcNorthwest.companies.controller;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/llc-northwest/companies")
@CrossOrigin(origins = "*")
public class CompaniesController {

    @Autowired
    private CompanyService companyService;

    /**
     * GET /api/llc-northwest/companies
     * Fetch from API and save to database, then return
     */
    @GetMapping
    public ResponseEntity<?> getCompanies(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String[] names) {
        
        try {
            CompaniesResponseDTO response = companyService.fetchAndSaveCompanies(limit, offset, names);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/llc-northwest/companies/database
     * Get companies from local database only
     */
    @GetMapping("/database")
    public ResponseEntity<?> getCompaniesFromDatabase() {
        try {
            CompaniesResponseDTO response = companyService.getCompaniesFromDatabase();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/llc-northwest/companies/{id}
     * Get a specific company by ID from database
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable UUID id) {
        try {
            CompaniesResponseDTO response = companyService.getCompanyByIdFromDatabase(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/llc-northwest/companies
     * Create new companies via API and save to database
     */
    @PostMapping
    public ResponseEntity<?> createCompanies(@RequestBody CreateCompanyRequestDTO request) {
        try {
            CompaniesResponseDTO response = companyService.createAndSaveCompanies(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/llc-northwest/companies
     * Update companies via API and save to database
     */
    @PatchMapping
    public ResponseEntity<?> updateCompanies(@RequestBody UpdateCompanyRequestDTO request) {
        try {
            CompaniesResponseDTO response = companyService.updateAndSaveCompanies(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

