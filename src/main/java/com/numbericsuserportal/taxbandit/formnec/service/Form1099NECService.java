package com.numbericsuserportal.taxbandit.formnec.service;

import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECRequestDTO;
import com.numbericsuserportal.taxbandit.formnec.dto.CreateForm1099NECResponseDTO;

public interface Form1099NECService {
    CreateForm1099NECResponseDTO createForm1099NEC(CreateForm1099NECRequestDTO request);
}

