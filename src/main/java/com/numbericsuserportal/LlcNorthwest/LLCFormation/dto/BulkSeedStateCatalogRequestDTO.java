package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkSeedStateCatalogRequestDTO {
    private List<StateCatalogSeedItemDTO> states;

    /** When true (default), syncs each state's filing fee into llc_formation_rate as STATE_FEE. */
    private Boolean syncRates = Boolean.TRUE;
}
