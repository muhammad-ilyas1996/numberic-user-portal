package com.numbericsuserportal.LlcNorthwest.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for single document API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTO {
    
    private Boolean success;
    
    private String timestamp;
    
    private DocumentDTO result;
}

