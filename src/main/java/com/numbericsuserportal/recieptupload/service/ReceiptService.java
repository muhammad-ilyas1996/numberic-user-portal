package com.numbericsuserportal.recieptupload.service;

import com.numbericsuserportal.recieptupload.dto.ReceiptDataResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveRequestDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSearch;
import com.numbericsuserportal.recieptupload.dto.ReceiptUploadResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReceiptService {
    
    /**
     * Upload receipt, extract data using OCR, and return response with data and base64 image
     * Note: Data is extracted but NOT saved to ReceiptData table - user can edit and save later
     * 
     * @param file Receipt image file
     * @param userId User ID who uploaded the receipt
     * @return ReceiptUploadResponseDTO with extracted data and base64 encoded image
     */
    ReceiptUploadResponseDTO uploadReceipt(MultipartFile file, Long userId);
    
    /**
     * Upload receipt for manual entry (no OCR extraction)
     * User will manually enter all data
     * 
     * @param file Receipt image file
     * @param userId User ID who uploaded the receipt
     * @return ReceiptUploadResponseDTO with receiptId and base64 image (no extracted data)
     */
    ReceiptUploadResponseDTO uploadReceiptManual(MultipartFile file, Long userId);
    
    /**
     * Save receipt data (user-edited data) to database
     * For manual entry: file + data + entryType + receiptId all saved together
     * For OCR entry: only data is saved (file already uploaded)
     * 
     * @param saveRequest ReceiptSaveRequestDTO with receiptId, data, and entryType
     * @param file MultipartFile (required for MANUAL entry, optional for OCR entry)
     * @return ReceiptSaveResponseDTO with success status
     */
    ReceiptSaveResponseDTO saveReceiptData(ReceiptSaveRequestDTO saveRequest, MultipartFile file);
    
    /**
     * Get all saved receipt data for a user (both MANUAL and OCR)
     * Returns only receipt_data table columns
     * 
     * @param userId User ID
     * @return List of ReceiptDataResponseDTO
     */
    List<ReceiptDataResponseDTO> getUserReceipts(Long userId);
    
    /**
     * Get paginated saved receipt data for a user (both MANUAL and OCR)
     * Returns only receipt_data table columns with pagination (Invoice module style)
     * 
     * @param userId User ID
     * @param search ReceiptSearch DTO with pageNumber, pageSize, fromDate, toDate
     * @return Page of ReceiptDataResponseDTO
     */
    Page<ReceiptDataResponseDTO> getUserReceipts(Long userId, ReceiptSearch search);
}
