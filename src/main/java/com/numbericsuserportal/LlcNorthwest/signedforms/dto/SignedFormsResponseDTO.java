package com.numbericsuserportal.LlcNorthwest.signedforms.dto;

import lombok.Data;
import java.util.List;

@Data
public class SignedFormsResponseDTO {
    private Boolean success;
    private String timestamp;
    private List<SignedFormDTO> result;
}

