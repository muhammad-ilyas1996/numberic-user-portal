package com.numbericsuserportal.LlcNorthwest.LLCFormation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatesCatalogResponseDTO {
    private List<StateCatalogItemDTO> states;
    private StateCatalogMetaDTO meta;
}
