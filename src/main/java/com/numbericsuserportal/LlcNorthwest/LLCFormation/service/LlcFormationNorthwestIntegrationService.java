package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.NorthwestEntityTypes;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.NorthwestPrepareResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationMemberRepository;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRegisteredAgentRepository;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CompanyDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.entity.UserFormationCompany;
import com.numbericsuserportal.LlcNorthwest.companies.repo.UserFormationCompanyRepository;
import com.numbericsuserportal.LlcNorthwest.dto.FilingMethodDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class LlcFormationNorthwestIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(LlcFormationNorthwestIntegrationService.class);

    @Value("${llc.northwest.website-url:www.northwestregisteredagent.com}")
    private String websiteUrl;

    @Value("${llc.northwest.company-entity-type:Limited Liability Company}")
    private String configuredCompanyEntityType;

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Autowired
    private LlcFormationRepository formationRepository;

    @Autowired
    private LlcFormationStateCatalogService stateCatalogService;

    @Autowired
    private UserFormationCompanyRepository userFormationCompanyRepository;

    @Autowired
    private LlcFormationRegisteredAgentRepository registeredAgentRepository;

    @Autowired
    private LlcFormationMemberRepository memberRepository;

    @Autowired
    private NorthwestShoppingCartFormDataBuilder shoppingCartFormDataBuilder;

    @Autowired
    private NorthwestFilingProductSelector filingProductSelector;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates NW company, resolves filing product/method, builds shopping cart — idempotent.
     */
    @Transactional
    public NorthwestPrepareResponseDTO prepare(LlcFormation formation, User user) {
        validateFormationReady(formation);

        String jurisdictionFull = resolveJurisdictionFullName(formation);
        String companyId = ensureCompanyCreated(formation, user, jurisdictionFull);
        resolveFilingIds(formation, companyId, jurisdictionFull);
        buildAndSaveShoppingCart(formation, jurisdictionFull);

        if (!"PAID".equals(formation.getStatus()) && !"SUBMITTED".equals(formation.getStatus())) {
            formation.setStatus("READY_FOR_PAYMENT");
        }
        formation.setFilingStatus("northwest_prepare_complete");
        formationRepository.save(formation);

        return toPrepareResponse(formation, jurisdictionFull,
                "Northwest integration prepared: company and filing IDs resolved.");
    }

    /**
     * Called from Stripe webhook when company may not exist yet.
     */
    @Transactional
    public void ensureReadyForSubmit(LlcFormation formation, User user) {
        if (formation.getCompanyId() != null && !formation.getCompanyId().isBlank()
                && formation.getFilingMethodId() != null && !formation.getFilingMethodId().isBlank()
                && formation.getNorthwestShoppingCartJson() != null && !formation.getNorthwestShoppingCartJson().isBlank()) {
            return;
        }
        try {
            prepare(formation, user);
        } catch (Exception e) {
            log.error("Failed to prepare NW integration for formation {} on payment webhook", formation.getId(), e);
            formation.setFilingStatus(FilingStatusFormatter.failure("northwest_prepare_failed", e.getMessage()));
            formationRepository.save(formation);
            throw e;
        }
    }

    /**
     * Rebuilds cart JSON from latest wizard data (steps 1–4) before NW checkout.
     */
    @Transactional
    public void rebuildShoppingCartJson(LlcFormation formation) {
        if (formation.getCompanyId() == null || formation.getCompanyId().isBlank()
                || formation.getFilingMethodId() == null || formation.getFilingMethodId().isBlank()) {
            return;
        }
        String jurisdictionFull = resolveJurisdictionFullName(formation);
        buildAndSaveShoppingCart(formation, jurisdictionFull);
        formationRepository.save(formation);
    }

    private void validateFormationReady(LlcFormation formation) {
        if (formation.getLlcName() == null || formation.getLlcName().isBlank()) {
            throw new IllegalArgumentException("LLC name is required before Northwest prepare (complete step 2)");
        }
        if (formation.getJurisdiction() == null || formation.getJurisdiction().isBlank()) {
            throw new IllegalArgumentException("Jurisdiction is required before Northwest prepare (complete step 1)");
        }
    }

    private String resolveJurisdictionFullName(LlcFormation formation) {
        String code = formation.getJurisdiction().trim().toUpperCase();
        String full = stateCatalogService.resolveFullStateName(code);
        if (full == null || full.isBlank()) {
            throw new IllegalArgumentException("Unknown state code for Northwest: " + code);
        }
        return full;
    }

    private String ensureCompanyCreated(LlcFormation formation, User user, String jurisdictionFull) {
        if (formation.getCompanyId() != null && !formation.getCompanyId().isBlank()) {
            return formation.getCompanyId().trim();
        }

        RuntimeException lastError = null;
        for (String entityType : companyCreateEntityTypes()) {
            try {
                return createCompanyWithRequest(
                        formation, user, jurisdictionFull, entityType, true);
            } catch (RuntimeException e) {
                if (isInvalidEntityTypeError(e)) {
                    lastError = e;
                    log.warn("NW rejected entity_type '{}' (home_state) for formation {}: {}",
                            entityType, formation.getId(), e.getMessage());
                    continue;
                }
                throw e;
            }
        }

        for (String entityType : companyCreateEntityTypes()) {
            try {
                return createCompanyWithRequest(
                        formation, user, jurisdictionFull, entityType, false);
            } catch (RuntimeException e) {
                if (isInvalidEntityTypeError(e)) {
                    lastError = e;
                    log.warn("NW rejected entity_type '{}' (jurisdictions) for formation {}: {}",
                            entityType, formation.getId(), e.getMessage());
                    continue;
                }
                throw e;
            }
        }

        throw new IllegalStateException(
                "Northwest rejected all entity_type values for company create. "
                        + "Configure llc.northwest.company-entity-type or contact NW support.",
                lastError);
    }

    private String createCompanyWithRequest(
            LlcFormation formation,
            User user,
            String jurisdictionFull,
            String entityType,
            boolean useHomeState) {
        CreateCompanyRequestDTO request = buildCreateCompanyRequest(
                formation, jurisdictionFull, entityType, useHomeState);
        CompaniesResponseDTO response = corporateToolsApiService.createCompanies(request);
        if (response.getResult() == null || response.getResult().isEmpty()
                || response.getResult().get(0).getId() == null) {
            throw new IllegalStateException("Northwest did not return a company id");
        }

        CompanyDTO company = response.getResult().get(0);
        String companyId = company.getId().toString();
        formation.setCompanyId(companyId);
        linkUserToCompany(user, companyId);
        log.info("Created NW company {} for formation {} (entity_type={}, useHomeState={})",
                companyId, formation.getId(), entityType, useHomeState);
        return companyId;
    }

    private List<String> companyCreateEntityTypes() {
        if (configuredCompanyEntityType != null && !configuredCompanyEntityType.isBlank()) {
            String preferred = configuredCompanyEntityType.trim();
            if (NorthwestEntityTypes.COMPANY_CREATE_CANDIDATES.contains(preferred)) {
                return List.of(preferred);
            }
            return List.of(
                    preferred,
                    NorthwestEntityTypes.LLC_DISPLAY,
                    "limited_liability_company",
                    "llc");
        }
        return NorthwestEntityTypes.COMPANY_CREATE_CANDIDATES;
    }

    private CreateCompanyRequestDTO buildCreateCompanyRequest(
            LlcFormation formation, String jurisdictionFull, String entityType, boolean useHomeState) {
        CreateCompanyRequestDTO request = new CreateCompanyRequestDTO();
        request.setDuplicateNameAllowed(false);

        CreateCompanyRequestDTO.CompanyInputDTO input = new CreateCompanyRequestDTO.CompanyInputDTO();
        input.setName(formation.getLlcName().trim());
        input.setEntityType(entityType);
        if (useHomeState) {
            input.setHomeState(jurisdictionFull);
            input.setJurisdictions(null);
        } else {
            input.setHomeState(null);
            input.setJurisdictions(List.of(jurisdictionFull));
        }
        request.setCompanies(List.of(input));
        return request;
    }

    private static boolean isInvalidEntityTypeError(RuntimeException e) {
        String msg = e.getMessage();
        return msg != null && msg.contains("INVALID_ENTITY_TYPE");
    }

    private void linkUserToCompany(User user, String companyId) {
        if (user == null || user.getUserId() == null || companyId == null) {
            return;
        }
        if (!userFormationCompanyRepository.existsByUserIdAndCompanyId(user.getUserId(), companyId)) {
            UserFormationCompany link = new UserFormationCompany();
            link.setUserId(user.getUserId());
            link.setCompanyId(companyId);
            link.setLinkedAt(new Date());
            userFormationCompanyRepository.save(link);
        }
    }

    private void resolveFilingIds(LlcFormation formation, String companyId, String jurisdictionFull) {
        if (formation.getNorthwestCheckoutCompletedAt() != null) {
            return;
        }

        UUID companyUuid = UUID.fromString(companyId);

        FilingProductDTO product = resolveFilingProduct(companyId, jurisdictionFull);
        if (product == null || product.getId() == null) {
            throw new IllegalStateException(
                    "No LLC formation filing product found for " + jurisdictionFull
                            + ". Check NW catalog or set llc.northwest.filing-product-name.");
        }
        formation.setFilingProductId(product.getId().toString());

        UUID filingProductUuid = UUID.fromString(formation.getFilingProductId().trim());
        FilingMethodsResponseDTO methods = corporateToolsApiService.getFilingMethods(
                companyUuid, filingProductUuid, jurisdictionFull);
        FilingMethodDTO method = pickFilingMethod(methods, formation.getFilingSpeed());
        if (method == null || method.getId() == null) {
            throw new IllegalStateException("No filing method found for " + jurisdictionFull);
        }
        formation.setFilingMethodId(method.getId().toString());

        log.info("NW filing resolved for formation {}: product={} ({}) method={} ({})",
                formation.getId(),
                product.getId(),
                product.getName(),
                method.getId(),
                method.getFilingDescription());
    }

    private FilingProductDTO resolveFilingProduct(String companyId, String jurisdictionFull) {
        if (companyId != null && !companyId.isBlank()) {
            try {
                FilingProductsResponseDTO offerings = corporateToolsApiService.getFilingProductsOfferings(
                        companyId, jurisdictionFull);
                FilingProductDTO fromOfferings = pickFilingProduct(offerings);
                if (fromOfferings != null && fromOfferings.getId() != null) {
                    log.info("Resolved NW cart product from offerings for company {}: {} ({})",
                            companyId, fromOfferings.getId(), fromOfferings.getName());
                    return fromOfferings;
                }
            } catch (Exception e) {
                log.warn("NW filing-products/offerings failed for company {}: {}", companyId, e.getMessage());
            }
        }
        for (String entityType : NorthwestEntityTypes.FILING_PRODUCT_CANDIDATES) {
            FilingProductsResponseDTO products = corporateToolsApiService.getFilingProducts(
                    websiteUrl, jurisdictionFull, entityType);
            FilingProductDTO product = pickFilingProduct(products);
            if (product != null && product.getId() != null) {
                return product;
            }
        }
        return null;
    }

    private FilingProductDTO pickFilingProduct(FilingProductsResponseDTO products) {
        if (products == null || products.getResult() == null || products.getResult().isEmpty()) {
            return null;
        }
        return filingProductSelector.pickFormationProduct(products.getResult()).orElse(null);
    }

    private FilingMethodDTO pickFilingMethod(FilingMethodsResponseDTO methods, String filingSpeed) {
        if (methods == null || methods.getResult() == null || methods.getResult().isEmpty()) {
            return null;
        }
        List<FilingMethodDTO> list = filingProductSelector.filterFormationMethods(methods.getResult());
        String speed = filingSpeed == null ? "standard" : filingSpeed.trim().toLowerCase();

        if ("expedited".equals(speed)) {
            return list.stream()
                    .filter(m -> m.getName() != null && m.getName().toLowerCase().contains("exped"))
                    .findFirst()
                    .orElse(list.get(0));
        }
        if ("sameday".equals(speed)) {
            return list.stream()
                    .filter(m -> m.getName() != null && (m.getName().toLowerCase().contains("same day")
                            || m.getName().toLowerCase().contains("same-day")))
                    .findFirst()
                    .orElse(list.get(0));
        }
        return list.get(0);
    }

    /**
     * Returns form_data JSON for POST /order-items/requiring-attention after checkout.
     */
    public JsonNode buildFormDataJson(LlcFormation formation) {
        String jurisdictionFull = resolveJurisdictionFullName(formation);
        var registeredAgent = registeredAgentRepository.findByFormationId(formation.getId()).orElse(null);
        var members = memberRepository.findByFormationIdOrderByIdAsc(formation.getId());
        return shoppingCartFormDataBuilder.build(formation, registeredAgent, members, jurisdictionFull);
    }

    private void buildAndSaveShoppingCart(LlcFormation formation, String jurisdictionFullName) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("company_id", formation.getCompanyId().trim());

        ObjectNode addToCart = objectMapper.createObjectNode();
        addToCart.put("company_id", formation.getCompanyId().trim());
        if (formation.getFilingProductId() != null && !formation.getFilingProductId().isBlank()) {
            addToCart.put("product_id", formation.getFilingProductId().trim());
        }
        addToCart.put("product_option_id", formation.getFilingMethodId().trim());
        addToCart.put("quantity", 1);
        root.set("add_to_cart", addToCart);

        var registeredAgent = registeredAgentRepository.findByFormationId(formation.getId()).orElse(null);
        var members = memberRepository.findByFormationIdOrderByIdAsc(formation.getId());

        ObjectNode formData = shoppingCartFormDataBuilder.build(
                formation, registeredAgent, members, jurisdictionFullName);
        root.set("form_data", formData);

        // Legacy diagnostic shape used by Postman collection step 08.
        ArrayNode items = objectMapper.createArrayNode();
        ObjectNode item = objectMapper.createObjectNode();
        if (formation.getFilingProductId() != null && !formation.getFilingProductId().isBlank()) {
            item.put("product_id", formation.getFilingProductId().trim());
        }
        item.put("quantity", 1);
        item.put("product_option_id", formation.getFilingMethodId().trim());
        item.put("filing_method_id", formation.getFilingMethodId().trim());
        item.set("form_data", formData);
        items.add(item);
        root.set("shopping_cart_items", items);

        try {
            formation.setNorthwestShoppingCartJson(objectMapper.writeValueAsString(root));
            log.info("Shopping cart built for formation {} with {} form_data fields",
                    formation.getId(), formData.size());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build shopping cart JSON", e);
        }
    }

    private NorthwestPrepareResponseDTO toPrepareResponse(LlcFormation formation, String jurisdictionFull, String message) {
        boolean cartReady = formation.getNorthwestShoppingCartJson() != null
                && !formation.getNorthwestShoppingCartJson().isBlank();
        return new NorthwestPrepareResponseDTO(
                formation.getId(),
                formation.getStatus(),
                formation.getCompanyId(),
                formation.getFilingProductId(),
                formation.getFilingMethodId(),
                jurisdictionFull,
                cartReady,
                message
        );
    }
}
