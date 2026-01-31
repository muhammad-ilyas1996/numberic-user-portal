package com.numbericsuserportal.taxbandit.formnec.impl;

import com.numbericsuserportal.taxbandit.common.service.BaseFormService;
import com.numbericsuserportal.taxbandit.formnec.dto.*;
import com.numbericsuserportal.taxbandit.formnec.service.Form1099NECService;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service implementation for Form 1099-NEC.
 * Handles creation and processing of 1099-NEC forms for non-employee compensation.
 */
@Service
public class Form1099NECServiceImpl extends BaseFormService<CreateForm1099NECRequestDTO, CreateForm1099NECResponseDTO> 
        implements Form1099NECService {

    @Override
    public CreateForm1099NECResponseDTO createForm1099NEC(CreateForm1099NECRequestDTO request) {
        return processForm(request);
    }

    @Override
    public UpdateForm1099NECResponseDTO updateForm1099NEC(UpdateForm1099NECRequestDTO request) {
        return taxBanditsApiService.updateForm1099NEC(request);
    }

    @Override
    public TransmitForm1099NECResponseDTO transmitForm1099NEC(TransmitForm1099NECRequestDTO request) {
        return taxBanditsApiService.transmitForm1099NEC(request);
    }

    @Override
    public Form1099NECStatusResponseDTO getForm1099NECStatus(UUID submissionId) {
        return taxBanditsApiService.getForm1099NECStatus(submissionId);
    }

    @Override
    public ValidateForm1099NECResponseDTO validateForm1099NEC(ValidateForm1099NECRequestDTO request) {
        setValidateFormDefaults(request);
        return taxBanditsApiService.validateForm1099NEC(request);
    }

    @Override
    public ValidateForm1099NECResponseDTO validateForm1099NEC(UUID submissionId) {
        return taxBanditsApiService.validateForm1099NEC(submissionId);
    }

    @Override
    public CreateForm1099NECResponseDTO getForm1099NEC(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.getForm1099NEC(submissionId, recordId);
    }

    @Override
    public Object listForm1099NEC(UUID submissionId, String businessId) {
        return taxBanditsApiService.listForm1099NEC(submissionId, businessId);
    }

    @Override
    public Object deleteForm1099NEC(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.deleteForm1099NEC(submissionId, recordId);
    }

    @Override
    public Object requestPdfURLs(RequestPdfURLsForm1099NECRequestDTO request) {
        return taxBanditsApiService.requestPdfURLsForm1099NEC(request);
    }

    @Override
    public Object requestDraftPdfUrl(RequestDraftPdfUrlForm1099NECRequestDTO request) {
        return taxBanditsApiService.requestDraftPdfUrlForm1099NEC(request);
    }

    @Override
    public Object getPDF(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.getPDFForm1099NEC(submissionId, recordId);
    }

    @Override
    public Object generateFromTxns(GenerateFromTxnsForm1099NECRequestDTO request) {
        return taxBanditsApiService.generateFromTxnsForm1099NEC(request);
    }

    @Override
    public Object approveForm1099NEC(ApproveForm1099NECRequestDTO request) {
        return taxBanditsApiService.approveForm1099NEC(request);
    }

    @Override
    public Object getByRecordIds(GetbyRecordIdsForm1099NECRequestDTO request) {
        return taxBanditsApiService.getByRecordIdsForm1099NEC(request);
    }

    @Override
    public Object requestDistUrl(RequestDistUrlForm1099NECRequestDTO request) {
        return taxBanditsApiService.requestDistUrlForm1099NEC(request);
    }

    @Override
    public Object uploadAttachment(UploadAttachmentForm1099NECRequestDTO request) {
        return taxBanditsApiService.uploadAttachmentForm1099NEC(request);
    }
    
    /**
     * Set default values for required fields that cannot be null.
     * This prevents validation errors from TaxBandits API.
     */
    @Override
    protected void setDefaultValues(CreateForm1099NECRequestDTO request) {
        // Set default for Business.IsBusinessTerminated
        if (request.getReturnHeader() != null && 
            request.getReturnHeader().getBusiness() != null &&
            request.getReturnHeader().getBusiness().getIsBusinessTerminated() == null) {
            request.getReturnHeader().getBusiness().setIsBusinessTerminated(false);
        }
        
        // Set defaults for ReturnData items
        if (request.getReturnData() != null) {
            for (CreateForm1099NECRequestDTO.ReturnDataDTO returnData : request.getReturnData()) {
                // Set default for IsForced
                if (returnData.getIsForced() == null) {
                    returnData.setIsForced(false);
                }
                
                // Set defaults for NECFormData (TaxBandits rejects null for these booleans/decimals)
                if (returnData.getNecFormData() != null) {
                    var nec = returnData.getNecFormData();
                    if (nec.getB1NEC() == null) nec.setB1NEC(0.0);
                    if (nec.getB2IsDirectSales() == null) nec.setB2IsDirectSales(false);
                    if (nec.getB3EPP() == null) nec.setB3EPP(0.0);
                    if (nec.getB4FedTaxWH() == null) nec.setB4FedTaxWH(0.0);
                    if (nec.getIsFATCA() == null) nec.setIsFATCA(false);
                    if (nec.getIs2ndTINnot() == null) nec.setIs2ndTINnot(false);
                }
            }
        }
    }

    /**
     * Set defaults for ValidateForm request - same fields as Create to prevent TaxBandits validation errors.
     */
    private void setValidateFormDefaults(ValidateForm1099NECRequestDTO request) {
        if (request.getReturnHeader() != null && request.getReturnHeader().getBusiness() != null &&
            request.getReturnHeader().getBusiness().getIsBusinessTerminated() == null) {
            request.getReturnHeader().getBusiness().setIsBusinessTerminated(false);
        }
        if (request.getReturnData() != null) {
            for (var rd : request.getReturnData()) {
                if (rd.getIsForced() == null) rd.setIsForced(false);
                if (rd.getNecFormData() != null) {
                    var nec = rd.getNecFormData();
                    if (nec.getB3EPP() == null) nec.setB3EPP(0.0);
                    if (nec.getIsFATCA() == null) nec.setIsFATCA(false);
                    if (nec.getIs2ndTINnot() == null) nec.setIs2ndTINnot(false);
                }
            }
        }
    }
    
    /**
     * Call TaxBandits API to create Form 1099-NEC.
     */
    @Override
    protected CreateForm1099NECResponseDTO callTaxBanditsAPI(CreateForm1099NECRequestDTO request) {
        return taxBanditsApiService.createForm1099NEC(request);
    }
}

