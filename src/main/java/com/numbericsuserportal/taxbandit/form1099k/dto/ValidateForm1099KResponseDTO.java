package com.numbericsuserportal.taxbandit.form1099k.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO for Form 1099-K validation response.
 */
@Data
public class ValidateForm1099KResponseDTO {
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("StatusMessage")
    private String statusMessage;
    
    @JsonProperty("Errors")
    private List<CreateForm1099KResponseDTO.ErrorDTO> errors;
    
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

