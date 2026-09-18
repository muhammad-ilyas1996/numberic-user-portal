package com.numbericsuserportal.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.ai.action.TaalrIntent;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrInvoiceDraft;
import com.numbericsuserportal.ai.action.dto.TaalrLlcDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSalesTaxDraft;
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
import java.util.regex.Pattern;

@Service
@Slf4j
public class TaalrIntentParserService {

    private static final Pattern YES_PATTERN = Pattern.compile(
            "^(yes|y|yeah|yep|yup|ok|okay|sure|alright|confirm|proceed|go ahead|send it|do it)\\b.*",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern NO_PATTERN = Pattern.compile(
            "^(no|n|nope|nah|discard|don't|dont|no thanks|cancel that)(\\s*[.!])?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CANCEL_PATTERN = Pattern.compile(
            "^(cancel|nevermind|never mind|abort|stop)(\\s+.*)?$",
            Pattern.CASE_INSENSITIVE);

    private static final String INTENT_SYSTEM = """
        You are Taalr intent parser for Numbrics. Classify the user message for automation.
        Reply with ONLY valid JSON (no markdown fences). Schema:
        {
          "intent": "CHAT" | "RECEIPT" | "RECEIPT_LIST" | "RECEIPT_MANUAL" | "INVOICE" | "INVOICE_LIST" | "INVOICE_RESEND" | "LLC_FORMATION" | "LLC_STATUS" | "SALES_TAX_FILE" | "SALES_TAX_STATUS" | "CONFIRM_YES" | "CONFIRM_NO" | "CANCEL",
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
          "llc": {
            "jurisdiction": string or null,
            "llcName": string or null,
            "ownerFirstName": string or null,
            "ownerLastName": string or null,
            "filingSpeed": "standard" | "expedited" | "sameday" or null,
            "addonEin": boolean or null
          },
          "salesTax": {
            "stateCode": string or null,
            "businessType": string or null,
            "category": string or null,
            "subcategory": string or null,
            "taxableAmount": number or null,
            "exemptAmount": number or null,
            "city": string or null,
            "postalCode": string or null
          },
          "missingField": string or null,
          "question": string or null,
          "statusFilter": "ALL" | "UNPAID" | "PAID" | "DRAFT" or null
        }
        Rules:
        - Understand natural language in any phrasing (formal, casual, typos, Urdu-English mix). Do NOT require exact keywords.
        - INVOICE: create/send a NEW invoice.
        - INVOICE_LIST: list or filter invoices / payment status (including unpaid, outstanding, overdue, paid, draft). Set statusFilter when clear.
        - INVOICE_RESEND: resend/remind on an EXISTING invoice.
        - RECEIPT / RECEIPT_LIST / RECEIPT_MANUAL: receipt save, list, or manual entry.
        - LLC_FORMATION / LLC_STATUS: start formation or ask formation status.
        - SALES_TAX_FILE / SALES_TAX_STATUS: file/prepare sales tax or ask filing status.
        - CONFIRM_YES / CONFIRM_NO / CANCEL: explicit confirmation or rejection.
        - CHAT: guidance/questions that are NOT an automation action above.
        - Prefer automation intents over CHAT when the user clearly wants to do or see something in-product.
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
        return parse(message, awaitingConfirmation, true);
    }

    /**
     * @param allowIntentLlm when false, skip Claude intent (e.g. open draft session — short field answers).
     *                       When true, natural language is classified by Claude (agentic), not keyword lists.
     */
    public TaalrIntentParseResult parse(String message, boolean awaitingConfirmation, boolean allowIntentLlm) {
        if (message == null || message.isBlank()) {
            return defaultChat();
        }
        String trimmed = message.trim();

        // Structural only: yes / no / cancel — not product vocabulary
        TaalrIntentParseResult ruleBased = parseWithRules(trimmed, awaitingConfirmation);
        if (ruleBased.getIntent() != TaalrIntent.CHAT) {
            return ruleBased;
        }

        if (!allowIntentLlm || !taalrActionProperties.isIntentLlmEnabled()) {
            return defaultChat();
        }

        if (anthropicProperties.getKey() == null || anthropicProperties.getKey().isBlank()) {
            log.warn("Intent LLM skipped: ANTHROPIC_API_KEY missing");
            return defaultChat();
        }

        try {
            return parseWithClaude(trimmed, awaitingConfirmation);
        } catch (Exception e) {
            log.warn("Claude intent parse failed, falling back to CHAT: {}", e.getMessage());
            return defaultChat();
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
        JsonNode llc = node.path("llc");
        if (!llc.isMissingNode() && !llc.isNull()) {
            TaalrLlcDraft draft = new TaalrLlcDraft();
            if (llc.hasNonNull("jurisdiction")) {
                draft.setJurisdiction(llc.path("jurisdiction").asText(null));
            }
            if (llc.hasNonNull("llcName")) {
                draft.setLlcName(llc.path("llcName").asText(null));
            }
            if (llc.hasNonNull("ownerFirstName")) {
                draft.setOwnerFirstName(llc.path("ownerFirstName").asText(null));
            }
            if (llc.hasNonNull("ownerLastName")) {
                draft.setOwnerLastName(llc.path("ownerLastName").asText(null));
            }
            if (llc.hasNonNull("filingSpeed")) {
                draft.setFilingSpeed(llc.path("filingSpeed").asText(null));
            }
            if (llc.has("addonEin") && !llc.path("addonEin").isNull()) {
                draft.setAddonEin(llc.path("addonEin").asBoolean());
            }
            result.setLlc(draft);
        }
        JsonNode salesTax = node.path("salesTax");
        if (!salesTax.isMissingNode() && !salesTax.isNull()) {
            TaalrSalesTaxDraft draft = new TaalrSalesTaxDraft();
            if (salesTax.hasNonNull("stateCode")) {
                draft.setStateCode(salesTax.path("stateCode").asText(null));
            }
            if (salesTax.hasNonNull("businessType")) {
                draft.setBusinessType(salesTax.path("businessType").asText(null));
            }
            if (salesTax.hasNonNull("category")) {
                draft.setCategory(salesTax.path("category").asText(null));
            }
            if (salesTax.hasNonNull("subcategory")) {
                draft.setSubcategory(salesTax.path("subcategory").asText(null));
            }
            if (salesTax.has("taxableAmount") && !salesTax.path("taxableAmount").isNull()) {
                draft.setTaxableAmount(salesTax.path("taxableAmount").asDouble());
            }
            if (salesTax.has("exemptAmount") && !salesTax.path("exemptAmount").isNull()) {
                draft.setExemptAmount(salesTax.path("exemptAmount").asDouble());
            }
            if (salesTax.hasNonNull("city")) {
                draft.setCity(salesTax.path("city").asText(null));
            }
            if (salesTax.hasNonNull("postalCode")) {
                draft.setPostalCode(salesTax.path("postalCode").asText(null));
            }
            result.setSalesTax(draft);
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
