package com.numbericsuserportal.federaltax.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FederalTaxDocumentDto {
    private Long id;
    private String originalFilename;
    private String docType;
    private String uploadedVia;
    private Map<String, Object> extracted;
    private LocalDateTime createdAt;
}
