package com.numbericsuserportal.taxbandit.controller;

import com.numbericsuserportal.taxbandit.config.TaxBanditsArtifactAccessProperties;
import com.numbericsuserportal.taxbandit.webhook.TaxBanditsPdfWebhookService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * Authenticated download for TaxBandits-generated PDFs stored on disk after webhook ingestion.
 */
@RestController
@RequestMapping("/api/taxbandits/artifacts")
@CrossOrigin(origins = "*")
public class TaxBanditsArtifactDownloadController {

    public static final String ARTIFACT_KEY_HEADER = "X-Numbrics-Artifact-Key";

    private final TaxBanditsPdfWebhookService pdfWebhookService;
    private final TaxBanditsArtifactAccessProperties accessProperties;

    public TaxBanditsArtifactDownloadController(TaxBanditsPdfWebhookService pdfWebhookService,
                                               TaxBanditsArtifactAccessProperties accessProperties) {
        this.pdfWebhookService = pdfWebhookService;
        this.accessProperties = accessProperties;
    }

    /**
     * GET /api/taxbandits/artifacts/pdf?submissionId=...&recordId=...&file=1099NEC_COPY1_1Up.pdf
     */
    @GetMapping("/pdf")
    public ResponseEntity<?> downloadLatestPdf(@RequestParam UUID submissionId,
                                               @RequestParam UUID recordId,
                                               @RequestParam("file") String fileName,
                                               @RequestHeader(value = ARTIFACT_KEY_HEADER, required = false) String apiKey) {
        try {
            if (accessProperties.isRequireApiKey()) {
                String expected = accessProperties.getApiKey();
                if (expected == null || expected.isBlank()) {
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "success", false,
                        "error", "Artifact downloads are not configured (missing taxbandits.artifact.api-key)"
                    ));
                }
                if (apiKey == null || !expected.equals(apiKey)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "error", "Forbidden"
                    ));
                }
            }

            Path path = pdfWebhookService.resolveLatestPdfFile(submissionId.toString(), recordId.toString(), fileName);
            if (path == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "PDF not found for latest artifact delivery"
                ));
            }

            Resource resource = new FileSystemResource(path.toFile());

            String safeLeaf = path.getFileName().toString();
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeLeaf + "\"")
                .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
