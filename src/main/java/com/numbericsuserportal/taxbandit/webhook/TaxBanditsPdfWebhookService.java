package com.numbericsuserportal.taxbandit.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.taxbandit.config.TaxBanditsWebhookStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class TaxBanditsPdfWebhookService {

    private static final Logger log = LoggerFactory.getLogger(TaxBanditsPdfWebhookService.class);

    private final ObjectMapper objectMapper;
    private final TaxBanditsWebhookStorageProperties storageProperties;
    private final TaskExecutor taskExecutor;
    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    public TaxBanditsPdfWebhookService(ObjectMapper objectMapper,
                                      TaxBanditsWebhookStorageProperties storageProperties,
                                      @Qualifier("taxBanditsWebhookExecutor") TaskExecutor taskExecutor) {
        this.objectMapper = objectMapper;
        this.storageProperties = storageProperties;
        this.taskExecutor = taskExecutor;
    }

    public void handleIncomingJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            String eventType = text(root, "EventType");
            if (!"PDF_COMPLETE".equalsIgnoreCase(eventType)) {
                return;
            }

            String formType = text(root, "FormType");
            String submissionId = text(root, "SubmissionId");
            String businessId = text(root, "BusinessId");

            JsonNode records = root.path("Records");
            if (!records.isArray() || records.isEmpty()) {
                log.warn("PDF_COMPLETE webhook missing Records array");
                return;
            }

            for (JsonNode rec : records) {
                String recordId = text(rec, "RecordId");
                String sequenceId = text(rec, "SequenceId");
                String fileName = text(rec, "FileName");
                String filePathUrl = text(rec, "FilePath");
                String status = text(rec, "Status");

                if (recordId == null || recordId.isBlank() || submissionId == null || submissionId.isBlank()) {
                    log.warn("Skipping PDF_COMPLETE record missing SubmissionId/RecordId");
                    continue;
                }

                if (filePathUrl == null || filePathUrl.isBlank()) {
                    log.warn("PDF_COMPLETE record missing FilePath for record {}", recordId);
                    continue;
                }

                if (!"SUCCESS".equalsIgnoreCase(status == null ? "" : status)) {
                    log.info("PDF_COMPLETE record not SUCCESS (status={}) submission={} record={}", status, submissionId, recordId);
                    continue;
                }

                String deliveryId = UUID.randomUUID().toString();
                Path base = artifactDir(submissionId, recordId, deliveryId);
                writeRawPayload(base, rawJson);

                Runnable job = () -> processPdfZipDelivery(
                    formType,
                    submissionId,
                    businessId,
                    recordId,
                    sequenceId,
                    fileName,
                    filePathUrl,
                    base
                );

                taskExecutor.execute(wrapException(job));
            }
        } catch (Exception e) {
            log.warn("Failed to parse/handle TaxBandits webhook JSON: {}", e.getMessage());
        }
    }

    private Runnable wrapException(Runnable r) {
        return () -> {
            try {
                r.run();
            } catch (Exception e) {
                log.error("TaxBandits webhook async job failed", e);
            }
        };
    }

    private void processPdfZipDelivery(String formType,
                                      String submissionId,
                                      String businessId,
                                      String recordId,
                                      String sequenceId,
                                      String fileName,
                                      String zipUrl,
                                      Path base) {
        try {
            Files.createDirectories(base);

            String zipFileName = (fileName == null || fileName.isBlank()) ? (recordId + ".zip") : fileName;
            Path zipPath = base.resolve(sanitizeFileName(zipFileName));

            downloadToFile(zipUrl, zipPath);

            List<String> extractedPdfRelPaths = new ArrayList<>();
            try (InputStream in = Files.newInputStream(zipPath);
                 ZipInputStream zis = new ZipInputStream(in)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String name = entry.getName();
                    if (!name.toLowerCase().endsWith(".pdf")) {
                        continue;
                    }
                    Path out = base.resolve(sanitizeFileName(name.replace("/", "_")));
                    Files.createDirectories(out.getParent());
                    Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                    extractedPdfRelPaths.add(toRelativeUploadPath(out));
                }
            }

            LinkedHashMap<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("deliveryId", base.getFileName().toString());
            manifest.put("receivedAt", Instant.now().toString());
            manifest.put("eventType", "PDF_COMPLETE");
            manifest.put("formType", formType == null ? "" : formType);
            manifest.put("submissionId", submissionId);
            manifest.put("businessId", businessId == null ? "" : businessId);
            manifest.put("recordId", recordId);
            manifest.put("sequenceId", sequenceId == null ? "" : sequenceId);
            manifest.put("zipUrl", zipUrl);
            manifest.put("zipLocalRelativePath", toRelativeUploadPath(zipPath));
            manifest.put("extractedPdfRelativePaths", extractedPdfRelPaths);
            manifest.put("status", extractedPdfRelPaths.isEmpty() ? "ZIP_DOWNLOADED_NO_PDF_ENTRIES" : "READY");
            writeManifest(base, manifest);

            log.info("TaxBandits PDF webhook processed: submission={} record={} pdfs={}", submissionId, recordId, extractedPdfRelPaths.size());
        } catch (Exception e) {
            log.error("Failed processing PDF zip for submission={} record={}: {}", submissionId, recordId, e.getMessage());
            try {
                LinkedHashMap<String, Object> fail = new LinkedHashMap<>();
                fail.put("deliveryId", base.getFileName().toString());
                fail.put("failedAt", Instant.now().toString());
                fail.put("eventType", "PDF_COMPLETE");
                fail.put("formType", formType == null ? "" : formType);
                fail.put("submissionId", submissionId);
                fail.put("recordId", recordId);
                fail.put("zipUrl", zipUrl);
                fail.put("status", "FAILED");
                fail.put("error", e.getMessage());
                writeManifest(base, fail);
            } catch (Exception ignored) {
                // best-effort
            }
        }
    }

    private void downloadToFile(String url, Path dest) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<InputStream> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
        int code = resp.statusCode();
        if (code < 200 || code >= 300) {
            try (InputStream err = resp.body()) {
                String bodySnippet = readSnippet(err, 512);
                throw new IOException("HTTP " + code + " downloading zip. Snippet=" + bodySnippet);
            }
        }

        Files.createDirectories(dest.getParent());
        try (InputStream in = resp.body()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        // Quick sanity: reject obvious XML error payloads saved as zip (without loading entire file into memory)
        try (InputStream peek = Files.newInputStream(dest)) {
            byte[] head = peek.readNBytes(16);
            if (head.length > 0 && head[0] == '<') {
                String s = new String(head, StandardCharsets.UTF_8);
                throw new IOException("Downloaded content looks like XML/error, not a zip. Snippet=" + s);
            }
        }
    }

    private static String readSnippet(InputStream in, int maxBytes) throws IOException {
        byte[] buf = new byte[maxBytes];
        int read = in.readNBytes(buf, 0, maxBytes);
        return new String(buf, 0, read, StandardCharsets.UTF_8).replaceAll("\\s+", " ");
    }

    private Path artifactDir(String submissionId, String recordId, String deliveryId) {
        String baseDir = storageProperties.getBaseDir();
        return Paths.get(baseDir)
            .resolve(sanitizePathSegment(submissionId))
            .resolve(sanitizePathSegment(recordId))
            .resolve(sanitizePathSegment(deliveryId));
    }

    private void writeRawPayload(Path base, String rawJson) throws IOException {
        Files.createDirectories(base);
        Files.writeString(base.resolve("webhook_raw.json"), rawJson == null ? "" : rawJson);
    }

    private void writeManifest(Path base, Map<String, ?> manifest) throws IOException {
        Files.createDirectories(base);
        // LinkedHashMap iteration order preserved for readability
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest);
        Files.writeString(base.resolve("manifest.json"), json);
    }

    private String toRelativeUploadPath(Path absolute) {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path norm = absolute.toAbsolutePath().normalize();
        try {
            Path rel = cwd.relativize(norm);
            return rel.toString().replace("\\", "/");
        } catch (Exception e) {
            return norm.toString().replace("\\", "/");
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        String s = v.asText();
        return s == null ? null : s.trim();
    }

    private static String sanitizePathSegment(String s) {
        if (s == null || s.isBlank()) {
            return "unknown";
        }
        return s.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "file.bin";
        }
        String base = name.replace("\\", "/");
        int idx = base.lastIndexOf('/');
        String leaf = idx >= 0 ? base.substring(idx + 1) : base;
        return leaf.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }

    /**
     * Looks up latest manifest for a submission/record by scanning delivery folders.
     */
    public Map<String, Object> findLatestManifest(String submissionId, String recordId) throws IOException {
        Path root = Paths.get(storageProperties.getBaseDir())
            .resolve(sanitizePathSegment(submissionId))
            .resolve(sanitizePathSegment(recordId));

        if (!Files.exists(root)) {
            return null;
        }

        Path latestDir = null;
        Instant latestTime = null;

        try (var stream = Files.list(root)) {
            for (Path p : stream.toList()) {
                if (!Files.isDirectory(p)) {
                    continue;
                }
                Path manifest = p.resolve("manifest.json");
                if (!Files.exists(manifest)) {
                    continue;
                }
                Instant t = Files.getLastModifiedTime(manifest).toInstant();
                if (latestTime == null || t.isAfter(latestTime)) {
                    latestTime = t;
                    latestDir = p;
                }
            }
        }

        if (latestDir == null) {
            return null;
        }

        String json = Files.readString(latestDir.resolve("manifest.json"));
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.readValue(json, LinkedHashMap.class);
        map.put("artifactDirectory", toRelativeUploadPath(latestDir));
        return map;
    }
}
