package com.numbericsuserportal.LlcNorthwest.registeredagent.service;

import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductsResponseDTO;

public interface RegisteredAgentProductService {
    RegisteredAgentProductsResponseDTO getRegisteredAgentProducts(String url);
}

