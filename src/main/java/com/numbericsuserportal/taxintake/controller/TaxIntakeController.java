package com.numbericsuserportal.taxintake.controller;

import com.numbericsuserportal.taxintake.dto.TaxIntakeRecordDto;
import com.numbericsuserportal.taxintake.dto.TaxSoftwareDto;
import com.numbericsuserportal.taxintake.service.TaxIntakeService;
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
import java.util.Locale;
import java.util.Map;

/**
 * Multi-software tax intake: ProConnect, Drake, ProSeries, TaxWise, CCH Axcess, Lacerte.
 * Same pattern for all: PDF/OCR upload and/or CSV-Excel sheet upload + list/view.
 */
@RestController
@RequestMapping("/api/tax/intake")
@CrossOrigin(origins = "*")
public class TaxIntakeController {

    @Autowired
    private TaxIntakeService intakeService;

    /** Catalog of supported tax softwares (formats, API status, notes). */
    @GetMapping("/softwares")
    public ResponseEntity<?> listSoftwares() {
        List<TaxSoftwareDto> softwares = intakeService.listSoftwares();
        return ResponseEntity.ok(Map.of("count", softwares.size(), "softwares", softwares));
    }

    @GetMapping("/{software}/info")
    public ResponseEntity<?> softwareInfo(@PathVariable String software) {
        try {
            return ResponseEntity.ok(intakeService.getSoftware(software));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/{software}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @PathVariable String software,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(intakeService.uploadDocument(software, file, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/{software}/upload-sheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSheet(
            @PathVariable String software,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(intakeService.uploadSpreadsheet(software, file, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{software}/template/csv")
    public ResponseEntity<?> downloadCsvTemplate(@PathVariable String software) {
        try {
            byte[] bytes = intakeService.buildCsvTemplate(software);
            String filename = software.toLowerCase(Locale.ROOT) + "_intake_template.csv";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(new ByteArrayResource(bytes));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{software}/template/excel")
    public ResponseEntity<?> downloadExcelTemplate(@PathVariable String software) {
        try {
            byte[] bytes = intakeService.buildExcelTemplate(software);
            String filename = software.toLowerCase(Locale.ROOT) + "_intake_template.xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok().headers(headers).contentLength(bytes.length).body(new ByteArrayResource(bytes));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{software}/list")
    public ResponseEntity<?> list(
            @PathVariable String software,
            @AuthenticationPrincipal User currentUser) {
        try {
            List<TaxIntakeRecordDto> rows = intakeService.listRecords(software, currentUser);
            return ResponseEntity.ok(Map.of("count", rows.size(), "records", rows));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{software}/{id}")
    public ResponseEntity<?> view(
            @PathVariable String software,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(intakeService.getRecord(software, id, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
