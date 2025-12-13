package com.numbericsuserportal.LlcNorthwest.registeredagent.impl;

import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.registeredagent.service.RegisteredAgentProductService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisteredAgentProductServiceImpl implements RegisteredAgentProductService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    public RegisteredAgentProductsResponseDTO getRegisteredAgentProducts(String url) {
        return corporateToolsApiService.getRegisteredAgentProducts(url);
    }
}

