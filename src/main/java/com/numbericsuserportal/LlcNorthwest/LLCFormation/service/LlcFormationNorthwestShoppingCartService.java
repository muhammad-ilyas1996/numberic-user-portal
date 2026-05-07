package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Step 6 — POST shopping cart, verify cart, checkout (backend-only; invoked after Stripe payment succeeds).
 */
@Service
public class LlcFormationNorthwestShoppingCartService {

    private static final Logger log = LoggerFactory.getLogger(LlcFormationNorthwestShoppingCartService.class);

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Autowired
    private LlcFormationRepository formationRepository;

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

        String cartBody = formation.getNorthwestShoppingCartJson();
        if (cartBody == null || cartBody.isBlank()) {
            cartBody = buildFallbackCartBody(formation);
        }

        try {
            corporateToolsApiService.shoppingCartPost(cartBody);
            corporateToolsApiService.shoppingCartGet(List.of(companyUuid));
            corporateToolsApiService.shoppingCartCheckoutPost("{}");

            formation.setNorthwestCheckoutCompletedAt(OffsetDateTime.now());
            formation.setStatus("SUBMITTED");
            formation.setFilingStatus("northwest_checkout_complete");
            formationRepository.save(formation);
        } catch (Exception e) {
            log.error("Northwest checkout failed for formation {}", formation.getId(), e);
            formation.setFilingStatus("northwest_checkout_failed: " + e.getMessage());
            formationRepository.save(formation);
        }
    }

    /**
     * Best-effort default body when the client has not stored an exact NW payload yet.
     * Corporate Tools usually requires richer form_data from filing-methods/schemas.
     */
    private String buildFallbackCartBody(LlcFormation formation) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("company_id", formation.getCompanyId().trim());
        ArrayNode items = objectMapper.createArrayNode();
        ObjectNode item = objectMapper.createObjectNode();
        if (formation.getFilingMethodId() != null && !formation.getFilingMethodId().isBlank()) {
            item.put("filing_method_id", formation.getFilingMethodId().trim());
        }
        item.set("form_data", objectMapper.createObjectNode());
        items.add(item);
        root.set("shopping_cart_items", items);
        return root.toString();
    }
}
