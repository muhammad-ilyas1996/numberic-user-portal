package com.numbericsuserportal.LlcNorthwest.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for page URL information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageUrlDTO {
    
    /**
     * Document ID
     */
    private UUID id;
    
    /**
     * URL of the page/document
     */
    private String url;
}

