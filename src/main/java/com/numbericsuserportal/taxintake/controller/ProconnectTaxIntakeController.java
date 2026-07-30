package com.numbericsuserportal.taxintake.controller;

import com.numbericsuserportal.taxintake.dto.ProconnectTaxIntakeRecordDto;
import com.numbericsuserportal.taxintake.service.ProconnectTaxIntakeService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tax/intake/proconnect")
@CrossOrigin(origins = "*")
public class ProconnectTaxIntakeController {

    @Autowired
    private ProconnectTaxIntakeService intakeService;

    /** Upload PDF/PNG/JPG, run OCR, and save extracted data. */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        try {
            ProconnectTaxIntakeRecordDto dto = intakeService.uploadAndExtract(file, currentUser);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Upload CSV/XLS/XLSX intake sheet and save parsed rows. */
    @PostMapping(value = "/upload-sheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSheet(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        try {
            ProconnectTaxIntakeRecordDto dto = intakeService.uploadSpreadsheet(file, currentUser);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Download CSV intake template. */
    @GetMapping("/template/csv")
    public ResponseEntity<?> downloadCsvTemplate() {
        byte[] bytes = intakeService.buildCsvTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "proconnect_intake_template.csv");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    /** Download Excel intake template. */
    @GetMapping("/template/excel")
    public ResponseEntity<?> downloadExcelTemplate() {
        byte[] bytes = intakeService.buildExcelTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "proconnect_intake_template.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    /** List current user's extracted ProConnect intake records. */
    @GetMapping("/list")
    public ResponseEntity<?> list(@AuthenticationPrincipal User currentUser) {
        try {
            List<ProconnectTaxIntakeRecordDto> rows = intakeService.listMyRecords(currentUser);
            return ResponseEntity.ok(Map.of("count", rows.size(), "records", rows));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** View a single intake record by ID (current user scope). */
    @GetMapping("/{id}")
    public ResponseEntity<?> view(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(intakeService.getRecord(id, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
