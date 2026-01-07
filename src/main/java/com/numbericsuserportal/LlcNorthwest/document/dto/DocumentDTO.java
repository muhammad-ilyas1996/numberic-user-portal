package com.numbericsuserportal.LlcNorthwest.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for document information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    
    /**
     * Unique identifier for record
     */
    private UUID id;
    
    /**
     * Datetime of creation
     */
    @JsonProperty("created_at")
    private String createdAt;
    
    /**
     * Datetime of last update
     */
    @JsonProperty("updated_at")
    private String updatedAt;
    
    /**
     * Name of the document
     */
    private String title;
    
    /**
     * Document status: read, unread
     */
    private String status;
    
    /**
     * Document type (Filing Document, Annual Report, EIN Letter, etc.)
     */
    private String type;
    
    /**
     * Document source: mail, generated, hand-delivered
     */
    private String source;
    
    /**
     * Number of pages in this document
     */
    private Integer pages;
    
    /**
     * True if document is received for client not having active registered agent service
     */
    @JsonProperty("payment_locked")
    private Boolean paymentLocked;
    
    /**
     * Name of state where the document was received
     */
    private String state;
    
    /**
     * Name of company the document is addressed to
     */
    @JsonProperty("company_name")
    private String companyName;
    
    /**
     * Jurisdiction of company
     */
    private String jurisdiction;
    
    /**
     * UUID of the company
     */
    @JsonProperty("company_id")
    private UUID companyId;
}

