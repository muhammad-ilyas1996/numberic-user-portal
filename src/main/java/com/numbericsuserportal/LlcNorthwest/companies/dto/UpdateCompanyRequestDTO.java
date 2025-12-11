package com.numbericsuserportal.LlcNorthwest.companies.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCompanyRequestDTO {
    private List<CompanyUpdateInputDTO> companies;
    
    @JsonProperty("duplicate_name_allowed")
    private Boolean duplicateNameAllowed;
    
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CompanyUpdateInputDTO {
        @JsonProperty("company_id")
        private UUID companyId;
        
        private String company; // Company name for lookup
        
        private String name;
        
        @JsonProperty("entity_type")
        private String entityType;
        
        private List<String> jurisdictions;
        
        @JsonProperty("home_state")
        private String homeState;
    }
}

