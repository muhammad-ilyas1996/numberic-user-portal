package com.numbericsuserportal.LlcNorthwest.document.controller;

import com.numbericsuserportal.LlcNorthwest.document.dto.*;
import com.numbericsuserportal.LlcNorthwest.document.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Document operations
 */
@RestController
@RequestMapping("/api/llc-northwest/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * GET /api/llc-northwest/documents
     * Returns a list of documents with optional filters
     */
    @GetMapping
    public ResponseEntity<?> getDocuments(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String stop,
            @RequestParam(required = false) String jurisdiction,
            @RequestParam(required = false, name = "company_id") UUID companyId) {
        try {
            DocumentsResponseDTO response = documentService.getDocuments(
                limit, offset, status, start, stop, jurisdiction, companyId
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /api/llc-northwest/documents/{id}
     * Returns a specific document by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable UUID id) {
        try {
            DocumentResponseDTO response = documentService.getDocumentById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /api/llc-northwest/documents/{id}/page/{pageNumber}
     * Returns a specific page of a document as PNG image
     */
    @GetMapping("/{id}/page/{pageNumber}")
    public ResponseEntity<?> getDocumentPage(
            @PathVariable UUID id,
            @PathVariable Integer pageNumber,
            @RequestParam(required = false) Integer dpi) {
        try {
            byte[] imageData = documentService.getDocumentPage(id, pageNumber, dpi);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageData.length);
            
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /api/llc-northwest/documents/{id}/download
     * Download a document as PDF
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadDocument(@PathVariable UUID id) {
        try {
            byte[] pdfData = documentService.downloadDocument(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfData.length);
            headers.setContentDispositionFormData("attachment", "document-" + id + ".pdf");
            
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /api/llc-northwest/documents/{id}/page/{pageNumber}/url
     * Returns the URL of the specified page
     */
    @GetMapping("/{id}/page/{pageNumber}/url")
    public ResponseEntity<?> getDocumentPageUrl(
            @PathVariable UUID id,
            @PathVariable Integer pageNumber,
            @RequestParam(required = false) Integer dpi) {
        try {
            PageUrlResponseDTO response = documentService.getDocumentPageUrl(id, pageNumber, dpi);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * GET /api/llc-northwest/documents/bulk-download
     * Returns URLs for multiple documents
     */
    @GetMapping("/bulk-download")
    public ResponseEntity<?> bulkDownload(@RequestParam UUID[] ids) {
        try {
            BulkDownloadResponseDTO response = documentService.bulkDownload(ids);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * POST /api/llc-northwest/documents/unlock/{id}
     * Unlocks a document after receiving payment
     */
    @PostMapping("/unlock/{id}")
    public ResponseEntity<?> unlockDocument(
            @PathVariable UUID id,
            @Valid @RequestBody UnlockDocumentRequestDTO request) {
        try {
            UnlockDocumentResponseDTO response = documentService.unlockDocument(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}

