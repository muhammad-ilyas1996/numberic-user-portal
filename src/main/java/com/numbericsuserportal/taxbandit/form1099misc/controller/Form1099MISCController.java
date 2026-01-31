package com.numbericsuserportal.taxbandit.form1099misc.controller;

import com.numbericsuserportal.taxbandit.exception.TaxBanditsApiException;
import com.numbericsuserportal.taxbandit.form1099misc.dto.*;
import com.numbericsuserportal.taxbandit.form1099misc.service.Form1099MISCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Form 1099-MISC operations.
 */
@RestController
@RequestMapping("/api/taxbandits/form1099misc")
@CrossOrigin(origins = "*")
public class Form1099MISCController {

    @Autowired
    private Form1099MISCService form1099MISCService;

    // ========== Core Operations ==========
    
    /**
     * POST /api/taxbandits/form1099misc/create
     * Create Form 1099-MISC (up to 250 forms per payer in a single request)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createForm1099MISC(@RequestBody CreateForm1099MISCRequestDTO request) {
        try {
            CreateForm1099MISCResponseDTO response = form1099MISCService.createForm1099MISC(request);
            return ResponseEntity.ok(response);
        } catch (TaxBanditsApiException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * PUT /api/taxbandits/form1099misc/update
     * Update existing Form 1099-MISC before transmission
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateForm1099MISC(@RequestBody UpdateForm1099MISCRequestDTO request) {
        try {
            UpdateForm1099MISCResponseDTO response = form1099MISCService.updateForm1099MISC(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099misc/transmit
     * Transmit Form 1099-MISC to IRS electronically
     */
    @PostMapping("/transmit")
    public ResponseEntity<?> transmitForm1099MISC(@RequestBody TransmitForm1099MISCRequestDTO request) {
        try {
            TransmitForm1099MISCResponseDTO response = form1099MISCService.transmitForm1099MISC(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/taxbandits/form1099misc/status?submissionId={submissionId}
     * Get filing status of Form 1099-MISC
     */
    @GetMapping("/status")
    public ResponseEntity<?> getForm1099MISCStatus(@RequestParam UUID submissionId) {
        try {
            Form1099MISCStatusResponseDTO response = form1099MISCService.getForm1099MISCStatus(submissionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== Validation Operations ==========
    
    /**
     * POST /api/taxbandits/form1099misc/validateform
     * Validate form data before creating (field-level validation)
     */
    @PostMapping("/validateform")
    public ResponseEntity<?> validateForm1099MISC(@RequestBody ValidateForm1099MISCRequestDTO request) {
        try {
            ValidateForm1099MISCResponseDTO response = form1099MISCService.validateForm1099MISC(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/taxbandits/form1099misc/validate?submissionId={submissionId}
     * Validate created form before transmission (audit)
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateForm1099MISC(@RequestParam UUID submissionId) {
        try {
            ValidateForm1099MISCResponseDTO response = form1099MISCService.validateForm1099MISC(submissionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== Retrieval Operations ==========
    
    /**
     * GET /api/taxbandits/form1099misc/get?submissionId={submissionId}&recordId={recordId}
     * Get specific Form 1099-MISC details
     */
    @GetMapping("/get")
    public ResponseEntity<?> getForm1099MISC(@RequestParam UUID submissionId, 
                                            @RequestParam(required = false) UUID recordId) {
        try {
            CreateForm1099MISCResponseDTO response = form1099MISCService.getForm1099MISC(submissionId, recordId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/taxbandits/form1099misc/list?submissionId={submissionId}&businessId={businessId}
     * List all Form 1099-MISC forms for a submission or payer
     */
    @GetMapping("/list")
    public ResponseEntity<?> listForm1099MISC(@RequestParam UUID submissionId,
                                             @RequestParam(required = false) String businessId) {
        try {
            Object response = form1099MISCService.listForm1099MISC(submissionId, businessId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/taxbandits/form1099misc/delete?submissionId={submissionId}&recordId={recordId}
     * Delete Form 1099-MISC before transmission
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteForm1099MISC(@RequestParam UUID submissionId,
                                               @RequestParam(required = false) UUID recordId) {
        try {
            Object response = form1099MISCService.deleteForm1099MISC(submissionId, recordId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== PDF Operations ==========
    
    /**
     * POST /api/taxbandits/form1099misc/requestpdfurls
     * Request PDF URLs for single or multiple recipients
     */
    @PostMapping("/requestpdfurls")
    public ResponseEntity<?> requestPdfURLs(@RequestBody RequestPdfURLsForm1099MISCRequestDTO request) {
        try {
            Object response = form1099MISCService.requestPdfURLs(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099misc/requestdraftpdfurl
     * Request draft PDF URL for review (with watermark)
     */
    @PostMapping("/requestdraftpdfurl")
    public ResponseEntity<?> requestDraftPdfUrl(@RequestBody RequestDraftPdfUrlForm1099MISCRequestDTO request) {
        try {
            Object response = form1099MISCService.requestDraftPdfUrl(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/taxbandits/form1099misc/getpdf?submissionId={submissionId}&recordId={recordId}
     * Get PDF link for Form 1099-MISC (requires webhook configuration)
     */
    @GetMapping("/getpdf")
    public ResponseEntity<?> getPDF(@RequestParam UUID submissionId,
                                    @RequestParam(required = false) UUID recordId) {
        try {
            Object response = form1099MISCService.getPDF(submissionId, recordId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== Additional Operations ==========
    
    /**
     * POST /api/taxbandits/form1099misc/generatefromtxns
     * Generate Form 1099-MISC from recorded transactions
     */
    @PostMapping("/generatefromtxns")
    public ResponseEntity<?> generateFromTxns(@RequestBody GenerateFromTxnsForm1099MISCRequestDTO request) {
        try {
            Object response = form1099MISCService.generateFromTxns(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099misc/approve
     * Approve Form 1099-MISC generated from transactions
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveForm1099MISC(@RequestBody ApproveForm1099MISCRequestDTO request) {
        try {
            Object response = form1099MISCService.approveForm1099MISC(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099misc/getbyrecordids
     * Get forms by record IDs (without SubmissionId)
     */
    @PostMapping("/getbyrecordids")
    public ResponseEntity<?> getByRecordIds(@RequestBody GetbyRecordIdsForm1099MISCRequestDTO request) {
        try {
            Object response = form1099MISCService.getByRecordIds(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099misc/requestdisturl
     * Request distribution URL
     */
    @PostMapping("/requestdisturl")
    public ResponseEntity<?> requestDistUrl(@RequestBody RequestDistUrlForm1099MISCRequestDTO request) {
        try {
            Object response = form1099MISCService.requestDistUrl(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/taxbandits/form1099misc/uploadattachment
     * Upload attachment to Form 1099-MISC
     */
    @PostMapping("/uploadattachment")
    public ResponseEntity<?> uploadAttachment(@RequestBody UploadAttachmentForm1099MISCRequestDTO request) {
        try {
            Object response = form1099MISCService.uploadAttachment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}



