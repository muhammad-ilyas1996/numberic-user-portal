package com.numbericsuserportal.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.ai.config.AnthropicProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Claude Managed Agents: {@code POST /v1/sessions}, {@code POST .../events}, {@code GET .../events}.
 * Docs: <a href="https://docs.anthropic.com/en/api/overview">API overview</a>
 */
@Component
public class ManagedAgentsClient {

    @Autowired
    private AnthropicProperties props;

    @Autowired
    private RestClient anthropicRestClient;

    @Autowired
    private ObjectMapper objectMapper;

    public String createSession(String agentId, String environmentId, Map<String, String> metadata) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("agent", agentId);
        body.put("environment_id", environmentId);
        if (metadata != null && !metadata.isEmpty()) {
            body.put("metadata", metadata);
        }
        String raw = anthropicRestClient.post()
                .uri("/v1/sessions")
                .headers(this::managedAgentHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        JsonNode root = objectMapper.readTree(raw);
        if (root.has("error")) {
            throw new IllegalStateException(root.path("error").path("message").asText("Session create failed"));
        }
        return root.path("id").asText(null);
    }

    public void sendUserTextMessage(String sessionId, String userText) throws Exception {
        Map<String, Object> textBlock = new LinkedHashMap<>();
        textBlock.put("type", "text");
        textBlock.put("text", userText);
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(textBlock);
        Map<String, Object> ev = new LinkedHashMap<>();
        ev.put("type", "user.message");
        ev.put("content", content);
        Map<String, Object> body = Map.of("events", List.of(ev));

        String raw = anthropicRestClient.post()
                .uri("/v1/sessions/{sessionId}/events", sessionId)
                .headers(this::managedAgentHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        JsonNode root = objectMapper.readTree(raw);
        if (root.has("error")) {
            throw new IllegalStateException(root.path("error").path("message").asText("Send event failed"));
        }
    }

    /**
     * After {@link #sendUserTextMessage}, polls until the newest {@code agent.message} text <strong>differs</strong>
     * from the snapshot taken immediately after the send. Otherwise the API still returns the previous turn's reply
     * while the model is generating, and callers would incorrectly return duplicate content.
     */
    public String pollUntilNewAssistantReply(String sessionId, int maxAttempts, long sleepMs) throws Exception {
        String baseline = fetchLatestAgentMessageText(sessionId);
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            if (attempt > 0) {
                Thread.sleep(sleepMs);
            }
            String current = fetchLatestAgentMessageText(sessionId);
            if (current == null || current.isBlank()) {
                continue;
            }
            if (baseline == null) {
                return current.trim();
            }
            if (!current.trim().equals(baseline.trim())) {
                return current.trim();
            }
        }
        return null;
    }

    private String fetchLatestAgentMessageText(String sessionId) throws Exception {
        String raw = anthropicRestClient.get()
                .uri("/v1/sessions/{sessionId}/events?order=desc&limit=40", sessionId)
                .headers(this::managedAgentHeaders)
                .retrieve()
                .body(String.class);
        JsonNode root = objectMapper.readTree(raw);
        if (root.has("error")) {
            return null;
        }
        JsonNode data = root.path("data");
        if (!data.isArray()) {
            return null;
        }
        for (JsonNode ev : data) {
            if ("agent.message".equals(ev.path("type").asText())) {
                return joinTextBlocks(ev.path("content"));
            }
        }
        return null;
    }

    private static String joinTextBlocks(JsonNode contentArr) {
        if (!contentArr.isArray() || contentArr.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode block : contentArr) {
            if ("text".equals(block.path("type").asText())) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(block.path("text").asText(""));
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private void managedAgentHeaders(org.springframework.http.HttpHeaders h) {
        h.set("x-api-key", props.getKey());
        h.set("anthropic-version", props.getVersion());
        h.set("anthropic-beta", props.getManagedAgentsBeta());
    }
}
