package com.numbericsuserportal.taxbandit.formnec.impl;

import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECRequestDTO;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECResponseDTO;
import com.numbericsuserportal.taxbandit.formnec.service.Form1099NECService;
import com.numbericsuserportal.taxbandit.service.TaxBanditsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Form1099NECServiceImpl implements Form1099NECService {

    @Autowired
    private TaxBanditsApiService taxBanditsApiService;

    @Override
    public CreateForm1099NECResponseDTO createForm1099NEC(CreateForm1099NECRequestDTO request) {
        // Set default values for fields that cannot be null (TaxBandits API requirement)
        setDefaultValues(request);
        
        return taxBanditsApiService.createForm1099NEC(request);
    }
    
    /**
     * Set default values for required fields that cannot be null
     * This prevents validation errors from TaxBandits API
     */
    private void setDefaultValues(CreateForm1099NECRequestDTO request) {
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
                
                // Set default for NECFormData.B3EPP (Excess golden parachute payments)
                if (returnData.getNECFormData() != null && returnData.getNECFormData().getB3EPP() == null) {
                    returnData.getNECFormData().setB3EPP(0.0);
                }
            }
        }
    }
}

