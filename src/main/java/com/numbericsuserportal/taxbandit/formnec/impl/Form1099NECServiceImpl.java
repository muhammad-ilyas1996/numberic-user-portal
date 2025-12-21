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
        return taxBanditsApiService.createForm1099NEC(request);
    }
}

