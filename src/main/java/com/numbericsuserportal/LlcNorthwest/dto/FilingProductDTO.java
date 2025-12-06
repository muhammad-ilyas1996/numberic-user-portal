package com.numbericsuserportal.LlcNorthwest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class FilingProductDTO {
    private UUID id;
    private String name;
    
    @JsonProperty("filing_name")
    private String filingName;
    
    private String price;
    
    @JsonProperty("filing_methods")
    private List<FilingMethodDTO> filingMethods;
}

