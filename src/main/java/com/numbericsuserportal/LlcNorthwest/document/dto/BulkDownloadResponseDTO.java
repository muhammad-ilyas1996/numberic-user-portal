package com.numbericsuserportal.LlcNorthwest.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for bulk download API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkDownloadResponseDTO {
    
    private Boolean success;
    
    private String timestamp;
    
    private List<PageUrlDTO> result;
}

