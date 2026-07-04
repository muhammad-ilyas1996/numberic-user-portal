package com.numbericsuserportal.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.ai.action.TaalrIntent;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrInvoiceDraft;
import com.numbericsuserportal.ai.config.AnthropicProperties;
import com.numbericsuserportal.ai.config.TaalrActionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TaalrIntentParserService {

    private static final Pattern YES_PATTERN = Pattern.compile(
            "^(yes|y|yeah|yep|confirm|ok|okay|save|send|proceed|go ahead|sure)\\b.*",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern NO_PATTERN = Pattern.compile(
            "^(no|n|nope|discard|don't|dont)(\\s*[.!])?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CANCEL_PATTERN = Pattern.compile(
            "^(cancel|nevermind|never mind|abort|stop)(\\s+.*)?$",
            Pattern.CASE_INSENSITIVE);

    private static final String INTENT_SYSTEM = """
        You are Taalr intent parser for Numbrics. Classify the user message for automation.
        Reply with ONLY valid JSON (no markdown fences). Schema:
        {
          "intent": "CHAT" | "RECEIPT" | "INVOICE" | "INVOICE_LIST" | "INVOICE_RESEND" | "CONFIRM_YES" | "CONFIRM_NO" | "CANCEL",
          "invoice": {
            "customerName": string or null,
            "customerEmail": string or null,
            "customerPhone": string or null,
            "amount": number or null,
            "description": string or null,
            "channel": "WHATSAPP" | "EMAIL" or null,
            "invoiceId": number or null,
            "invoiceNum": string or null,
            "recipientPhoneOrEmail": string or null
          },
          "missingField": string or null,
          "question": string or null,
          "statusFilter": "ALL" | "UNPAID" | "PAID" | "DRAFT" or null
        }
        Rules:
        - INVOICE: user wants to create and/or send a NEW invoice (e.g. "invoice Jane $500", "create invoice").
        - INVOICE_LIST: user wants to see invoices or status (e.g. "my invoices", "unpaid invoices", "status of INV-001").
        - INVOICE_RESEND: user wants to resend/remind on an EXISTING invoice (e.g. "resend INV-001", "send reminder", "follow up on invoice").
        - RECEIPT: user mentions saving/uploading a receipt without an image in this message.
        - CONFIRM_YES / CONFIRM_NO / CANCEL: explicit confirmation or rejection.
        - CHAT: general questions, taxes, greetings, unrelated.
        - Extract amounts as numbers without currency symbols. Default channel WHATSAPP if phone mentioned, EMAIL if email mentioned.
        - If INVOICE intent but customer name or amount missing, set missingField and a short question.
        """;

    @Autowired
    private AnthropicProperties anthropicProperties;

    @Autowired
    private TaalrActionProperties taalrActionProperties;

    @Autowired
    private RestClient anthropicRestClient;

    @Autowired
    private ObjectMapper objectMapper;

    public TaalrIntentParseResult parse(String message, boolean awaitingConfirmation) {
        if (message == null || message.isBlank()) {
            return defaultChat();
        }
        String trimmed = message.trim();

        TaalrIntentParseResult ruleBased = parseWithRules(trimmed, awaitingConfirmation);
        if (ruleBased.getIntent() != TaalrIntent.CHAT) {
            return ruleBased;
        }

        if (anthropicProperties.getKey() == null || anthropicProperties.getKey().isBlank()) {
            return parseWithKeywords(trimmed);
        }

        try {
            return parseWithClaude(trimmed, awaitingConfirmation);
        } catch (Exception e) {
            log.warn("Claude intent parse failed, using keyword fallback: {}", e.getMessage());
            return parseWithKeywords(trimmed);
        }
    }

    private TaalrIntentParseResult parseWithRules(String message, boolean awaitingConfirmation) {
        if (CANCEL_PATTERN.matcher(message).matches()) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.CANCEL);
            return r;
        }
        if (awaitingConfirmation) {
            if (YES_PATTERN.matcher(message).matches()) {
                TaalrIntentParseResult r = new TaalrIntentParseResult();
                r.setIntent(TaalrIntent.CONFIRM_YES);
                return r;
            }
            if (NO_PATTERN.matcher(message).matches()) {
                TaalrIntentParseResult r = new TaalrIntentParseResult();
                r.setIntent(TaalrIntent.CONFIRM_NO);
                return r;
            }
        }
        return defaultChat();
    }

    private TaalrIntentParseResult parseWithKeywords(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("receipt") || lower.contains("expense")) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.RECEIPT);
            return r;
        }
        if (lower.contains("resend") || lower.contains("send again") || lower.contains("remind")
                || lower.contains("reminder") || lower.contains("follow up") || lower.contains("follow-up")) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.INVOICE_RESEND);
            r.setInvoice(extractInvoiceHeuristic(message));
            extractInvoiceNumToDraft(message, r.getInvoice());
            return r;
        }
        if (lower.contains("my invoices") || lower.contains("list invoice") || lower.contains("show invoice")
                || lower.contains("invoice list") || lower.contains("invoice status")
                || lower.contains("unpaid invoice") || lower.contains("outstanding invoice")
                || (lower.contains("status") && lower.contains("inv-"))) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.INVOICE_LIST);
            r.setInvoice(extractInvoiceHeuristic(message));
            extractInvoiceNumToDraft(message, r.getInvoice());
            if (lower.contains("unpaid") || lower.contains("outstanding") || lower.contains("overdue")) {
                r.setStatusFilter("UNPAID");
            } else if (lower.contains("paid")) {
                r.setStatusFilter("PAID");
            } else if (lower.contains("draft")) {
                r.setStatusFilter("DRAFT");
            }
            return r;
        }
        if (lower.contains("invoice") || lower.contains("bill client") || lower.contains("send bill")) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.INVOICE);
            r.setInvoice(extractInvoiceHeuristic(message));
            return r;
        }
        return defaultChat();
    }

    private static void extractInvoiceNumToDraft(String message, TaalrInvoiceDraft draft) {
        if (message == null || draft == null) {
            return;
        }
        Matcher m = Pattern.compile("\\b(INV-[A-Za-z0-9\\-]+)\\b", Pattern.CASE_INSENSITIVE).matcher(message);
        if (m.find()) {
            draft.setInvoiceNum(m.group(1).toUpperCase());
        }
    }

    private TaalrInvoiceDraft extractInvoiceHeuristic(String message) {
        TaalrInvoiceDraft draft = new TaalrInvoiceDraft();
        java.util.regex.Matcher amountMatcher = Pattern.compile("\\$?([0-9]+(?:\\.[0-9]{1,2})?)")
                .matcher(message);
        if (amountMatcher.find()) {
            draft.setAmount(Double.parseDouble(amountMatcher.group(1)));
        }
        java.util.regex.Matcher emailMatcher = Pattern.compile("[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}")
                .matcher(message);
        if (emailMatcher.find()) {
            draft.setCustomerEmail(emailMatcher.group());
            draft.setRecipientPhoneOrEmail(emailMatcher.group());
            draft.setChannel("EMAIL");
        }
        return draft;
    }

    private TaalrIntentParseResult parseWithClaude(String message, boolean awaitingConfirmation) throws Exception {
        String userPrompt = awaitingConfirmation
                ? "User is replying to a confirmation prompt. Message: " + message
                : "User message: " + message;

        Map<String, Object> body = new LinkedHashMap<>();
        String model = taalrActionProperties.getIntentModel();
        if (model == null || model.isBlank()) {
            model = anthropicProperties.getModel();
        }
        body.put("model", model);
        body.put("max_tokens", taalrActionProperties.getIntentMaxTokens());
        body.put("system", INTENT_SYSTEM);
        body.put("messages", List.of(Map.of("role", "user", "content", userPrompt)));

        String raw = anthropicRestClient.post()
                .uri("/v1/messages")
                .header("x-api-key", anthropicProperties.getKey())
                .header("anthropic-version", anthropicProperties.getVersion())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(raw);
        if (root.has("error")) {
            throw new IllegalStateException(root.path("error").path("message").asText("Anthropic error"));
        }
        String text = root.path("content").get(0).path("text").asText("");
        text = text.trim();
        if (text.startsWith("```")) {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            if (start >= 0 && end > start) {
                text = text.substring(start, end + 1);
            }
        }
        return mapJsonToResult(objectMapper.readTree(text));
    }

    private TaalrIntentParseResult mapJsonToResult(JsonNode node) {
        TaalrIntentParseResult result = new TaalrIntentParseResult();
        String intentStr = node.path("intent").asText("CHAT");
        try {
            result.setIntent(TaalrIntent.valueOf(intentStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            result.setIntent(TaalrIntent.CHAT);
        }
        JsonNode inv = node.path("invoice");
        if (!inv.isMissingNode() && !inv.isNull()) {
            TaalrInvoiceDraft draft = new TaalrInvoiceDraft();
            if (inv.hasNonNull("customerName")) {
                draft.setCustomerName(inv.path("customerName").asText(null));
            }
            if (inv.hasNonNull("customerEmail")) {
                draft.setCustomerEmail(inv.path("customerEmail").asText(null));
            }
            if (inv.hasNonNull("customerPhone")) {
                draft.setCustomerPhone(inv.path("customerPhone").asText(null));
            }
            if (inv.has("amount") && !inv.path("amount").isNull()) {
                draft.setAmount(inv.path("amount").asDouble());
            }
            if (inv.hasNonNull("description")) {
                draft.setDescription(inv.path("description").asText(null));
            }
            if (inv.hasNonNull("channel")) {
                draft.setChannel(inv.path("channel").asText(null));
            }
            if (inv.has("invoiceId") && !inv.path("invoiceId").isNull()) {
                draft.setInvoiceId(inv.path("invoiceId").asLong());
            }
            if (inv.hasNonNull("invoiceNum")) {
                draft.setInvoiceNum(inv.path("invoiceNum").asText(null));
            }
            if (inv.hasNonNull("recipientPhoneOrEmail")) {
                draft.setRecipientPhoneOrEmail(inv.path("recipientPhoneOrEmail").asText(null));
            }
            result.setInvoice(draft);
        }
        if (node.hasNonNull("missingField")) {
            result.setMissingField(node.path("missingField").asText(null));
        }
        if (node.hasNonNull("question")) {
            result.setQuestion(node.path("question").asText(null));
        }
        if (node.hasNonNull("statusFilter")) {
            result.setStatusFilter(node.path("statusFilter").asText(null));
        }
        return result;
    }

    private static TaalrIntentParseResult defaultChat() {
        TaalrIntentParseResult r = new TaalrIntentParseResult();
        r.setIntent(TaalrIntent.CHAT);
        return r;
    }
}
