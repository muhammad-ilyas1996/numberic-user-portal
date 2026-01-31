package com.numbericsuserportal.taxbandit.form1099k.service;

import com.numbericsuserportal.taxbandit.form1099k.dto.*;
import java.util.UUID;

/**
 * Service interface for Form 1099-K operations.
 */
public interface Form1099KService {
    // Core Operations
    CreateForm1099KResponseDTO createForm1099K(CreateForm1099KRequestDTO request);
    UpdateForm1099KResponseDTO updateForm1099K(UpdateForm1099KRequestDTO request);
    TransmitForm1099KResponseDTO transmitForm1099K(TransmitForm1099KRequestDTO request);
    Form1099KStatusResponseDTO getForm1099KStatus(UUID submissionId);
    
    // Validation
    ValidateForm1099KResponseDTO validateForm1099K(ValidateForm1099KRequestDTO request);
    ValidateForm1099KResponseDTO validateForm1099K(UUID submissionId);
    
    // Retrieval
    CreateForm1099KResponseDTO getForm1099K(UUID submissionId, UUID recordId);
    Object listForm1099K(UUID submissionId, String businessId);
    
    // PDF Operations
    Object requestPdfURLs(RequestPdfURLsForm1099KRequestDTO request);
    Object requestDraftPdfUrl(RequestDraftPdfUrlForm1099KRequestDTO request);
    Object getPDF(UUID submissionId, UUID recordId);
    
    // Additional Operations
    Object deleteForm1099K(UUID submissionId, UUID recordId);
    Object generateFromTxns(GenerateFromTxnsForm1099KRequestDTO request);
    Object approveForm1099K(ApproveForm1099KRequestDTO request);
    Object getByRecordIds(GetbyRecordIdsForm1099KRequestDTO request);
    Object requestDistUrl(RequestDistUrlForm1099KRequestDTO request);
    Object uploadAttachment(UploadAttachmentForm1099KRequestDTO request);
}

