package com.numbericsuserportal.invoice.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serves the public pay-invoice page (link from WhatsApp/email).
 * URL: GET /pay-invoice?token=xxx
 */
@RestController
public class InvoicePaymentPageController {

    @GetMapping(value = "/pay-invoice", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> payInvoicePage() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/pay-invoice.html");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        try (InputStream is = resource.getInputStream()) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(html);
        }
    }
}
