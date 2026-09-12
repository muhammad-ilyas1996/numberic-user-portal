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
import com.numbericsuserportal.ai.util.TaalrInputValidation;
import com.numbericsuserportal.kintsugi.domain.SalesTaxBusinessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
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
        - INVOICE: user wants to create and/or send a NEW invoice (e.g. "invoice Jane $500", "create invoice").
        - INVOICE_LIST: user wants to see invoices or status (e.g. "my invoices", "unpaid invoices", "status of INV-001").
        - INVOICE_RESEND: user wants to resend/remind on an EXISTING invoice (e.g. "resend INV-001", "send reminder", "follow up on invoice").
        - RECEIPT: user mentions saving/uploading a receipt without an image in this message.
        - RECEIPT_LIST: user wants to see saved receipts.
        - RECEIPT_MANUAL: user wants to enter receipt details without uploading a photo.
        - LLC_FORMATION: user wants to start/continue LLC formation, set state/name/owner details, or says LLC automation.
        - LLC_STATUS: user asks status of LLC formation/order.
        - SALES_TAX_FILE: user wants to file/prepare sales tax, enter quarterly sales, start sales tax automation.
        - SALES_TAX_STATUS: user asks sales tax status, filings, due dates, nexus summary.
        - CONFIRM_YES / CONFIRM_NO / CANCEL: explicit confirmation or rejection.
        - CHAT: general questions, greetings, unrelated — NOT when user wants invoice/receipt/llc/sales-tax automation.
        - If user asks "can I create invoice in chat" or wants to create one, use INVOICE not CHAT.
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
     * @param allowIntentLlm when false, skip the Claude intent call (use rules + keywords only).
     *                       Use false for WhatsApp speed and while a draft/confirm session is open.
     */
    public TaalrIntentParseResult parse(String message, boolean awaitingConfirmation, boolean allowIntentLlm) {
        if (message == null || message.isBlank()) {
            return defaultChat();
        }
        String trimmed = message.trim();

        TaalrIntentParseResult ruleBased = parseWithRules(trimmed, awaitingConfirmation);
        if (ruleBased.getIntent() != TaalrIntent.CHAT) {
            return ruleBased;
        }

        // Keywords before Claude — production was misclassifying "create invoice" as CHAT.
        TaalrIntentParseResult keyword = parseWithKeywords(trimmed);
        if (keyword.getIntent() != TaalrIntent.CHAT) {
            return keyword;
        }

        // Fast path: no second LLM round-trip for intent (draft answers like "Jane" / "$500" stay instant)
        if (!allowIntentLlm || !taalrActionProperties.isIntentLlmEnabled()) {
            return keyword;
        }

        if (anthropicProperties.getKey() == null || anthropicProperties.getKey().isBlank()) {
            return keyword;
        }

        try {
            TaalrIntentParseResult claude = parseWithClaude(trimmed, awaitingConfirmation);
            if (claude.getIntent() != TaalrIntent.CHAT) {
                return claude;
            }
            return keyword;
        } catch (Exception e) {
            log.warn("Claude intent parse failed, using keyword fallback: {}", e.getMessage());
            return keyword;
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
        if (lower.contains("manual receipt") || lower.contains("enter receipt")
                || lower.contains("receipt without photo") || lower.contains("receipt manually")
                || lower.contains("add receipt manually")) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.RECEIPT_MANUAL);
            return r;
        }
        if (lower.contains("my receipts") || lower.contains("list receipt") || lower.contains("show receipt")
                || lower.contains("receipt list") || lower.contains("saved receipts")) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.RECEIPT_LIST);
            return r;
        }
        if (containsWord(lower, "receipt") || lower.contains("expense report") || lower.contains("scan expense")) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.RECEIPT);
            return r;
        }
        if (lower.contains("resend") || lower.contains("send again")
                || ((lower.contains("remind") || lower.contains("reminder")
                || lower.contains("follow up") || lower.contains("follow-up"))
                && (containsWord(lower, "invoice") || lower.contains("inv-") || lower.contains("payment")))) {
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
        if (containsWord(lower, "invoice") || lower.contains("bill client") || lower.contains("send bill")
                || lower.contains("create an invoice") || lower.contains("creat an invoice")
                || lower.contains("new invoice") || lower.contains("make an invoice")
                || lower.contains("invoice bna") || lower.contains("invoice ban")) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.INVOICE);
            r.setInvoice(extractInvoiceHeuristic(message));
            return r;
        }
        if (containsWord(lower, "llc")
                || lower.contains("register company") || lower.contains("company formation")
                || lower.contains("llc formation") || lower.contains("form an llc") || lower.contains("start llc")
                || lower.contains("incorporat") || (containsWord(lower, "formation") && lower.contains("compan"))) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            if (lower.contains("status") || lower.contains("llc update")) {
                r.setIntent(TaalrIntent.LLC_STATUS);
            } else {
                r.setIntent(TaalrIntent.LLC_FORMATION);
            }
            r.setLlc(extractLlcHeuristic(message));
            return r;
        }
        if (lower.contains("llc status") || lower.contains("formation status") || lower.contains("company status")) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.LLC_STATUS);
            return r;
        }
        if (isSalesTaxStatus(lower)) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.SALES_TAX_STATUS);
            return r;
        }
        if (isSalesTaxFile(lower)) {
            TaalrIntentParseResult r = new TaalrIntentParseResult();
            r.setIntent(TaalrIntent.SALES_TAX_FILE);
            r.setSalesTax(extractSalesTaxHeuristic(message));
            return r;
        }
        return defaultChat();
    }

    private static boolean isSalesTaxStatus(String lower) {
        return lower.contains("sales tax status") || lower.contains("sales-tax status")
                || (lower.contains("filing status") && lower.contains("tax"))
                || (lower.contains("sales tax") && (lower.contains("status") || lower.contains("due")
                || lower.contains("schedule") || lower.contains("summary") || lower.contains("pending")))
                || lower.contains("my sales tax") || lower.contains("sales tax filings");
    }

    private static boolean isSalesTaxFile(String lower) {
        if (lower.equals("sales tax") || lower.equals("sales-tax") || lower.equals("salestax")
                || lower.equals("sales taxes")) {
            return true;
        }
        return lower.contains("file sales tax") || lower.contains("sales tax filing")
                || lower.contains("file my sales tax") || lower.contains("prepare sales tax")
                || lower.contains("sales tax automation") || lower.contains("start sales tax")
                || lower.contains("sales tax draft") || lower.contains("quarterly sales tax")
                || (containsWord(lower, "sales") && containsWord(lower, "tax")
                && (lower.contains("file") || lower.contains("filing") || lower.contains("remit")
                || lower.contains("nexus") || lower.contains("estimate")));
    }

    private TaalrSalesTaxDraft extractSalesTaxHeuristic(String message) {
        TaalrSalesTaxDraft draft = new TaalrSalesTaxDraft();
        String state = TaalrInputValidation.parseUsState(message);
        if (state != null) {
            draft.setStateCode(state);
        }
        java.util.regex.Matcher amountMatcher = Pattern.compile("\\$?([0-9]+(?:\\.[0-9]{1,2})?)")
                .matcher(message);
        if (amountMatcher.find()) {
            draft.setTaxableAmount(Double.parseDouble(amountMatcher.group(1)));
        }
        String lower = message.toLowerCase(Locale.ROOT);
        for (SalesTaxBusinessType type : SalesTaxBusinessType.values()) {
            if (lower.contains(type.name().replace('_', ' ')) || lower.contains(type.name())) {
                draft.setBusinessType(type.name());
                break;
            }
        }
        return draft;
    }

    private static boolean containsWord(String lowerMessage, String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE)
                .matcher(lowerMessage)
                .find();
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

    private TaalrLlcDraft extractLlcHeuristic(String message) {
        TaalrLlcDraft draft = new TaalrLlcDraft();
        String lower = message.toLowerCase();
        Matcher stateCode = Pattern.compile("\\b([A-Z]{2})\\b").matcher(message);
        while (stateCode.find()) {
            String code = stateCode.group(1).toUpperCase();
            if ("LL".equals(code)) {
                continue;
            }
            // Skip bare "IN" unless clearly a state cue (avoids English "in").
            if ("IN".equals(code)
                    && !(lower.contains("indiana") || lower.contains("state") || lower.contains("jurisdiction")
                    || lower.matches(".*\\bin\\s+in\\b.*") || lower.contains("llc in"))) {
                continue;
            }
            draft.setJurisdiction(code);
            break;
        }
        Matcher forName = Pattern.compile("(?i)(?:named|name|llc name)\\s+([A-Za-z0-9&'\\- ]{3,60})").matcher(message);
        if (forName.find()) {
            draft.setLlcName(forName.group(1).trim());
        }
        Matcher owner = Pattern.compile("(?i)(?:owner|member)\\s+([A-Za-z]+)\\s+([A-Za-z]+)").matcher(message);
        if (owner.find()) {
            draft.setOwnerFirstName(owner.group(1).trim());
            draft.setOwnerLastName(owner.group(2).trim());
        }
        if (lower.contains("expedited")) {
            draft.setFilingSpeed("expedited");
        } else if (lower.contains("same day") || lower.contains("sameday")) {
            draft.setFilingSpeed("sameday");
        } else if (lower.contains("standard")) {
            draft.setFilingSpeed("standard");
        }
        if (lower.contains("ein")) {
            draft.setAddonEin(Boolean.TRUE);
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
