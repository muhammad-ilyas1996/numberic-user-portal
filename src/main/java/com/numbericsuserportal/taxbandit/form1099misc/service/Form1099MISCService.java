package com.numbericsuserportal.taxbandit.form1099misc.service;

import com.numbericsuserportal.taxbandit.form1099misc.dto.*;
import java.util.UUID;

/**
 * Service interface for Form 1099-MISC operations.
 */
public interface Form1099MISCService {
    // Core Operations
    CreateForm1099MISCResponseDTO createForm1099MISC(CreateForm1099MISCRequestDTO request);
    UpdateForm1099MISCResponseDTO updateForm1099MISC(UpdateForm1099MISCRequestDTO request);
    TransmitForm1099MISCResponseDTO transmitForm1099MISC(TransmitForm1099MISCRequestDTO request);
    Form1099MISCStatusResponseDTO getForm1099MISCStatus(UUID submissionId);
    
    // Validation
    ValidateForm1099MISCResponseDTO validateForm1099MISC(ValidateForm1099MISCRequestDTO request);
    ValidateForm1099MISCResponseDTO validateForm1099MISC(UUID submissionId);
    
    // Retrieval
    CreateForm1099MISCResponseDTO getForm1099MISC(UUID submissionId, UUID recordId);
    Object listForm1099MISC(UUID submissionId, String businessId);
    
    // PDF Operations
    Object requestPdfURLs(RequestPdfURLsForm1099MISCRequestDTO request);
    Object requestDraftPdfUrl(RequestDraftPdfUrlForm1099MISCRequestDTO request);
    Object getPDF(UUID submissionId, UUID recordId);
    
    // Additional Operations
    Object deleteForm1099MISC(UUID submissionId, UUID recordId);
    Object generateFromTxns(GenerateFromTxnsForm1099MISCRequestDTO request);
    Object approveForm1099MISC(ApproveForm1099MISCRequestDTO request);
    Object getByRecordIds(GetbyRecordIdsForm1099MISCRequestDTO request);
    Object requestDistUrl(RequestDistUrlForm1099MISCRequestDTO request);
    Object uploadAttachment(UploadAttachmentForm1099MISCRequestDTO request);
}



