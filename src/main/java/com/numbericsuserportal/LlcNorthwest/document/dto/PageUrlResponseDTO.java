package com.numbericsuserportal.LlcNorthwest.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for page URL API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageUrlResponseDTO {
    
    private Boolean success;
    
    private String timestamp;
    
    private PageUrlDTO result;
}

