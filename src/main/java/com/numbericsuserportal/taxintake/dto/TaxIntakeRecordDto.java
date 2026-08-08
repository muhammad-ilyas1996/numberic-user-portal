package com.numbericsuserportal.taxintake.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxIntakeRecordDto {
    private Long id;
    private String softwareName;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String documentType;
    private String rawText;
    private String extractedDataJson;
    private Double overallConfidence;
    private Integer pageCount;
    private String processingStatus;
    private String errorMessage;
    private Date createdOn;
    private String createdBy;
}
