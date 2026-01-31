package com.numbericsuserportal.taxbandit.form1099misc.impl;

import com.numbericsuserportal.taxbandit.common.service.BaseFormService;
import com.numbericsuserportal.taxbandit.form1099misc.dto.*;
import com.numbericsuserportal.taxbandit.form1099misc.service.Form1099MISCService;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service implementation for Form 1099-MISC.
 * Handles creation and processing of 1099-MISC forms for miscellaneous payments.
 */
@Service
public class Form1099MISCServiceImpl extends BaseFormService<CreateForm1099MISCRequestDTO, CreateForm1099MISCResponseDTO> 
        implements Form1099MISCService {

    @Override
    public CreateForm1099MISCResponseDTO createForm1099MISC(CreateForm1099MISCRequestDTO request) {
        return processForm(request);
    }

    @Override
    public UpdateForm1099MISCResponseDTO updateForm1099MISC(UpdateForm1099MISCRequestDTO request) {
        return taxBanditsApiService.updateForm1099MISC(request);
    }

    @Override
    public TransmitForm1099MISCResponseDTO transmitForm1099MISC(TransmitForm1099MISCRequestDTO request) {
        return taxBanditsApiService.transmitForm1099MISC(request);
    }

    @Override
    public Form1099MISCStatusResponseDTO getForm1099MISCStatus(UUID submissionId) {
        return taxBanditsApiService.getForm1099MISCStatus(submissionId);
    }

    @Override
    public ValidateForm1099MISCResponseDTO validateForm1099MISC(ValidateForm1099MISCRequestDTO request) {
        setValidateFormDefaults(request);
        return taxBanditsApiService.validateForm1099MISC(request);
    }

    @Override
    public ValidateForm1099MISCResponseDTO validateForm1099MISC(UUID submissionId) {
        return taxBanditsApiService.validateForm1099MISC(submissionId);
    }

    @Override
    public CreateForm1099MISCResponseDTO getForm1099MISC(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.getForm1099MISC(submissionId, recordId);
    }

    @Override
    public Object listForm1099MISC(UUID submissionId, String businessId) {
        return taxBanditsApiService.listForm1099MISC(submissionId, businessId);
    }

    @Override
    public Object deleteForm1099MISC(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.deleteForm1099MISC(submissionId, recordId);
    }

    @Override
    public Object requestPdfURLs(RequestPdfURLsForm1099MISCRequestDTO request) {
        return taxBanditsApiService.requestPdfURLsForm1099MISC(request);
    }

    @Override
    public Object requestDraftPdfUrl(RequestDraftPdfUrlForm1099MISCRequestDTO request) {
        return taxBanditsApiService.requestDraftPdfUrlForm1099MISC(request);
    }

    @Override
    public Object getPDF(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.getPDFForm1099MISC(submissionId, recordId);
    }

    @Override
    public Object generateFromTxns(GenerateFromTxnsForm1099MISCRequestDTO request) {
        return taxBanditsApiService.generateFromTxnsForm1099MISC(request);
    }

    @Override
    public Object approveForm1099MISC(ApproveForm1099MISCRequestDTO request) {
        return taxBanditsApiService.approveForm1099MISC(request);
    }

    @Override
    public Object getByRecordIds(GetbyRecordIdsForm1099MISCRequestDTO request) {
        return taxBanditsApiService.getByRecordIdsForm1099MISC(request);
    }

    @Override
    public Object requestDistUrl(RequestDistUrlForm1099MISCRequestDTO request) {
        return taxBanditsApiService.requestDistUrlForm1099MISC(request);
    }

    @Override
    public Object uploadAttachment(UploadAttachmentForm1099MISCRequestDTO request) {
        return taxBanditsApiService.uploadAttachmentForm1099MISC(request);
    }
    
    /**
     * Set default values for required fields that cannot be null.
     * This prevents validation errors from TaxBandits API.
     */
    @Override
    protected void setDefaultValues(CreateForm1099MISCRequestDTO request) {
        // Set default for Business.IsBusinessTerminated
        if (request.getReturnHeader() != null && 
            request.getReturnHeader().getBusiness() != null &&
            request.getReturnHeader().getBusiness().getIsBusinessTerminated() == null) {
            request.getReturnHeader().getBusiness().setIsBusinessTerminated(false);
        }
        
        // Set defaults for ReturnData items
        if (request.getReturnData() != null) {
            for (CreateForm1099MISCRequestDTO.ReturnDataDTO returnData : request.getReturnData()) {
                // Set default for IsForced
                if (returnData.getIsForced() == null) {
                    returnData.setIsForced(false);
                }
                
                // Set defaults for MISCFormData fields that cannot be null
                if (returnData.getMiscFormData() != null) {
                    var miscData = returnData.getMiscFormData();
                    if (miscData.getB9DirectSales() == null) miscData.setB9DirectSales(false);
                    if (miscData.getB13ExcessGoldenParachute() == null) miscData.setB13ExcessGoldenParachute(0.0);
                    if (miscData.getIsFATCA() == null) miscData.setIsFATCA(false);
                    if (miscData.getIs2ndTINnot() == null) miscData.setIs2ndTINnot(false);
                }
            }
        }
    }

    private void setValidateFormDefaults(ValidateForm1099MISCRequestDTO request) {
        if (request.getReturnHeader() != null && request.getReturnHeader().getBusiness() != null &&
            request.getReturnHeader().getBusiness().getIsBusinessTerminated() == null) {
            request.getReturnHeader().getBusiness().setIsBusinessTerminated(false);
        }
        if (request.getReturnData() != null) {
            for (var rd : request.getReturnData()) {
                if (rd.getIsForced() == null) rd.setIsForced(false);
                if (rd.getMiscFormData() != null) {
                    var m = rd.getMiscFormData();
                    if (m.getB9DirectSales() == null) m.setB9DirectSales(false);
                    if (m.getB13ExcessGoldenParachute() == null) m.setB13ExcessGoldenParachute(0.0);
                    if (m.getIsFATCA() == null) m.setIsFATCA(false);
                    if (m.getIs2ndTINnot() == null) m.setIs2ndTINnot(false);
                }
            }
        }
    }
    
    /**
     * Call TaxBandits API to create Form 1099-MISC.
     */
    @Override
    protected CreateForm1099MISCResponseDTO callTaxBanditsAPI(CreateForm1099MISCRequestDTO request) {
        return taxBanditsApiService.createForm1099MISC(request);
    }
}



