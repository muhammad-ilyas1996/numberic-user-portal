package com.numbericsuserportal.taxbandit.service;

import com.numbericsuserportal.taxbandit.formnec.dto.*;
import com.numbericsuserportal.taxbandit.form1099misc.dto.*;
import com.numbericsuserportal.taxbandit.form1099k.dto.*;
import java.util.UUID;

public interface TaxBanditsApiService {
    // 1099-NEC Form Operations
    CreateForm1099NECResponseDTO createForm1099NEC(CreateForm1099NECRequestDTO request);
    UpdateForm1099NECResponseDTO updateForm1099NEC(UpdateForm1099NECRequestDTO request);
    TransmitForm1099NECResponseDTO transmitForm1099NEC(TransmitForm1099NECRequestDTO request);
    Form1099NECStatusResponseDTO getForm1099NECStatus(UUID submissionId);
    ValidateForm1099NECResponseDTO validateForm1099NEC(ValidateForm1099NECRequestDTO request);
    ValidateForm1099NECResponseDTO validateForm1099NEC(UUID submissionId);
    CreateForm1099NECResponseDTO getForm1099NEC(UUID submissionId, UUID recordId);
    Object listForm1099NEC(UUID submissionId, String businessId);
    Object deleteForm1099NEC(UUID submissionId, UUID recordId);
    Object requestPdfURLsForm1099NEC(RequestPdfURLsForm1099NECRequestDTO request);
    Object requestDraftPdfUrlForm1099NEC(RequestDraftPdfUrlForm1099NECRequestDTO request);
    Object getPDFForm1099NEC(UUID submissionId, UUID recordId);
    Object generateFromTxnsForm1099NEC(GenerateFromTxnsForm1099NECRequestDTO request);
    Object approveForm1099NEC(ApproveForm1099NECRequestDTO request);
    Object getByRecordIdsForm1099NEC(GetbyRecordIdsForm1099NECRequestDTO request);
    Object requestDistUrlForm1099NEC(RequestDistUrlForm1099NECRequestDTO request);
    Object uploadAttachmentForm1099NEC(UploadAttachmentForm1099NECRequestDTO request);
    
    // 1099-MISC Form Operations
    CreateForm1099MISCResponseDTO createForm1099MISC(CreateForm1099MISCRequestDTO request);
    UpdateForm1099MISCResponseDTO updateForm1099MISC(UpdateForm1099MISCRequestDTO request);
    TransmitForm1099MISCResponseDTO transmitForm1099MISC(TransmitForm1099MISCRequestDTO request);
    Form1099MISCStatusResponseDTO getForm1099MISCStatus(UUID submissionId);
    ValidateForm1099MISCResponseDTO validateForm1099MISC(ValidateForm1099MISCRequestDTO request);
    ValidateForm1099MISCResponseDTO validateForm1099MISC(UUID submissionId);
    CreateForm1099MISCResponseDTO getForm1099MISC(UUID submissionId, UUID recordId);
    Object listForm1099MISC(UUID submissionId, String businessId);
    Object deleteForm1099MISC(UUID submissionId, UUID recordId);
    Object requestPdfURLsForm1099MISC(RequestPdfURLsForm1099MISCRequestDTO request);
    Object requestDraftPdfUrlForm1099MISC(RequestDraftPdfUrlForm1099MISCRequestDTO request);
    Object getPDFForm1099MISC(UUID submissionId, UUID recordId);
    Object generateFromTxnsForm1099MISC(GenerateFromTxnsForm1099MISCRequestDTO request);
    Object approveForm1099MISC(ApproveForm1099MISCRequestDTO request);
    Object getByRecordIdsForm1099MISC(GetbyRecordIdsForm1099MISCRequestDTO request);
    Object requestDistUrlForm1099MISC(RequestDistUrlForm1099MISCRequestDTO request);
    Object uploadAttachmentForm1099MISC(UploadAttachmentForm1099MISCRequestDTO request);
    
    // 1099-K Form Operations
    CreateForm1099KResponseDTO createForm1099K(CreateForm1099KRequestDTO request);
    UpdateForm1099KResponseDTO updateForm1099K(UpdateForm1099KRequestDTO request);
    TransmitForm1099KResponseDTO transmitForm1099K(TransmitForm1099KRequestDTO request);
    Form1099KStatusResponseDTO getForm1099KStatus(UUID submissionId);
    ValidateForm1099KResponseDTO validateForm1099K(ValidateForm1099KRequestDTO request);
    ValidateForm1099KResponseDTO validateForm1099K(UUID submissionId);
    CreateForm1099KResponseDTO getForm1099K(UUID submissionId, UUID recordId);
    Object listForm1099K(UUID submissionId, String businessId);
    Object deleteForm1099K(UUID submissionId, UUID recordId);
    Object requestPdfURLsForm1099K(RequestPdfURLsForm1099KRequestDTO request);
    Object requestDraftPdfUrlForm1099K(RequestDraftPdfUrlForm1099KRequestDTO request);
    Object getPDFForm1099K(UUID submissionId, UUID recordId);
    Object generateFromTxnsForm1099K(GenerateFromTxnsForm1099KRequestDTO request);
    Object approveForm1099K(ApproveForm1099KRequestDTO request);
    Object getByRecordIdsForm1099K(GetbyRecordIdsForm1099KRequestDTO request);
    Object requestDistUrlForm1099K(RequestDistUrlForm1099KRequestDTO request);
    Object uploadAttachmentForm1099K(UploadAttachmentForm1099KRequestDTO request);
}

