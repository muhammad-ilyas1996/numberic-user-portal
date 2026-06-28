package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRegisteredAgent;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO.SchemaFieldDTO;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NorthwestShoppingCartFormDataBuilderTest {

    @Mock
    private CorporateToolsApiService corporateToolsApiService;

    @Mock
    private UserRepository userRepository;

    private NorthwestShoppingCartFormDataBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new NorthwestShoppingCartFormDataBuilder(
                corporateToolsApiService, userRepository, new ObjectMapper());
    }

    @Test
    void build_usesSchemaOnlyKeysWithWyomingAddressShape() {
        LlcFormation formation = sampleFormation();
        LlcFormationRegisteredAgent agent = sampleAgent();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser()));
        when(corporateToolsApiService.getFilingMethodSchemas(any(), any())).thenReturn(wyomingSchema());

        ObjectNode formData = builder.build(formation, agent, List.of(), "Wyoming");

        assertTrue(formData.has("management_type"));
        assertTrue(formData.has("official.manager"));
        assertTrue(formData.has("official.member"));
        assertTrue(formData.has("company_mailing_address"));
        assertTrue(formData.has("company_principal_address"));
        assertTrue(formData.has("registered_agent"));
        assertTrue(formData.has("filer"));

        assertFalse(formData.has("company_name"));
        assertFalse(formData.has("principal_address"));
        assertFalse(formData.has("members"));

        var mailing = formData.get("company_mailing_address");
        assertEquals("100 Business Ave", mailing.get("line1").asText());
        assertEquals("Cheyenne", mailing.get("city").asText());
        assertEquals("WY", mailing.get("state_province_region").asText());
        assertEquals("US", mailing.get("country").asText());
        assertEquals("82001", mailing.get("zip_postal_code").asText());

        var manager = formData.get("official.manager").get(0);
        assertEquals("owner@example.com", manager.get("email_address").asText());
        assertEquals("Manager", manager.get("role").asText());
    }

    private static LlcFormation sampleFormation() {
        LlcFormation f = new LlcFormation();
        f.setId(54L);
        f.setUserId(1L);
        f.setCompanyId("f77dcd67-8db3-4092-aebb-5387c7c01d30");
        f.setFilingMethodId("34a9870f-f089-47c3-92c9-30afcc18ec25");
        f.setLlcName("Muti Ur Rehman");
        f.setManagementType("member");
        f.setOwnerFirstName("John");
        f.setOwnerLastName("Doe");
        f.setOwnershipPct(100);
        f.setJurisdiction("WY");
        f.setBusinessStreet("100 Business Ave");
        f.setBusinessCity("Cheyenne");
        f.setBusinessState("WY");
        f.setBusinessZip("82001");
        return f;
    }

    private static LlcFormationRegisteredAgent sampleAgent() {
        LlcFormationRegisteredAgent agent = new LlcFormationRegisteredAgent();
        agent.setAgentType("OWN");
        agent.setAgentName("Jane Agent");
        agent.setStreet("123 Main Street");
        agent.setCity("Cheyenne");
        agent.setState("WY");
        agent.setZip("82001");
        return agent;
    }

    private static User sampleUser() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("owner@example.com");
        return user;
    }

    private static FilingMethodSchemaResponseDTO wyomingSchema() {
        FilingMethodSchemaResponseDTO response = new FilingMethodSchemaResponseDTO();
        response.setSuccess(true);
        response.setResult(List.of(
                field("entity_type"),
                field("disclaimer"),
                field("management_type"),
                field("official.manager"),
                field("official.member"),
                field("company_mailing_address"),
                field("company_principal_address"),
                field("registered_agent"),
                field("filer")));
        return response;
    }

    private static SchemaFieldDTO field(String name) {
        SchemaFieldDTO dto = new SchemaFieldDTO();
        dto.setName(name);
        dto.setRequired(true);
        return dto;
    }
}
