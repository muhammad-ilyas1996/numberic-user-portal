package com.numbericsuserportal.LlcNorthwest.complianceevents.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

@Data
public class ComplianceEventDTO {
    private UUID id;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    private String status; // active or none_required
    
    @JsonProperty("due_date")
    private String dueDate;
    
    private String jurisdiction; // Single jurisdiction string (not array in response)
    
    private String company;
    
    private String filing;
}

