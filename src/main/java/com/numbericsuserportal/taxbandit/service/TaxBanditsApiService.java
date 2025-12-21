package com.numbericsuserportal.taxbandit.service;

import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECRequestDTO;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECResponseDTO;

public interface TaxBanditsApiService {
    CreateForm1099NECResponseDTO createForm1099NEC(CreateForm1099NECRequestDTO request);
}

