package com.numbericsuserportal.LlcNorthwest.websites.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebsitesServiceImpl implements WebsitesService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    public JsonNode getWebsites(String websiteUrl) {
        return corporateToolsApiService.getWebsites(websiteUrl);
    }
}
