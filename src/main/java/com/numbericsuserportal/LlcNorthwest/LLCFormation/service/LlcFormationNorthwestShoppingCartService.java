package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Step 6 — CorpTools checkout flow after Stripe payment:
 * POST /shopping-cart (flat add) → GET cart → POST checkout → POST /order-items/requiring-attention.
 */
@Service
public class LlcFormationNorthwestShoppingCartService {

    private static final Logger log = LoggerFactory.getLogger(LlcFormationNorthwestShoppingCartService.class);

    @Value("${llc.northwest.payment-method-id:}")
    private String configuredPaymentMethodId;

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Autowired
    private LlcFormationRepository formationRepository;

    @Autowired
    private LlcFormationNorthwestIntegrationService northwestIntegrationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Idempotent: if {@link LlcFormation#getNorthwestCheckoutCompletedAt()} is already set, no-op.
     */
    @Transactional
    public void submitAfterPaymentIfNeeded(LlcFormation formation) {
        if (formation.getNorthwestCheckoutCompletedAt() != null) {
            return;
        }
        if (formation.getCompanyId() == null || formation.getCompanyId().isBlank()) {
            log.warn("Skipping NW checkout for formation {}: missing companyId", formation.getId());
            formation.setFilingStatus("northwest_checkout_skipped_no_company_id");
            formationRepository.save(formation);
            return;
        }
        UUID companyUuid;
        try {
            companyUuid = UUID.fromString(formation.getCompanyId().trim());
        } catch (Exception e) {
            log.warn("Skipping NW checkout for formation {}: invalid companyId", formation.getId());
            formation.setFilingStatus("northwest_checkout_skipped_invalid_company_id");
            formationRepository.save(formation);
            return;
        }

        String storedCartJson = formation.getNorthwestShoppingCartJson();
        try {
            northwestIntegrationService.rebuildShoppingCartJson(formation);
            formation = formationRepository.findById(formation.getId()).orElse(formation);
            storedCartJson = formation.getNorthwestShoppingCartJson();
        } catch (Exception e) {
            log.warn("Could not rebuild shopping cart for formation {}: {}", formation.getId(), e.getMessage());
        }

        String companyId = formation.getCompanyId().trim();
        JsonNode formData = resolveFormData(formation, storedCartJson);
        String addToCartBody = buildAddToCartBody(formation, storedCartJson, formData);

        try {
            assertNwSuccess(corporateToolsApiService.shoppingCartPost(addToCartBody), "shopping cart POST");

            String shoppingCartItemId = null;
            try {
                JsonNode cartGet = corporateToolsApiService.shoppingCartGet(List.of(companyUuid));
                assertNwSuccess(cartGet, "shopping cart GET");
                shoppingCartItemId = findShoppingCartItemId(cartGet, formation);
            } catch (Exception getEx) {
                log.warn("Shopping cart GET failed for formation {}; checkout will use company_ids only: {}",
                        formation.getId(), getEx.getMessage());
            }

            String checkoutBody = buildCheckoutBody(formation, shoppingCartItemId);
            assertNwSuccess(corporateToolsApiService.shoppingCartCheckoutPost(checkoutBody), "shopping cart checkout");

            JsonNode attentionGet = corporateToolsApiService.getOrderItemsRequiringAttention(companyUuid);
            assertNwSuccess(attentionGet, "order items requiring attention GET");
            String orderItemId = findOrderItemId(attentionGet, companyId);

            String formBody = buildOrderItemFormBody(companyId, orderItemId, formData);
            assertNwSuccess(
                    corporateToolsApiService.postOrderItemsRequiringAttention(formBody),
                    "order items requiring attention POST");

            formation.setNorthwestCheckoutCompletedAt(OffsetDateTime.now());
            formation.setStatus("SUBMITTED");
            formation.setFilingStatus("northwest_checkout_complete");
            formationRepository.save(formation);
            log.info("Northwest checkout completed for formation {}", formation.getId());
        } catch (Exception e) {
            log.error("Northwest checkout failed for formation {}", formation.getId(), e);
            persistCheckoutFailure(formation, e);
            throw new IllegalStateException(
                    FilingStatusFormatter.failure("northwest_checkout_failed", e.getMessage()), e);
        }
    }

    private void persistCheckoutFailure(LlcFormation formation, Exception e) {
        try {
            formation.setFilingStatus(FilingStatusFormatter.failure("northwest_checkout_failed", e.getMessage()));
            formationRepository.save(formation);
        } catch (Exception saveEx) {
            log.error("Could not persist filing_status for formation {}", formation.getId(), saveEx);
        }
    }

    private String buildAddToCartBody(LlcFormation formation, String storedCartJson, JsonNode formData) {
        if (storedCartJson != null && !storedCartJson.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(storedCartJson);
                if (root.has("add_to_cart") && root.get("add_to_cart").isObject()) {
                    return root.get("add_to_cart").toString();
                }
            } catch (Exception e) {
                log.warn("Could not parse add_to_cart from stored cart JSON for formation {}: {}",
                        formation.getId(), e.getMessage());
            }
        }
        return buildFallbackAddToCartBody(formation);
    }

