package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkSeedStateCatalogResponseDTO {
    private int upserted;
    private int ratesSynced;
    private boolean globalRatesSynced;
}
