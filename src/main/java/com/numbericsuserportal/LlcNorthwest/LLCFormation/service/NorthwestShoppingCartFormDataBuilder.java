package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationMember;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRegisteredAgent;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO.SchemaFieldDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Builds shopping-cart form_data aligned to NW GET /filing-methods/schemas (schema keys only).
 */
@Component
public class NorthwestShoppingCartFormDataBuilder {

    private static final Logger log = LoggerFactory.getLogger(NorthwestShoppingCartFormDataBuilder.class);
    private static final Set<String> SKIP_SCHEMA_FIELDS = Set.of("entity_type", "disclaimer");

    private final CorporateToolsApiService corporateToolsApiService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public NorthwestShoppingCartFormDataBuilder(
            CorporateToolsApiService corporateToolsApiService,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.corporateToolsApiService = corporateToolsApiService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public ObjectNode build(
            LlcFormation formation,
            LlcFormationRegisteredAgent registeredAgent,
            List<LlcFormationMember> members,
            String jurisdictionFullName) {

        String contactEmail = resolveContactEmail(formation);
        FormationContext ctx = FormationContext.from(
                formation, registeredAgent, members, jurisdictionFullName, contactEmail);

        if (formation.getCompanyId() != null && !formation.getCompanyId().isBlank()
                && formation.getFilingMethodId() != null && !formation.getFilingMethodId().isBlank()) {
            try {
                UUID companyId = UUID.fromString(formation.getCompanyId().trim());
                UUID filingMethodId = UUID.fromString(formation.getFilingMethodId().trim());
                FilingMethodSchemaResponseDTO schema = corporateToolsApiService.getFilingMethodSchemas(
                        companyId, filingMethodId);
                if (schema != null && schema.getResult() != null && !schema.getResult().isEmpty()) {
                    ObjectNode schemaOnly = buildFromSchema(schema.getResult(), ctx);
                    log.info("Built schema-only form_data for formation {} ({} keys)",
                            formation.getId(), schemaOnly.size());
                    return schemaOnly;
                }
            } catch (Exception e) {
                log.warn("Could not build schema form_data for formation {}: {}",
                        formation.getId(), e.getMessage());
            }
        }

        return buildMinimalFallback(ctx);
    }

    private String resolveContactEmail(LlcFormation formation) {
        if (formation.getUserId() == null) {
            return null;
        }
        Optional<User> user = userRepository.findById(formation.getUserId());
        return user.map(User::getEmail).filter(e -> e != null && !e.isBlank()).orElse(null);
    }

    private ObjectNode buildFromSchema(List<SchemaFieldDTO> fields, FormationContext ctx) {
        ObjectNode formData = objectMapper.createObjectNode();
        for (SchemaFieldDTO field : fields) {
            if (field.getName() == null || field.getName().isBlank()) {
                continue;
            }
            if (SKIP_SCHEMA_FIELDS.contains(field.getName())) {
                continue;
            }
            var value = valueForSchemaField(field, ctx);
            if (value != null && !value.isNull()) {
                formData.set(field.getName(), value);
            }
        }
        return formData;
    }

    private com.fasterxml.jackson.databind.JsonNode valueForSchemaField(SchemaFieldDTO field, FormationContext ctx) {
        return switch (field.getName()) {
            case "management_type" -> textOrNull(ctx.managementTypeLabel());
            case "official.manager" -> buildOfficialArray(ctx, "Manager");
            case "official.member" -> buildOfficialArray(ctx, "Member");
            case "company_mailing_address" -> buildCompanyAddressNode(ctx);
            case "company_principal_address" -> buildCompanyAddressNode(ctx);
            case "registered_agent" -> buildRegisteredAgentNode(ctx);
            case "filer" -> buildFilerNode(ctx);
            default -> null;
        };
    }

    private com.fasterxml.jackson.databind.JsonNode textOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return objectMapper.getNodeFactory().textNode(value.trim());
    }

