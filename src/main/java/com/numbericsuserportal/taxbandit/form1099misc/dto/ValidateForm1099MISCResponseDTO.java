package com.numbericsuserportal.taxbandit.form1099misc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO for Form 1099-MISC validation response.
 */
@Data
public class ValidateForm1099MISCResponseDTO {
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("StatusMessage")
    private String statusMessage;
    
    @JsonProperty("Errors")
    private List<CreateForm1099MISCResponseDTO.ErrorDTO> errors;
    
    @JsonProperty("ValidationResults")
    private List<ValidationResultDTO> validationResults;
    
    @Data
    public static class ValidationResultDTO {
        @JsonProperty("FieldName")
        private String fieldName;
        
        @JsonProperty("IsValid")
        private Boolean isValid;
        
        @JsonProperty("ErrorMessage")
        private String errorMessage;
    }
}

