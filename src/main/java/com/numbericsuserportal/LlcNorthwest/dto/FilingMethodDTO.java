package com.numbericsuserportal.LlcNorthwest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

@Data
public class FilingMethodDTO {
    private UUID id;
    private String name;
    private String type; // mail, online, or fax
    private String cost;
    
    @JsonProperty("filed_in")
    private TimeFrameDTO filedIn;
    
    @JsonProperty("docs_in")
    private TimeFrameDTO docsIn;
    
    @JsonProperty("agency_name")
    private String agencyName;
    
    @JsonProperty("filing_description")
    private String filingDescription;
    
    private String jurisdiction;
}