    private ArrayNode buildOfficialArray(FormationContext ctx, String role) {
        ArrayNode array = objectMapper.createArrayNode();
        if (ctx.members() != null && !ctx.members().isEmpty()) {
            for (LlcFormationMember member : ctx.members()) {
                array.add(buildOfficialNode(ctx, role, member.getFirstName(), member.getLastName(),
                        member.getOwnershipPct()));
            }
            return array.size() > 0 ? array : null;
        }
        ObjectNode official = buildOfficialNode(
                ctx, role, ctx.ownerFirstName(), ctx.ownerLastName(), ctx.ownershipPct());
        array.add(official);
        return array;
    }

    private ObjectNode buildOfficialNode(
            FormationContext ctx, String role, String firstName, String lastName, Integer ownershipPct) {
        ObjectNode official = objectMapper.createObjectNode();
        official.put("role", role);
        official.put("official_is_company", false);
        putIfPresent(official, "company_name", ctx.llcName());
        putIfPresent(official, "first_name", firstName);
        putIfPresent(official, "last_name", lastName);
        putIfPresent(official, "email_address", ctx.contactEmail());
        if (ownershipPct != null) {
            official.put("company_ownership_percentage", String.valueOf(ownershipPct));
        }
        ObjectNode address = buildCompanyAddressNode(ctx);
        if (address.size() > 0) {
            official.set("address", address);
        }
        return official;
    }

    private ObjectNode buildCompanyAddressNode(FormationContext ctx) {
        ObjectNode address = objectMapper.createObjectNode();
        putIfPresent(address, "line1", ctx.mailingStreet());
        putIfPresent(address, "city", ctx.mailingCity());
        putIfPresent(address, "state_province_region", ctx.mailingStateCode());
        putIfPresent(address, "country", "US");
        putIfPresent(address, "zip_postal_code", ctx.mailingZip());
        return address;
    }

    private ObjectNode buildRegisteredAgentNode(FormationContext ctx) {
        if (ctx.agentName() == null || ctx.agentName().isBlank()) {
            return null;
        }
        ObjectNode agent = objectMapper.createObjectNode();
        agent.put("name", ctx.agentName().trim());
        ObjectNode address = objectMapper.createObjectNode();
        putIfPresent(address, "line1", ctx.agentStreet());
        putIfPresent(address, "city", ctx.agentCity());
        putIfPresent(address, "state_province_region", ctx.agentStateCode());
        putIfPresent(address, "country", "US");
        putIfPresent(address, "zip_postal_code", ctx.agentZip());
        if (address.size() > 0) {
            agent.set("address", address);
        }
        return agent;
    }

    private ObjectNode buildFilerNode(FormationContext ctx) {
        ObjectNode filer = objectMapper.createObjectNode();
        putIfPresent(filer, "first_name", ctx.ownerFirstName());
        putIfPresent(filer, "last_name", ctx.ownerLastName());
        putIfPresent(filer, "company_name", ctx.llcName());
        putIfPresent(filer, "email_address", ctx.contactEmail());
        ObjectNode address = buildCompanyAddressNode(ctx);
        if (address.size() > 0) {
            filer.set("address", address);
        }
        return filer.size() > 0 ? filer : null;
    }

    private ObjectNode buildMinimalFallback(FormationContext ctx) {
        ObjectNode formData = objectMapper.createObjectNode();
        putIfPresent(formData, "management_type", ctx.managementTypeLabel());
        var managers = buildOfficialArray(ctx, "Manager");
        if (managers != null) {
            formData.set("official.manager", managers);
        }
        var members = buildOfficialArray(ctx, "Member");
        if (members != null) {
            formData.set("official.member", members);
        }
        ObjectNode mailing = buildCompanyAddressNode(ctx);
        if (mailing.size() > 0) {
            formData.set("company_mailing_address", mailing);
            formData.set("company_principal_address", mailing.deepCopy());
        }
        ObjectNode agent = buildRegisteredAgentNode(ctx);
        if (agent != null) {
            formData.set("registered_agent", agent);
        }
        ObjectNode filer = buildFilerNode(ctx);
        if (filer != null) {
            formData.set("filer", filer);
        }
        return formData;
    }

