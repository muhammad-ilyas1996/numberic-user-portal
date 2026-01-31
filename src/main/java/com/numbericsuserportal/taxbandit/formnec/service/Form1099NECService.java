package com.numbericsuserportal.taxbandit.formnec.service;

import com.numbericsuserportal.taxbandit.formnec.dto.*;
import java.util.UUID;

public interface Form1099NECService {
    // Core Operations
    CreateForm1099NECResponseDTO createForm1099NEC(CreateForm1099NECRequestDTO request);
    UpdateForm1099NECResponseDTO updateForm1099NEC(UpdateForm1099NECRequestDTO request);
    TransmitForm1099NECResponseDTO transmitForm1099NEC(TransmitForm1099NECRequestDTO request);
    Form1099NECStatusResponseDTO getForm1099NECStatus(UUID submissionId);
    
    // Validation
    ValidateForm1099NECResponseDTO validateForm1099NEC(ValidateForm1099NECRequestDTO request);
    ValidateForm1099NECResponseDTO validateForm1099NEC(UUID submissionId);
    
    // Retrieval
    CreateForm1099NECResponseDTO getForm1099NEC(UUID submissionId, UUID recordId);
    Object listForm1099NEC(UUID submissionId, String businessId);
    
    // PDF Operations
    Object requestPdfURLs(RequestPdfURLsForm1099NECRequestDTO request);
    Object requestDraftPdfUrl(RequestDraftPdfUrlForm1099NECRequestDTO request);
    Object getPDF(UUID submissionId, UUID recordId);
    
    // Additional Operations
    Object deleteForm1099NEC(UUID submissionId, UUID recordId);
    Object generateFromTxns(GenerateFromTxnsForm1099NECRequestDTO request);
    Object approveForm1099NEC(ApproveForm1099NECRequestDTO request);
    Object getByRecordIds(GetbyRecordIdsForm1099NECRequestDTO request);
    Object requestDistUrl(RequestDistUrlForm1099NECRequestDTO request);
    Object uploadAttachment(UploadAttachmentForm1099NECRequestDTO request);
}

