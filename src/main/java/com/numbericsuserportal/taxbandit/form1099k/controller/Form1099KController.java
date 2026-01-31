package com.numbericsuserportal.taxbandit.form1099k.controller;

import com.numbericsuserportal.taxbandit.exception.TaxBanditsApiException;
import com.numbericsuserportal.taxbandit.form1099k.dto.*;
import com.numbericsuserportal.taxbandit.form1099k.service.Form1099KService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Form 1099-K operations.
 */
@RestController
@RequestMapping("/api/taxbandits/form1099k")
@CrossOrigin(origins = "*")
public class Form1099KController {

    @Autowired
    private Form1099KService form1099KService;

    // ========== Core Operations ==========
    
    /**
     * POST /api/taxbandits/form1099k/create
     * Create Form 1099-K (up to 250 forms per payer in a single request)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createForm1099K(@RequestBody CreateForm1099KRequestDTO request) {
        try {
            CreateForm1099KResponseDTO response = form1099KService.createForm1099K(request);
            return ResponseEntity.ok(response);
        } catch (TaxBanditsApiException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * PUT /api/taxbandits/form1099k/update
     * Update existing Form 1099-K before transmission
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateForm1099K(@RequestBody UpdateForm1099KRequestDTO request) {
        try {
            UpdateForm1099KResponseDTO response = form1099KService.updateForm1099K(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099k/transmit
     * Transmit Form 1099-K to IRS electronically
     */
    @PostMapping("/transmit")
    public ResponseEntity<?> transmitForm1099K(@RequestBody TransmitForm1099KRequestDTO request) {
        try {
            TransmitForm1099KResponseDTO response = form1099KService.transmitForm1099K(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/taxbandits/form1099k/status?submissionId={submissionId}
     * Get filing status of Form 1099-K
     */
    @GetMapping("/status")
    public ResponseEntity<?> getForm1099KStatus(@RequestParam UUID submissionId) {
        try {
            Form1099KStatusResponseDTO response = form1099KService.getForm1099KStatus(submissionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== Validation Operations ==========
    
    /**
     * POST /api/taxbandits/form1099k/validateform
     * Validate form data before creating (field-level validation)
     */
    @PostMapping("/validateform")
    public ResponseEntity<?> validateForm1099K(@RequestBody ValidateForm1099KRequestDTO request) {
        try {
            ValidateForm1099KResponseDTO response = form1099KService.validateForm1099K(request);
            return ResponseEntity.ok(response);
        } catch (TaxBanditsApiException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/taxbandits/form1099k/validate?submissionId={submissionId}
     * Validate created form before transmission (audit)
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateForm1099K(@RequestParam UUID submissionId) {
        try {
            ValidateForm1099KResponseDTO response = form1099KService.validateForm1099K(submissionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== Retrieval Operations ==========
    
    /**
     * GET /api/taxbandits/form1099k/get?submissionId={submissionId}&recordId={recordId}
     * Get specific Form 1099-K details
     */
    @GetMapping("/get")
    public ResponseEntity<?> getForm1099K(@RequestParam UUID submissionId, 
                                          @RequestParam(required = false) UUID recordId) {
        try {
            CreateForm1099KResponseDTO response = form1099KService.getForm1099K(submissionId, recordId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/taxbandits/form1099k/list?submissionId={submissionId}&businessId={businessId}
     * List all Form 1099-K forms for a submission or payer
     */
    @GetMapping("/list")
    public ResponseEntity<?> listForm1099K(@RequestParam UUID submissionId,
                                           @RequestParam(required = false) String businessId) {
        try {
            Object response = form1099KService.listForm1099K(submissionId, businessId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/taxbandits/form1099k/delete?submissionId={submissionId}&recordId={recordId}
     * Delete Form 1099-K before transmission
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteForm1099K(@RequestParam UUID submissionId,
                                             @RequestParam(required = false) UUID recordId) {
        try {
            Object response = form1099KService.deleteForm1099K(submissionId, recordId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== PDF Operations ==========
    
    /**
     * POST /api/taxbandits/form1099k/requestpdfurls
     * Request PDF URLs for single or multiple recipients
     */
    @PostMapping("/requestpdfurls")
    public ResponseEntity<?> requestPdfURLs(@RequestBody RequestPdfURLsForm1099KRequestDTO request) {
        try {
            Object response = form1099KService.requestPdfURLs(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099k/requestdraftpdfurl
     * Request draft PDF URL for review (with watermark)
     */
    @PostMapping("/requestdraftpdfurl")
    public ResponseEntity<?> requestDraftPdfUrl(@RequestBody RequestDraftPdfUrlForm1099KRequestDTO request) {
        try {
            Object response = form1099KService.requestDraftPdfUrl(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/taxbandits/form1099k/getpdf?submissionId={submissionId}&recordId={recordId}
     * Get PDF link for Form 1099-K (requires webhook configuration)
     */
    @GetMapping("/getpdf")
    public ResponseEntity<?> getPDF(@RequestParam UUID submissionId,
                                    @RequestParam(required = false) UUID recordId) {
        try {
            Object response = form1099KService.getPDF(submissionId, recordId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== Additional Operations ==========
    
    /**
     * POST /api/taxbandits/form1099k/generatefromtxns
     * Generate Form 1099-K from recorded transactions
     */
    @PostMapping("/generatefromtxns")
    public ResponseEntity<?> generateFromTxns(@RequestBody GenerateFromTxnsForm1099KRequestDTO request) {
        try {
            Object response = form1099KService.generateFromTxns(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099k/approve
     * Approve Form 1099-K generated from transactions
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveForm1099K(@RequestBody ApproveForm1099KRequestDTO request) {
        try {
            Object response = form1099KService.approveForm1099K(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099k/getbyrecordids
     * Get forms by record IDs (without SubmissionId)
     */
    @PostMapping("/getbyrecordids")
    public ResponseEntity<?> getByRecordIds(@RequestBody GetbyRecordIdsForm1099KRequestDTO request) {
        try {
            Object response = form1099KService.getByRecordIds(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099k/requestdisturl
     * Request distribution URL
     */
    @PostMapping("/requestdisturl")
    public ResponseEntity<?> requestDistUrl(@RequestBody RequestDistUrlForm1099KRequestDTO request) {
        try {
            Object response = form1099KService.requestDistUrl(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099k/uploadattachment
     * Upload attachment to Form 1099-K
     */
    @PostMapping("/uploadattachment")
    public ResponseEntity<?> uploadAttachment(@RequestBody UploadAttachmentForm1099KRequestDTO request) {
        try {
            Object response = form1099KService.uploadAttachment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