    private void putIfPresent(ObjectNode node, String key, String value) {
        if (value != null && !value.isBlank()) {
            node.put(key, value.trim());
        }
    }

    private record FormationContext(
            String llcName,
            String managementType,
            String ownerFirstName,
            String ownerLastName,
            Integer ownershipPct,
            String principalStreet,
            String principalCity,
            String principalState,
            String principalZip,
            String jurisdictionCode,
            String agentName,
            String agentStreet,
            String agentCity,
            String agentState,
            String agentZip,
            String contactEmail,
            List<LlcFormationMember> members) {

        static FormationContext from(
                LlcFormation f,
                LlcFormationRegisteredAgent agent,
                List<LlcFormationMember> members,
                String jurisdictionFullName,
                String contactEmail) {

            String principalStreet = firstNonBlank(f.getBusinessStreet(), f.getOperatingBusinessStreet());
            String principalCity = firstNonBlank(f.getBusinessCity(), f.getOperatingBusinessCity());
            String principalState = firstNonBlank(f.getBusinessState(), f.getOperatingBusinessState(), f.getJurisdiction());
            String principalZip = firstNonBlank(f.getBusinessZip(), f.getOperatingBusinessZip());

            String agentName = null;
            String agentStreet = null;
            String agentCity = null;
            String agentState = null;
            String agentZip = null;
            if (agent != null) {
                if ("NUMBRICS_NW".equalsIgnoreCase(agent.getAgentType())) {
                    agentName = agent.getAgentNameSnapshot();
                    if (agent.getAgentAddressSnapshot() != null && !agent.getAgentAddressSnapshot().isBlank()) {
                        agentStreet = agent.getAgentAddressSnapshot();
                    }
                    agentCity = principalCity;
                    agentState = principalState;
                    agentZip = principalZip;
                } else {
                    agentName = agent.getAgentName();
                    agentStreet = agent.getStreet();
                    agentCity = agent.getCity();
                    agentState = agent.getState();
                    agentZip = agent.getZip();
                }
            }

            return new FormationContext(
                    f.getLlcName(),
                    f.getManagementType(),
                    f.getOwnerFirstName(),
                    f.getOwnerLastName(),
                    f.getOwnershipPct(),
                    principalStreet,
                    principalCity,
                    principalState,
                    principalZip,
                    f.getJurisdiction(),
                    agentName,
                    agentStreet,
                    agentCity,
                    agentState,
                    agentZip,
                    contactEmail,
                    members != null ? members : List.of());
        }

        String managementTypeLabel() {
            if (managementType == null || managementType.isBlank()) {
                return "member-managed";
            }
            String m = managementType.trim().toLowerCase(Locale.ROOT);
            if ("manager".equals(m) || m.contains("manager-managed")) {
                return "manager-managed";
            }
            return "member-managed";
        }

        String mailingStreet() {
            return principalStreet;
        }

        String mailingCity() {
            return principalCity;
        }

        String mailingZip() {
            return principalZip;
        }

        String mailingStateCode() {
            return toStateCode(principalState, jurisdictionCode);
        }

        String agentStateCode() {
            return toStateCode(agentState, jurisdictionCode);
        }
    }

    private static String toStateCode(String state, String jurisdictionCode) {
        if (state != null && state.trim().length() == 2) {
            return state.trim().toUpperCase(Locale.ROOT);
        }
        if (jurisdictionCode != null && !jurisdictionCode.isBlank()) {
            return jurisdictionCode.trim().toUpperCase(Locale.ROOT);
        }
        if (state != null && !state.isBlank()) {
            return state.trim();
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }
}
