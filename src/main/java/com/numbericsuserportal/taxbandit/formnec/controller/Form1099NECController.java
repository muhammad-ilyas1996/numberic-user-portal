package com.numbericsuserportal.taxbandit.formnec.controller;

import com.numbericsuserportal.taxbandit.auth.TaxBanditsAuthService;
import com.numbericsuserportal.taxbandit.formnec.dto.*;
import com.numbericsuserportal.taxbandit.formnec.service.Form1099NECService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/taxbandits/form1099nec")
@CrossOrigin(origins = "*")
public class Form1099NECController {

    @Autowired
    private Form1099NECService form1099NECService;
    
    @Autowired
    private TaxBanditsAuthService taxBanditsAuthService;

    /**
     * GET /api/taxbandits/form1099nec/test-auth
     * Test TaxBandits OAuth authentication
     */
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuthentication() {
        try {
            String accessToken = taxBanditsAuthService.getAccessToken();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "TaxBandits authentication successful",
                "accessToken", accessToken.substring(0, Math.min(50, accessToken.length())) + "...",
                "tokenLength", accessToken.length()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    // ========== Core Operations ==========

    /**
     * POST /api/taxbandits/form1099nec/create
     * Create Form 1099-NEC (up to 250 forms per payer in a single request)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createForm1099NEC(@RequestBody CreateForm1099NECRequestDTO request) {
        CreateForm1099NECResponseDTO response = form1099NECService.createForm1099NEC(request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/update")
    public ResponseEntity<?> updateForm1099NEC(@RequestBody UpdateForm1099NECRequestDTO request) {
        UpdateForm1099NECResponseDTO response = form1099NECService.updateForm1099NEC(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/taxbandits/form1099nec/transmit
     * Transmit Form 1099-NEC to IRS electronically
     */
    @PostMapping("/transmit")
    public ResponseEntity<?> transmitForm1099NEC(@RequestBody TransmitForm1099NECRequestDTO request) {
        TransmitForm1099NECResponseDTO response = form1099NECService.transmitForm1099NEC(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getForm1099NECStatus(@RequestParam UUID submissionId) {
        Form1099NECStatusResponseDTO response = form1099NECService.getForm1099NECStatus(submissionId);
        return ResponseEntity.ok(response);
    }
    
    // ========== Validation Operations ==========
    
    /**
     * POST /api/taxbandits/form1099nec/validateform
     * Validate form data before creating (field-level validation)
     */
    @PostMapping("/validateform")
    public ResponseEntity<?> validateForm1099NEC(@RequestBody ValidateForm1099NECRequestDTO request) {
        ValidateForm1099NECResponseDTO response = form1099NECService.validateForm1099NEC(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateForm1099NEC(@RequestParam UUID submissionId) {
        ValidateForm1099NECResponseDTO response = form1099NECService.validateForm1099NEC(submissionId);
        return ResponseEntity.ok(response);
    }
    
    // ========== Retrieval Operations ==========
    
    /**
     * GET /api/taxbandits/form1099nec/get?submissionId={submissionId}&recordId={recordId}
     * Get specific Form 1099-NEC details
     */
    @GetMapping("/get")
    public ResponseEntity<?> getForm1099NEC(@RequestParam UUID submissionId, 
                                            @RequestParam(required = false) UUID recordId) {
        CreateForm1099NECResponseDTO response = form1099NECService.getForm1099NEC(submissionId, recordId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> listForm1099NEC(@RequestParam UUID submissionId,
                                             @RequestParam(required = false) String businessId) {
        Object response = form1099NECService.listForm1099NEC(submissionId, businessId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteForm1099NEC(@RequestParam UUID submissionId,
                                               @RequestParam(required = false) UUID recordId) {
        Object response = form1099NECService.deleteForm1099NEC(submissionId, recordId);
        return ResponseEntity.ok(response);
    }
    
    // ========== PDF Operations ==========
    
    /**
     * POST /api/taxbandits/form1099nec/requestpdfurls
     * Request PDF URLs for single or multiple recipients
     */
    @PostMapping("/requestpdfurls")
    public ResponseEntity<?> requestPdfURLs(@RequestBody RequestPdfURLsForm1099NECRequestDTO request) {
        Object response = form1099NECService.requestPdfURLs(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/requestdraftpdfurl")
    public ResponseEntity<?> requestDraftPdfUrl(@RequestBody RequestDraftPdfUrlForm1099NECRequestDTO request) {
        Object response = form1099NECService.requestDraftPdfUrl(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/getpdf")
    public ResponseEntity<?> getPDF(@RequestParam UUID submissionId,
                                    @RequestParam(required = false) UUID recordId) {
        Object response = form1099NECService.getPDF(submissionId, recordId);
        return ResponseEntity.ok(response);
    }
    
    // ========== Additional Operations ==========
    
    /**
     * POST /api/taxbandits/form1099nec/generatefromtxns
     * Generate Form 1099-NEC from recorded transactions
     */
    @PostMapping("/generatefromtxns")
    public ResponseEntity<?> generateFromTxns(@RequestBody GenerateFromTxnsForm1099NECRequestDTO request) {
        Object response = form1099NECService.generateFromTxns(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/approve")
    public ResponseEntity<?> approveForm1099NEC(@RequestBody ApproveForm1099NECRequestDTO request) {
        Object response = form1099NECService.approveForm1099NEC(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/getbyrecordids")
    public ResponseEntity<?> getByRecordIds(@RequestBody GetbyRecordIdsForm1099NECRequestDTO request) {
        Object response = form1099NECService.getByRecordIds(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/requestdisturl")
    public ResponseEntity<?> requestDistUrl(@RequestBody RequestDistUrlForm1099NECRequestDTO request) {
        Object response = form1099NECService.requestDistUrl(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/uploadattachment")
    public ResponseEntity<?> uploadAttachment(@RequestBody UploadAttachmentForm1099NECRequestDTO request) {
        Object response = form1099NECService.uploadAttachment(request);
        return ResponseEntity.ok(response);
    }
}

