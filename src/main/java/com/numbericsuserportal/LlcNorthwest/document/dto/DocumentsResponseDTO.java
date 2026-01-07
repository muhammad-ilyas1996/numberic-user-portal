package com.numbericsuserportal.LlcNorthwest.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for documents list API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentsResponseDTO {
    
    private Boolean success;
    
    private String timestamp;
    
    private List<DocumentDTO> result;
}

