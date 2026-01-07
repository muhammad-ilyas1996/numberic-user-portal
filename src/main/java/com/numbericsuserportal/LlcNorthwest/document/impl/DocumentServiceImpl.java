package com.numbericsuserportal.LlcNorthwest.document.impl;

import com.numbericsuserportal.LlcNorthwest.document.dto.*;
import com.numbericsuserportal.LlcNorthwest.document.service.DocumentService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service implementation for Document operations
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    public DocumentsResponseDTO getDocuments(
            Integer limit,
            Integer offset,
            String status,
            String start,
            String stop,
            String jurisdiction,
            UUID companyId) {
        return corporateToolsApiService.getDocuments(limit, offset, status, start, stop, jurisdiction, companyId);
    }

    @Override
    public DocumentResponseDTO getDocumentById(UUID id) {
        return corporateToolsApiService.getDocumentById(id);
    }

    @Override
    public byte[] getDocumentPage(UUID id, Integer pageNumber, Integer dpi) {
        return corporateToolsApiService.getDocumentPage(id, pageNumber, dpi);
    }

    @Override
    public byte[] downloadDocument(UUID id) {
        return corporateToolsApiService.downloadDocument(id);
    }

    @Override
    public PageUrlResponseDTO getDocumentPageUrl(UUID id, Integer pageNumber, Integer dpi) {
        return corporateToolsApiService.getDocumentPageUrl(id, pageNumber, dpi);
    }

    @Override
    public BulkDownloadResponseDTO bulkDownload(UUID[] ids) {
        return corporateToolsApiService.bulkDownloadDocuments(ids);
    }

    @Override
    public UnlockDocumentResponseDTO unlockDocument(UUID id, UnlockDocumentRequestDTO request) {
        return corporateToolsApiService.unlockDocument(id, request);
    }
}