    private JsonNode resolveFormData(LlcFormation formation, String storedCartJson) {
        if (storedCartJson != null && !storedCartJson.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(storedCartJson);
                if (root.has("form_data") && root.get("form_data").isObject()) {
                    return root.get("form_data");
                }
            } catch (Exception e) {
                log.warn("Could not parse form_data from stored cart JSON for formation {}: {}",
                        formation.getId(), e.getMessage());
            }
        }
        return northwestIntegrationService.buildFormDataJson(formation);
    }

    /**
     * CorpTools POST /shopping-cart expects a flat body (not shopping_cart_items).
     */
    private String buildFallbackAddToCartBody(LlcFormation formation) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("company_id", formation.getCompanyId().trim());
        if (formation.getFilingProductId() != null && !formation.getFilingProductId().isBlank()) {
            body.put("product_id", formation.getFilingProductId().trim());
        }
        if (formation.getFilingMethodId() != null && !formation.getFilingMethodId().isBlank()) {
            body.put("product_option_id", formation.getFilingMethodId().trim());
        }
        body.put("quantity", 1);
        return body.toString();
    }

    private String buildCheckoutBody(LlcFormation formation, String shoppingCartItemId) {
        ObjectNode checkout = objectMapper.createObjectNode();

        if (shoppingCartItemId != null && !shoppingCartItemId.isBlank()) {
            ArrayNode itemIds = objectMapper.createArrayNode();
            itemIds.add(shoppingCartItemId);
            checkout.set("item_ids", itemIds);
        }

        ArrayNode companyIds = objectMapper.createArrayNode();
        companyIds.add(formation.getCompanyId().trim());
        checkout.set("company_ids", companyIds);

        if (formation.getFilingMethodId() != null && !formation.getFilingMethodId().isBlank()) {
            checkout.put("product_option_id", formation.getFilingMethodId().trim());
        }

        String paymentToken = resolvePaymentToken();
        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalStateException(buildMissingPaymentTokenMessage());
        }
        checkout.put("payment_token", paymentToken.trim());
        return checkout.toString();
    }

    private String buildMissingPaymentTokenMessage() {
        return "NW payment_token is required for checkout. "
                + "Stripe payment does not replace NW wholesaler billing. "
                + "1) GET /api/llc-northwest/payment-methods — if empty, "
                + "2) POST /api/llc-northwest/payment-methods with a valid card, "
                + "3) set server config llc.northwest.payment-method-id=<uuid from GET>.";
    }

    private String resolvePaymentToken() {
        if (configuredPaymentMethodId != null && !configuredPaymentMethodId.isBlank()) {
            log.info("Using configured NW payment_token from llc.northwest.payment-method-id");
            return configuredPaymentMethodId.trim();
        }
        try {
            PaymentMethodsResponseDTO paymentMethods = corporateToolsApiService.getPaymentMethods();
            if (Boolean.FALSE.equals(paymentMethods.getSuccess())) {
                log.warn("NW GET /payment-methods returned success=false");
                return null;
            }
            if (paymentMethods.getResult() != null && !paymentMethods.getResult().isEmpty()) {
                for (var method : paymentMethods.getResult()) {
                    if (method.getId() != null) {
                        log.info("Using NW account payment method {} (brand={}, last4={})",
                                method.getId(), method.getBrand(), method.getLast4());
                        return method.getId().toString();
                    }
                }
            }
            log.warn("NW account has no saved payment methods");
        } catch (Exception e) {
            log.warn("Could not resolve NW payment method from account: {}", e.getMessage());
        }
        return null;
    }

    private String buildOrderItemFormBody(String companyId, String orderItemId, JsonNode formData) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("company_id", companyId);
        body.put("order_item_id", orderItemId);
        body.set("form_data", formData);
        return body.toString();
    }

    private String findShoppingCartItemId(JsonNode response, LlcFormation formation) {
        if (!response.has("result") || !response.get("result").isArray()) {
            throw new IllegalStateException("Shopping cart GET returned no items");
        }
        String productId = formation.getFilingProductId();
        String optionId = formation.getFilingMethodId();
        String companyId = formation.getCompanyId().trim();

        for (JsonNode item : response.get("result")) {
            if (!item.has("id")) {
                continue;
            }
            if (item.has("status") && !"active".equalsIgnoreCase(item.get("status").asText())) {
                continue;
            }
            if (matchesField(item, "product_id", productId)
                    && matchesField(item, "product_option_id", optionId)
                    && matchesField(item, "company_id", companyId)) {
                return item.get("id").asText();
            }
        }
        for (JsonNode item : response.get("result")) {
            if (item.has("id") && matchesField(item, "company_id", companyId)) {
                return item.get("id").asText();
            }
        }
        throw new IllegalStateException("No matching shopping cart item found for formation " + formation.getId());
    }

    private String findOrderItemId(JsonNode response, String companyId) {
        if (!response.has("result") || !response.get("result").isArray() || response.get("result").isEmpty()) {
            throw new IllegalStateException("No order items requiring attention found");
        }
        for (JsonNode item : response.get("result")) {
            if (item.has("id") && matchesField(item, "company_id", companyId)) {
                return item.get("id").asText();
            }
        }
        JsonNode first = response.get("result").get(0);
        if (first.has("id")) {
            return first.get("id").asText();
        }
        throw new IllegalStateException("Order items requiring attention response had no id");
    }

    private boolean matchesField(JsonNode item, String field, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        return item.has(field) && expected.equals(item.get(field).asText());
    }

    private void assertNwSuccess(JsonNode response, String operation) {
        if (response == null) {
            throw new IllegalStateException("Northwest " + operation + " returned no response");
        }
        if (response.has("httpStatus")) {
            throw new IllegalStateException("Northwest " + operation + " failed: " + response);
        }
        if (response.isEmpty()) {
            throw new IllegalStateException("Northwest " + operation + " returned an empty response");
        }
        if (response.has("success") && !response.get("success").asBoolean()) {
            String detail = response.has("result") ? response.get("result").toString() : response.toString();
            throw new IllegalStateException("Northwest " + operation + " failed: " + detail);
        }
        if (response.has("error") && (!response.has("success") || response.get("success").isNull())) {
            throw new IllegalStateException("Northwest " + operation + " failed: " + response);
        }
    }
}
