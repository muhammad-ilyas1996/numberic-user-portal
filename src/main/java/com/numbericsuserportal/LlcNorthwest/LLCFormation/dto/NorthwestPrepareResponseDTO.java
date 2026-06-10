package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NorthwestPrepareResponseDTO {
    private Long formationId;
    private String status;
    private String companyId;
    private String filingProductId;
    private String filingMethodId;
    private String jurisdictionFullName;
    private boolean shoppingCartReady;
    private String message;
}
