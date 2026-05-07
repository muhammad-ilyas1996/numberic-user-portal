package com.numbericsuserportal.LlcNorthwest.websites.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface WebsitesService {
    JsonNode getWebsites(String websiteUrl);
}
