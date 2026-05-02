package com.numbericsuserportal.taxbandit.controller;

import com.numbericsuserportal.taxbandit.webhook.TaxBanditsPdfWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Public callback endpoint registered in TaxBandits Developer Console for webhooks.
 * TaxBandits validates the URL by POSTing sample JSON; must return HTTP 200 for activation.
 */
@RestController
@RequestMapping("/api/taxbandits")
@CrossOrigin(origins = "*")
public class TaxBanditsWebhookController {

    private static final Logger log = LoggerFactory.getLogger(TaxBanditsWebhookController.class);

    private final TaxBanditsPdfWebhookService pdfWebhookService;

    public TaxBanditsWebhookController(TaxBanditsPdfWebhookService pdfWebhookService) {
        this.pdfWebhookService = pdfWebhookService;
    }

    @RequestMapping(value = "/webhook", method = { RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.PATCH })
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody(required = false) String body) {
        if (body != null && !body.isBlank()) {
            log.info("TaxBandits webhook payload received ({} chars)", body.length());
            pdfWebhookService.handleIncomingJson(body);
        } else {
            log.info("TaxBandits webhook ping (empty body)");
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Returns the latest stored artifact manifest for a Form 1099-K record (written by PDF_COMPLETE webhook processing).
     */
    @GetMapping("/form1099k/pdf-artifact")
    public ResponseEntity<?> getLatestPdfArtifact(@RequestParam UUID submissionId, @RequestParam UUID recordId) {
        try {
            Map<String, Object> manifest = pdfWebhookService.findLatestManifest(submissionId.toString(), recordId.toString());
            if (manifest == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "No PDF artifact found yet for this submission/record"
                ));
            }
            return ResponseEntity.ok(manifest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
