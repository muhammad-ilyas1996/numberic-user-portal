package com.numbericsuserportal.LlcNorthwest.LLCFormation.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductDTO;
import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductsResponseDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Populated at application startup via {@link NorthwestCatalogWarmupRunner}. Used for Step 4
 * jurisdiction filtering without a new Corporate Tools call.
 */
@Component
public class NorthwestCatalogCache {

    private volatile JsonNode websites;
    private volatile FilingProductsResponseDTO filingProductsAllStates;
    private volatile RegisteredAgentProductsResponseDTO registeredAgentProductsAllStates;
    private volatile String lastWarmupError;

    public JsonNode getWebsites() {
        return websites;
    }

    public void setWebsites(JsonNode websites) {
        this.websites = websites;
    }

    public FilingProductsResponseDTO getFilingProductsAllStates() {
        return filingProductsAllStates;
    }

    public void setFilingProductsAllStates(FilingProductsResponseDTO filingProductsAllStates) {
        this.filingProductsAllStates = filingProductsAllStates;
    }

    public RegisteredAgentProductsResponseDTO getRegisteredAgentProductsAllStates() {
        return registeredAgentProductsAllStates;
    }

    public void setRegisteredAgentProductsAllStates(RegisteredAgentProductsResponseDTO registeredAgentProductsAllStates) {
        this.registeredAgentProductsAllStates = registeredAgentProductsAllStates;
    }

    public String getLastWarmupError() {
        return lastWarmupError;
    }

    public void setLastWarmupError(String lastWarmupError) {
        this.lastWarmupError = lastWarmupError;
    }

    /**
     * Filters cached RA products by jurisdiction string (case-insensitive exact match on DTO field).
     */
    public RegisteredAgentProductsResponseDTO filterRegisteredAgentProductsByJurisdiction(String jurisdiction) {
        RegisteredAgentProductsResponseDTO full = registeredAgentProductsAllStates;
        RegisteredAgentProductsResponseDTO out = new RegisteredAgentProductsResponseDTO();
        if (full != null) {
            out.setSuccess(full.getSuccess());
            out.setTimestamp(full.getTimestamp());
        } else {
            out.setSuccess(false);
        }
        if (full == null || full.getResult() == null || jurisdiction == null || jurisdiction.isBlank()) {
            out.setResult(List.of());
            return out;
        }
        String j = jurisdiction.trim();
        List<RegisteredAgentProductDTO> filtered = full.getResult().stream()
                .filter(p -> p.getJurisdiction() != null && p.getJurisdiction().equalsIgnoreCase(j))
                .collect(Collectors.toCollection(ArrayList::new));
        out.setResult(filtered);
        return out;
    }
}
