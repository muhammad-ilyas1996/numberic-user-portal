package com.numbericsuserportal.invoice.controller;

import com.numbericsuserportal.invoice.dto.RecurringInvoiceCreateRequestDto;
import com.numbericsuserportal.invoice.dto.RecurringInvoiceDto;
import com.numbericsuserportal.invoice.dto.RecurringInvoiceSearch;
import com.numbericsuserportal.invoice.service.RecurringInvoiceService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/invoice/recurring")
public class RecurringInvoiceController {

    @Autowired
    private RecurringInvoiceService recurringInvoiceService;

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestBody RecurringInvoiceCreateRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        try {
            RecurringInvoiceDto created = recurringInvoiceService.create(request, currentUser);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody RecurringInvoiceCreateRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        try {
            RecurringInvoiceDto updated = recurringInvoiceService.update(id, request, currentUser);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/list")
    public ResponseEntity<Page<RecurringInvoiceDto>> list(
            @RequestBody RecurringInvoiceSearch search,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(recurringInvoiceService.list(search));
    }

    /** Get recurring profile by id (path). */
    @GetMapping("/{id}")
    public ResponseEntity<?> getByIdPath(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            RecurringInvoiceDto dto = recurringInvoiceService.getById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /** Get recurring profile by id (POST body, for compatibility). */
    @PostMapping("/view-detail")
    public ResponseEntity<?> getByIdBody(
            @RequestBody java.util.Map<String, Long> body,
            @AuthenticationPrincipal User currentUser) {
        Long id = body != null ? body.get("id") : null;
        if (id == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "id is required"));
        }
        try {
            RecurringInvoiceDto dto = recurringInvoiceService.getById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<?> pause(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            recurringInvoiceService.pause(id, currentUser);
            return ResponseEntity.ok(java.util.Map.of("message", "Recurring invoice paused"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<?> resume(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            recurringInvoiceService.resume(id, currentUser);
            return ResponseEntity.ok(java.util.Map.of("message", "Recurring invoice resumed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<?> stop(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            recurringInvoiceService.stop(id, currentUser);
            return ResponseEntity.ok(java.util.Map.of("message", "Recurring invoice stopped"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
