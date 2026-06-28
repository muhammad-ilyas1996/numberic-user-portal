package com.numbericsuserportal.kintsugi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.kintsugi.catalog.BusinessTypeCatalog;
import com.numbericsuserportal.kintsugi.catalog.ExemptionLookup;
import com.numbericsuserportal.kintsugi.catalog.UsStateCatalog;
import com.numbericsuserportal.kintsugi.domain.SalesTaxBusinessType;
import com.numbericsuserportal.kintsugi.dto.UpdateExemptionProfileRequestDTO;
import com.numbericsuserportal.kintsugi.entity.UserExemptionProfileEntity;
import com.numbericsuserportal.kintsugi.repo.UserExemptionProfileRepository;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExemptionService {

    private final UserExemptionProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ExemptionService(
            UserExemptionProfileRepository profileRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getBusinessTypeSetup(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("setupRequired", user.getBusinessType() == null);
        result.put("businessType", user.getBusinessType() != null ? user.getBusinessType().name() : null);
        result.put("businessTypeLabel", user.getBusinessType() != null
                ? BusinessTypeCatalog.displayName(user.getBusinessType()) : null);
        result.put("description", user.getBusinessTypeDescription());
        result.put("businessTypes", BusinessTypeCatalog.allTypes());
        return result;
    }

    @Transactional
    public Map<String, Object> saveBusinessType(User user, SalesTaxBusinessType businessType, String description) {
        User entity = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        entity.setBusinessType(businessType);
        if (businessType == SalesTaxBusinessType.other && description != null && !description.isBlank()) {
            entity.setBusinessTypeDescription(description.trim());
        }
        userRepository.save(entity);
        user.setBusinessType(entity.getBusinessType());
        user.setBusinessTypeDescription(entity.getBusinessTypeDescription());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("businessType", businessType.name());
        result.put("businessTypeLabel", BusinessTypeCatalog.displayName(businessType));
        result.put("setupRequired", false);
        return result;
    }

    public Map<String, Object> getExemptionProfile(User user, String stateCode) {
        if (user.getBusinessType() == null) {
            throw new IllegalStateException("Business type must be set before loading exemption profile");
        }
        String code = normalizeState(stateCode);
        UserExemptionProfileEntity saved = profileRepository
                .findByUserIdAndStateCodeAndBusinessType(user.getUserId(), code, user.getBusinessType())
                .orElse(null);

        ExemptionLookup.ExemptionProfile profile = saved != null
                ? new ExemptionLookup.ExemptionProfile(
                        readJsonList(saved.getTaxableCategoriesJson()),
                        readJsonList(saved.getExemptCategoriesJson()))
                : ExemptionLookup.resolve(user.getBusinessType(), code);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stateCode", code);
        result.put("stateName", UsStateCatalog.resolveName(code).orElse(code));
        result.put("businessType", user.getBusinessType().name());
        result.put("businessTypeLabel", BusinessTypeCatalog.displayName(user.getBusinessType()));
        result.put("taxableCategories", profile.taxableCategories());
        result.put("exemptCategories", profile.exemptCategories());
        result.put("taalrSuggested", saved == null || Boolean.TRUE.equals(saved.getTaalrSuggested()));
        result.put("userModified", saved != null && Boolean.TRUE.equals(saved.getUserModified()));
        return result;
    }

    @Transactional
    public Map<String, Object> updateExemptionProfile(User user, UpdateExemptionProfileRequestDTO request) {
        if (user.getBusinessType() == null) {
            throw new IllegalStateException("Business type must be set first");
        }
        String code = normalizeState(request.getStateCode());
        if (request.getTaxableCategories() == null || request.getExemptCategories() == null) {
            throw new IllegalArgumentException("taxableCategories and exemptCategories are required");
        }

        UserExemptionProfileEntity entity = profileRepository
                .findByUserIdAndStateCodeAndBusinessType(user.getUserId(), code, user.getBusinessType())
                .orElseGet(UserExemptionProfileEntity::new);

        entity.setUserId(user.getUserId());
        entity.setStateCode(code);
        entity.setBusinessType(user.getBusinessType());
        entity.setTaxableCategoriesJson(writeJson(request.getTaxableCategories()));
        entity.setExemptCategoriesJson(writeJson(request.getExemptCategories()));
        entity.setTaalrSuggested(false);
        entity.setUserModified(true);
        profileRepository.save(entity);

        return getExemptionProfile(user, code);
    }

    @Transactional
    public Map<String, Object> confirmExemptionProfile(User user, String stateCode) {
        if (user.getBusinessType() == null) {
            throw new IllegalStateException("Business type must be set before confirming exemptions");
        }
        ensureProfileSaved(user, stateCode);
        Map<String, Object> profile = getExemptionProfile(user, stateCode);
        profile.put("saved", true);
        return profile;
    }

    @Transactional
    public UserExemptionProfileEntity ensureProfileSaved(User user, String stateCode) {
        String code = normalizeState(stateCode);
        return profileRepository
                .findByUserIdAndStateCodeAndBusinessType(user.getUserId(), code, user.getBusinessType())
                .orElseGet(() -> {
                    ExemptionLookup.ExemptionProfile defaults =
                            ExemptionLookup.resolve(user.getBusinessType(), code);
                    UserExemptionProfileEntity entity = new UserExemptionProfileEntity();
                    entity.setUserId(user.getUserId());
                    entity.setStateCode(code);
                    entity.setBusinessType(user.getBusinessType());
                    entity.setTaxableCategoriesJson(writeJson(defaults.taxableCategories()));
                    entity.setExemptCategoriesJson(writeJson(defaults.exemptCategories()));
                    entity.setTaalrSuggested(true);
                    entity.setUserModified(false);
                    return profileRepository.save(entity);
                });
    }

    public List<String> exemptCategoriesFor(User user, String stateCode) {
        return readJsonList(ensureProfileSaved(user, stateCode).getExemptCategoriesJson());
    }

    public long calculateSavingsCents(long exemptAmountCents, double blendedRate) {
        return Math.round(exemptAmountCents * blendedRate);
    }

    private String normalizeState(String stateCode) {
        if (stateCode == null || stateCode.isBlank()) {
            throw new IllegalArgumentException("stateCode is required");
        }
        String code = stateCode.trim().toUpperCase();
        if (!UsStateCatalog.isValid(code)) {
            throw new IllegalArgumentException("Invalid stateCode: " + stateCode);
        }
        return code;
    }

    private List<String> readJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values != null ? values : List.of());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize categories", e);
        }
    }
}
