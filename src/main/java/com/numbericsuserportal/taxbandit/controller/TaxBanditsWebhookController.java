package com.numbericsuserportal.taxbandit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public callback endpoint registered in TaxBandits Developer Console for webhooks.
 * TaxBandits validates the URL by POSTing sample JSON; must return HTTP 200 for activation.
 */
@RestController
@RequestMapping("/api/taxbandits")
@CrossOrigin(origins = "*")
public class TaxBanditsWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TaxBanditsWebhookController.class);

    @RequestMapping(value = "/webhook", method = { RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.PATCH })
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody(required = false) String body) {
        if (body != null && !body.isBlank()) {
            log.info("TaxBandits webhook payload received ({} chars)", body.length());
            log.debug("TaxBandits webhook body: {}", body);
        } else {
            log.info("TaxBandits webhook ping (empty body)");
        }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
