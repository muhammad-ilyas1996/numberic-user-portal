package com.numbericsuserportal.LlcNorthwest.companies.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CompanyDTO {
    private UUID id;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    private String name;
    
    @JsonProperty("entity_type")
    private String entityType;
    
    private List<String> jurisdictions;
    
    @JsonProperty("home_state")
    private String homeState;
}

