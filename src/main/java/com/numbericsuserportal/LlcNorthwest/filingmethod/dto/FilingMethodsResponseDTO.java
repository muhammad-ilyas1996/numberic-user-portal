package com.numbericsuserportal.LlcNorthwest.filingmethod.dto;

import com.numbericsuserportal.LlcNorthwest.dto.FilingMethodDTO;
import lombok.Data;
import java.util.List;

@Data
public class FilingMethodsResponseDTO {
    private Boolean success;
    private String timestamp;
    private List<FilingMethodDTO> result;
}

