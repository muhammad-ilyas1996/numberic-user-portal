package com.numbericsuserportal.LlcNorthwest.websites.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.numbericsuserportal.LlcNorthwest.websites.service.WebsitesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llc-northwest/websites")
@CrossOrigin(origins = "*")
public class WebsitesController {

    @Autowired
    private WebsitesService websitesService;

    /**
     * GET /api/llc-northwest/websites?url=https://www.numbrics.ai
     */
    @GetMapping
    public ResponseEntity<?> getWebsites(@RequestParam String url) {
        try {
            JsonNode response = websitesService.getWebsites(url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
