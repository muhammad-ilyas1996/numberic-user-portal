package com.numbericsuserportal.LlcNorthwest.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for unlock document API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockDocumentResponseDTO {
    
    private Boolean success;
    
    private String timestamp;
    
    private UnlockResult result;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnlockResult {
        
        /**
         * Action performed (e.g., "capture")
         */
        private String action;
        
        /**
         * Amount in cents
         */
        private Integer amount;
        
        /**
         * Result message
         */
        private String result;
        
        /**
         * Whether the operation was successful
         */
        private Boolean success;
        
        /**
         * List of invoice IDs generated
         */
        @JsonProperty("invoice_ids")
        private List<String> invoiceIds;
    }
}

