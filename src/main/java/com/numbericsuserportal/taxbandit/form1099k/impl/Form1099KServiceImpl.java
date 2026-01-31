package com.numbericsuserportal.taxbandit.form1099k.impl;

import com.numbericsuserportal.taxbandit.common.service.BaseFormService;
import com.numbericsuserportal.taxbandit.form1099k.dto.*;
import com.numbericsuserportal.taxbandit.form1099k.service.Form1099KService;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service implementation for Form 1099-K.
 * Handles creation and processing of 1099-K forms for payment card and third party network transactions.
 */
@Service
public class Form1099KServiceImpl extends BaseFormService<CreateForm1099KRequestDTO, CreateForm1099KResponseDTO> 
        implements Form1099KService {

    @Override
    public CreateForm1099KResponseDTO createForm1099K(CreateForm1099KRequestDTO request) {
        return processForm(request);
    }

    @Override
    public UpdateForm1099KResponseDTO updateForm1099K(UpdateForm1099KRequestDTO request) {
        return taxBanditsApiService.updateForm1099K(request);
    }

    @Override
    public TransmitForm1099KResponseDTO transmitForm1099K(TransmitForm1099KRequestDTO request) {
        return taxBanditsApiService.transmitForm1099K(request);
    }

    @Override
    public Form1099KStatusResponseDTO getForm1099KStatus(UUID submissionId) {
        return taxBanditsApiService.getForm1099KStatus(submissionId);
    }

    @Override
    public ValidateForm1099KResponseDTO validateForm1099K(ValidateForm1099KRequestDTO request) {
        setValidateFormKDefaults(request);
        return taxBanditsApiService.validateForm1099K(request);
    }

    @Override
    public ValidateForm1099KResponseDTO validateForm1099K(UUID submissionId) {
        return taxBanditsApiService.validateForm1099K(submissionId);
    }

    @Override
    public CreateForm1099KResponseDTO getForm1099K(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.getForm1099K(submissionId, recordId);
    }

    @Override
    public Object listForm1099K(UUID submissionId, String businessId) {
        return taxBanditsApiService.listForm1099K(submissionId, businessId);
    }

    @Override
    public Object deleteForm1099K(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.deleteForm1099K(submissionId, recordId);
    }

    @Override
    public Object requestPdfURLs(RequestPdfURLsForm1099KRequestDTO request) {
        return taxBanditsApiService.requestPdfURLsForm1099K(request);
    }

    @Override
    public Object requestDraftPdfUrl(RequestDraftPdfUrlForm1099KRequestDTO request) {
        return taxBanditsApiService.requestDraftPdfUrlForm1099K(request);
    }

    @Override
    public Object getPDF(UUID submissionId, UUID recordId) {
        return taxBanditsApiService.getPDFForm1099K(submissionId, recordId);
    }

    @Override
    public Object generateFromTxns(GenerateFromTxnsForm1099KRequestDTO request) {
        return taxBanditsApiService.generateFromTxnsForm1099K(request);
    }

    @Override
    public Object approveForm1099K(ApproveForm1099KRequestDTO request) {
        return taxBanditsApiService.approveForm1099K(request);
    }

    @Override
    public Object getByRecordIds(GetbyRecordIdsForm1099KRequestDTO request) {
        return taxBanditsApiService.getByRecordIdsForm1099K(request);
    }

    @Override
    public Object requestDistUrl(RequestDistUrlForm1099KRequestDTO request) {
        return taxBanditsApiService.requestDistUrlForm1099K(request);
    }

    @Override
    public Object uploadAttachment(UploadAttachmentForm1099KRequestDTO request) {
        return taxBanditsApiService.uploadAttachmentForm1099K(request);
    }
    
    /**
     * Set default values for required fields that cannot be null.
     * This prevents validation errors from TaxBandits API.
     */
    @Override
    protected void setDefaultValues(CreateForm1099KRequestDTO request) {
        if (request.getReturnHeader() != null && request.getReturnHeader().getBusiness() != null) {
            var biz = request.getReturnHeader().getBusiness();
            if (biz.getIsBusinessTerminated() == null) biz.setIsBusinessTerminated(false);
            if (biz.getIsForeign() == null) biz.setIsForeign(false);
            if (biz.getIsEIN() == null && biz.getEinOrSSN() != null) biz.setIsEIN(true);
            if ((biz.getFirstNm() == null || biz.getFirstNm().isBlank()) && biz.getContactNm() != null) {
                String[] parts = biz.getContactNm().trim().split("\\s+", 2);
                biz.setFirstNm(parts[0]);
                if (parts.length > 1 && (biz.getLastNm() == null || biz.getLastNm().isBlank())) biz.setLastNm(parts[1]);
            }
            if (biz.getFirstNm() == null || biz.getFirstNm().isBlank()) biz.setFirstNm("N/A");
            if (biz.getLastNm() == null || biz.getLastNm().isBlank()) biz.setLastNm("N/A");
            if (biz.getBusinessNm() == null && biz.getBusinessId() == null) biz.setBusinessNm("Test Business");
            if (biz.getSigningAuthority() == null && (biz.getContactNm() != null || biz.getBusinessMemberType() != null)) {
                var sa = new CreateForm1099KRequestDTO.SigningAuthorityDTO();
                sa.setName(biz.getContactNm() != null ? biz.getContactNm() : "N/A");
                sa.setPhone(biz.getPhone());
                sa.setBusinessMemberType(biz.getBusinessMemberType() != null ? biz.getBusinessMemberType() : "ADMINISTRATOR");
                biz.setSigningAuthority(sa);
            }
            // TaxBandits: USAddress required when IsForeign is false
            if (Boolean.FALSE.equals(biz.getIsForeign())) {
                if (biz.getUsAddress() == null) {
                    var addr = new CreateForm1099KRequestDTO.USAddressDTO();
                    addr.setAddress1("N/A");
                    addr.setCity("N/A");
                    addr.setState("NY");
                    addr.setZipCd("10001");
                    biz.setUsAddress(addr);
                } else {
                    var a = biz.getUsAddress();
                    if (a.getAddress1() == null || a.getAddress1().isBlank()) a.setAddress1("N/A");
                    if (a.getCity() == null || a.getCity().isBlank()) a.setCity("N/A");
                    if (a.getState() == null || a.getState().isBlank()) a.setState("NY");
                    if (a.getZipCd() == null || a.getZipCd().isBlank()) a.setZipCd("10001");
                }
            }
        }
        if (request.getReturnData() != null) {
            for (var returnData : request.getReturnData()) {
                if (returnData.getIsForced() == null) returnData.setIsForced(false);
                if (returnData.getRecipient() != null) {
                    var rec = returnData.getRecipient();
                    if (rec.getIsForeign() == null) rec.setIsForeign(false);
                    if (Boolean.FALSE.equals(rec.getIsForeign()) && rec.getUsAddress() == null) {
                        var addr = new CreateForm1099KRequestDTO.USAddressDTO();
                        addr.setAddress1("N/A");
                        addr.setCity("N/A");
                        addr.setState("CA");
                        addr.setZipCd("90001");
                        rec.setUsAddress(addr);
                    }
                }
                if (returnData.getKFormData() != null) {
                    var k = returnData.getKFormData();
                    if (k.getB1aGrossAmt() == null) k.setB1aGrossAmt(0.0);
                    if (k.getB1bCardNotPresentTxns() == null) k.setB1bCardNotPresentTxns(0.0);
                    if (k.getB3NumPymtTxns() == null) k.setB3NumPymtTxns(0);
                    if (k.getB4FedTaxWH() == null) k.setB4FedTaxWH(0.0);
                    if (k.getIs2ndTINnot() == null) k.setIs2ndTINnot(false);
                    if (k.getFilerIndicator() == null) k.setFilerIndicator("PSE");
                    if (k.getIndicateTxnsReported() == null) k.setIndicateTxnsReported("Payment_Card");
                }
            }
        }
    }
    
    private void setValidateFormKDefaults(ValidateForm1099KRequestDTO request) {
        if (request.getReturnHeader() != null && request.getReturnHeader().getBusiness() != null) {
            var biz = request.getReturnHeader().getBusiness();
            if (biz.getIsBusinessTerminated() == null) biz.setIsBusinessTerminated(false);
            if (biz.getIsForeign() == null) biz.setIsForeign(false);
            if (biz.getIsEIN() == null && biz.getEinOrSSN() != null) biz.setIsEIN(true);
            if ((biz.getFirstNm() == null || biz.getFirstNm().isBlank()) && biz.getContactNm() != null) {
                String[] parts = biz.getContactNm().trim().split("\\s+", 2);
                biz.setFirstNm(parts[0]);
                if (parts.length > 1 && (biz.getLastNm() == null || biz.getLastNm().isBlank())) biz.setLastNm(parts[1]);
            }
            if (biz.getFirstNm() == null || biz.getFirstNm().isBlank()) biz.setFirstNm("N/A");
            if (biz.getLastNm() == null || biz.getLastNm().isBlank()) biz.setLastNm("N/A");
            if (biz.getSigningAuthority() == null && (biz.getContactNm() != null || biz.getBusinessMemberType() != null)) {
                var sa = new CreateForm1099KRequestDTO.SigningAuthorityDTO();
                sa.setName(biz.getContactNm() != null ? biz.getContactNm() : "N/A");
                sa.setPhone(biz.getPhone());
                sa.setBusinessMemberType(biz.getBusinessMemberType() != null ? biz.getBusinessMemberType() : "ADMINISTRATOR");
                biz.setSigningAuthority(sa);
            }
            if (Boolean.FALSE.equals(biz.getIsForeign()) && biz.getUsAddress() == null) {
                var addr = new CreateForm1099KRequestDTO.USAddressDTO();
                addr.setAddress1("N/A");
                addr.setCity("N/A");
                addr.setState("NY");
                addr.setZipCd("10001");
                biz.setUsAddress(addr);
            }
        }
        if (request.getReturnData() != null) {
            for (var rd : request.getReturnData()) {
                if (rd.getRecipient() != null) {
                    var rec = rd.getRecipient();
                    if (rec.getIsForeign() == null) rec.setIsForeign(false);
                    if (Boolean.FALSE.equals(rec.getIsForeign()) && rec.getUsAddress() == null) {
                        var addr = new CreateForm1099KRequestDTO.USAddressDTO();
                        addr.setAddress1("N/A");
                        addr.setCity("N/A");
                        addr.setState("CA");
                        addr.setZipCd("90001");
                        rec.setUsAddress(addr);
                    }
                }
                if (rd.getKFormData() != null) {
                    var k = rd.getKFormData();
                    if (k.getB1aGrossAmt() == null) k.setB1aGrossAmt(0.0);
                    if (k.getB1bCardNotPresentTxns() == null) k.setB1bCardNotPresentTxns(0.0);
                    if (k.getB3NumPymtTxns() == null) k.setB3NumPymtTxns(0);
                    if (k.getIs2ndTINnot() == null) k.setIs2ndTINnot(false);
                    if (k.getFilerIndicator() == null) k.setFilerIndicator("PSE");
                    if (k.getIndicateTxnsReported() == null) k.setIndicateTxnsReported("Payment_Card");
                }
            }
        }
    }

    @Override
    protected CreateForm1099KResponseDTO callTaxBanditsAPI(CreateForm1099KRequestDTO request) {
        return taxBanditsApiService.createForm1099K(request);
    }
}

