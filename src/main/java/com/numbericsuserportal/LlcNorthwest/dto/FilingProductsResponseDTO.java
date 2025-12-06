package com.numbericsuserportal.LlcNorthwest.dto;

import lombok.Data;
import java.util.List;

@Data
public class FilingProductsResponseDTO {
    private Boolean success;
    private String timestamp;
    private List<FilingProductDTO> result;
}

