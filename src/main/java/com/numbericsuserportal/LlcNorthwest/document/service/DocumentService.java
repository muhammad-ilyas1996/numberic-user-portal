package com.numbericsuserportal.LlcNorthwest.document.service;

import com.numbericsuserportal.LlcNorthwest.document.dto.*;

import java.util.UUID;

/**
 * Service interface for Document operations
 */
public interface DocumentService {
    
    /**
     * Get list of documents with optional filters
     * 
     * @param limit Maximum number of results (default 50, max 200)
     * @param offset Offset to start from (default 0)
     * @param status Filter by status (read, unread)
     * @param start Lower bound for created_at
     * @param stop Upper bound for created_at
     * @param jurisdiction Filter by jurisdiction
     * @param companyId Filter by company ID
     * @return DocumentsResponseDTO
     */
    DocumentsResponseDTO getDocuments(
        Integer limit,
        Integer offset,
        String status,
        String start,
        String stop,
        String jurisdiction,
        UUID companyId
    );
    
    /**
     * Get a specific document by ID
     * 
     * @param id Document UUID
     * @return DocumentResponseDTO
     */
    DocumentResponseDTO getDocumentById(UUID id);
    
    /**
     * Get a specific page of a document as PNG image
     * 
     * @param id Document UUID
     * @param pageNumber 0-indexed page number
     * @param dpi DPI (300, 150, or 72)
     * @return byte array of PNG image
     */
    byte[] getDocumentPage(UUID id, Integer pageNumber, Integer dpi);
    
    /**
     * Download a document as PDF
     * Changes status from unread to read
     * 
     * @param id Document UUID
     * @return byte array of PDF file
     */
    byte[] downloadDocument(UUID id);
    
    /**
     * Get URL of a specific page
     * 
     * @param id Document UUID
     * @param pageNumber 0-indexed page number
     * @param dpi DPI (300, 150, or 72)
     * @return PageUrlResponseDTO
     */
    PageUrlResponseDTO getDocumentPageUrl(UUID id, Integer pageNumber, Integer dpi);
    
    /**
     * Bulk download - get URLs for multiple documents
     * 
     * @param ids Array of document UUIDs
     * @return BulkDownloadResponseDTO
     */
    BulkDownloadResponseDTO bulkDownload(UUID[] ids);
    
    /**
     * Unlock a payment-locked document
     * 
     * @param id Document UUID
     * @param request UnlockDocumentRequestDTO with payment token
     * @return UnlockDocumentResponseDTO
     */
    UnlockDocumentResponseDTO unlockDocument(UUID id, UnlockDocumentRequestDTO request);
}

