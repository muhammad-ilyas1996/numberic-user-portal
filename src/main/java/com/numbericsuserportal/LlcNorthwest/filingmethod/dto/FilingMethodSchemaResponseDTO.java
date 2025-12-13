package com.numbericsuserportal.LlcNorthwest.filingmethod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FilingMethodSchemaResponseDTO {
    private Boolean success;
    private String timestamp;
    private List<SchemaFieldDTO> result;
    
    @Data
    public static class SchemaFieldDTO {
        private String name;
        private String title;
        private String type;
        
        @JsonProperty("meta_type")
        private String metaType;
        
        private Boolean required;
        
        @JsonProperty("help_text")
        private String helpText;
        
        @JsonProperty("can_have_multiple")
        private Boolean canHaveMultiple;
        
        private Map<String, String> validations;
        
        private List<SchemaFieldDTO> fields;
    }
}

