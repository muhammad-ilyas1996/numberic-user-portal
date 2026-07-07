package com.numbericsuserportal.kintsugi.service;

import com.numbericsuserportal.kintsugi.catalog.UsStateCatalog;
import com.numbericsuserportal.kintsugi.client.KintsugiApiClient;
import com.numbericsuserportal.kintsugi.dto.FilingFlowStateSummaryDTO;
import com.numbericsuserportal.kintsugi.dto.PhysicalNexusRequestDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KintsugiNexusService {

    private final KintsugiApiClient apiClient;

    public KintsugiNexusService(KintsugiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<FilingFlowStateSummaryDTO> listStatesWithNexus() {
        Map<String, Map<String, Object>> nexusByState = fetchNexusByState();
        return UsStateCatalog.allStates().stream()
                .map(s -> toSummary(s.get("stateCode"), s.get("stateName"), nexusByState.get(s.get("stateCode"))))
                .toList();
    }

    public Map<String, Object> getStateNexusDetail(String stateCode) {
        String code = normalizeStateCode(stateCode);
        Map<String, String> params = Map.of(
                "state_code", code,
                "without_pagination", "true",
                "country_code__in", "US"
        );
        Object response = apiClient.get(
                "/v1/nexus",
                Object.class,
                params);
        List<Map<String, Object>> items = extractItems(response);
        if (items.isEmpty()) {
            return Map.of(
                    "state_code", code,
                    "state_name", UsStateCatalog.resolveName(code).orElse(code),
                    "has_kintsugi_data", false
            );
        }
        Map<String, Object> detail = new HashMap<>(items.get(0));
        detail.put("has_kintsugi_data", true);
        return detail;
    }

    public Map<String, Object> createPhysicalNexus(PhysicalNexusRequestDTO request) {
        if (request.getStateCode() == null || request.getStateCode().isBlank()) {
            throw new IllegalArgumentException("stateCode is required");
        }
        if (request.getStartDate() == null || request.getStartDate().isBlank()) {
            throw new IllegalArgumentException("startDate is required (YYYY-MM-DD)");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new IllegalArgumentException("category is required (e.g. PHYSICAL_BUSINESS_LOCATION)");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("country_code", request.getCountryCode() != null ? request.getCountryCode() : "US");
        body.put("state_code", normalizeStateCode(request.getStateCode()));
        body.put("start_date", request.getStartDate());
        if (request.getEndDate() != null && !request.getEndDate().isBlank()) {
            body.put("end_date", request.getEndDate());
        }
        body.put("category", request.getCategory());
        body.put("source", "USER");
        if (request.getExternalId() != null) {
            body.put("external_id", request.getExternalId());
        }
        if (request.getStreet1() != null) {
            body.put("street_1", request.getStreet1());
        }
        if (request.getStreet2() != null) {
            body.put("street_2", request.getStreet2());
        }
        if (request.getCity() != null) {
            body.put("city", request.getCity());
        }
        if (request.getPostalCode() != null) {
            body.put("postal_code", request.getPostalCode());
        }

        return apiClient.post(
                "/v1/nexus/physical_nexus",
                body,
                new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    private Map<String, Map<String, Object>> fetchNexusByState() {
        Object response = apiClient.get(
                "/v1/nexus",
                Object.class,
                Map.of("without_pagination", "true", "country_code__in", "US"));
        return extractItems(response).stream()
                .filter(item -> item.get("state_code") != null)
                .collect(Collectors.toMap(
                        item -> String.valueOf(item.get("state_code")).toUpperCase(),
                        item -> item,
                        (a, b) -> a
                ));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(Object response) {
        if (response instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(i -> (Map<String, Object>) i)
                    .toList();
        } else if (response instanceof Map<?, ?> map) {
            Object items = map.get("items");
            if (items instanceof List<?> list) {
                return list.stream()
                        .filter(Map.class::isInstance)
                        .map(i -> (Map<String, Object>) i)
                        .toList();
            }
        }
        return List.of();
    }

    private FilingFlowStateSummaryDTO toSummary(String code, String name, Map<String, Object> nexus) {
        if (nexus == null) {
            return new FilingFlowStateSummaryDTO(
                    code, name, "NOT_TRACKED", false, false, false,
                    "0.00", "0.00", null, false);
        }
        return new FilingFlowStateSummaryDTO(
                code,
                name != null ? name : stringVal(nexus.get("state_name")),
                stringVal(nexus.get("status")),
                boolVal(nexus.get("nexus_met")),
                boolVal(nexus.get("economic_nexus_met")),
                boolVal(nexus.get("physical_nexus_met")),
                stringVal(nexus.get("transactions_amount")),
                stringVal(nexus.get("tax_liability")),
                intVal(nexus.get("threshold_sales")),
                true
        );
    }

    private String normalizeStateCode(String stateCode) {
        String code = stateCode.trim().toUpperCase();
        if (!UsStateCatalog.isValid(code)) {
            throw new IllegalArgumentException("Invalid US state code: " + stateCode);
        }
        return code;
    }

    private String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Boolean boolVal(Object o) {
        if (o instanceof Boolean b) {
            return b;
        }
        return o != null && Boolean.parseBoolean(String.valueOf(o));
    }

    private Integer intVal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
