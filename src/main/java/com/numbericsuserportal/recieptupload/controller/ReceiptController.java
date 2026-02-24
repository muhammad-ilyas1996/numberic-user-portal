package com.numbericsuserportal.recieptupload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.recieptupload.dto.ReceiptDataResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveRequestDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSearch;
import com.numbericsuserportal.recieptupload.dto.ReceiptUploadResponseDTO;
import com.numbericsuserportal.recieptupload.service.ReceiptService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receipt")
@CrossOrigin(origins = "*")
public class ReceiptController {
    
    @Autowired
    private ReceiptService receiptService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * POST /api/receipt/upload
     * Upload receipt image, extract data using OCR, and return response with data and base64 image
     * Note: Data is extracted but NOT saved - user can edit and save using /save endpoint
     * 
     * @param file Receipt image file (JPEG or PNG)
     * @param userId User ID (optional, can be extracted from auth context)
     * @return ReceiptUploadResponseDTO with extracted data and base64 encoded image
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadReceipt(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId) {
        
        try {
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "User ID is required"));
            }
            
            ReceiptUploadResponseDTO response = receiptService.uploadReceipt(file, userId);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/receipt/upload-manual
     * Upload receipt image for manual entry (no OCR extraction)
     * User will manually enter all data
     * 
     * @param file Receipt image file (JPEG or PNG)
     * @param userId User ID (optional, can be extracted from auth context)
     * @return ReceiptUploadResponseDTO with receiptId and base64 image (no extracted data)
     */
    @PostMapping("/upload-manual")
    public ResponseEntity<?> uploadReceiptManual(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId) {
        
        try {
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "User ID is required"));
            }
            
            ReceiptUploadResponseDTO response = receiptService.uploadReceiptManual(file, userId);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/receipt/save
     * Save receipt data to database
     * For manual entry: file + data + entryType + receiptId all saved together
     * For OCR entry: only data is saved (file already uploaded)
     * 
     * Accepts both:
     * 1. multipart/form-data with file and individual fields
     * 2. multipart/form-data with file and saveRequest (JSON string)
     * 
     * @param file MultipartFile (required for MANUAL entry, optional for OCR entry)
     * @param saveRequestJson JSON string of ReceiptSaveRequestDTO (if using JSON approach)
     * @param receiptId Receipt ID (if using individual fields approach)
     * @param userId User ID (if using individual fields approach)
     * @param entryType Entry type: "MANUAL" or "OCR" (required)
     * @param merchantName Merchant name
     * @param date Date in format YYYY-MM-DD
     * @param totalAmount Total amount as string
     * @param taxAmount Tax amount as string
     * @param category Category
     * @param status Status
     * @return ReceiptSaveResponseDTO with success status
     */
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveReceipt(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "saveRequest", required = false) String saveRequestJson,
            @RequestParam(value = "receiptId", required = false) Long receiptId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "entryType", required = false) String entryType,
            @RequestParam(value = "merchantName", required = false) String merchantName,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "totalAmount", required = false) String totalAmount,
            @RequestParam(value = "taxAmount", required = false) String taxAmount,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status) {
        
        try {
            ReceiptSaveRequestDTO saveRequest;
            
            // Parse request - either from JSON or individual fields
            if (saveRequestJson != null && !saveRequestJson.isEmpty()) {
                // Parse from JSON string
                saveRequest = objectMapper.readValue(saveRequestJson, ReceiptSaveRequestDTO.class);
            } else {
                // Build from individual fields
                saveRequest = new ReceiptSaveRequestDTO();
                saveRequest.setReceiptId(receiptId);
                saveRequest.setUserId(userId);
                saveRequest.setEntryType(entryType);
                saveRequest.setMerchantName(merchantName);
                
                // Parse date
                if (date != null && !date.isEmpty()) {
                    try {
                        saveRequest.setDate(LocalDate.parse(date));
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "error", "Invalid date format. Use YYYY-MM-DD"));
                    }
                }
                
                // Parse amounts
                if (totalAmount != null && !totalAmount.isEmpty()) {
                    try {
                        saveRequest.setTotalAmount(new BigDecimal(totalAmount));
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "error", "Invalid totalAmount format"));
                    }
                }
                
                if (taxAmount != null && !taxAmount.isEmpty()) {
                    try {
                        saveRequest.setTaxAmount(new BigDecimal(taxAmount));
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "error", "Invalid taxAmount format"));
                    }
                }
                
                saveRequest.setCategory(category);
                saveRequest.setStatus(status);
            }
            
            // Validate entryType
            if (saveRequest.getEntryType() == null || saveRequest.getEntryType().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Entry Type is required (MANUAL or OCR)"));
            }
            
            if (!saveRequest.getEntryType().equals("MANUAL") && !saveRequest.getEntryType().equals("OCR")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Entry Type must be MANUAL or OCR"));
            }
            
            // For manual entry, file is required
            if ("MANUAL".equals(saveRequest.getEntryType()) && (file == null || file.isEmpty())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "File is required for manual entry"));
            }
            
            // For OCR entry, receiptId is required
            if ("OCR".equals(saveRequest.getEntryType()) && saveRequest.getReceiptId() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Receipt ID is required for OCR entry"));
            }
            
            ReceiptSaveResponseDTO response = receiptService.saveReceiptData(saveRequest, file);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * POST /api/receipt/my-receipts
     * Get paginated saved receipt data for current logged-in user (both MANUAL and OCR)
     * Returns only receipt_data table columns with pagination (Invoice module style)
     * 
     * @param currentUser Current authenticated user from JWT token
     * @param search ReceiptSearch DTO with pageNumber, pageSize, fromDate, toDate
     * @return Page of ReceiptDataResponseDTO (Invoice module style)
     */
    @PostMapping("/my-receipts")
    public ResponseEntity<Page<ReceiptDataResponseDTO>> getMyReceipts(
            @RequestBody ReceiptSearch search,
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<ReceiptDataResponseDTO> receiptsPage = receiptService.getUserReceipts(
            currentUser.getUserId(), 
            search
        );
        
        return ResponseEntity.ok(receiptsPage);
    }
}
