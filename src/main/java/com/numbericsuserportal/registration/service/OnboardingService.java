package com.numbericsuserportal.registration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.registration.dto.OnboardingRequestDto;
import com.numbericsuserportal.registration.dto.OnboardingResponseDto;
import com.numbericsuserportal.registration.entity.BusinessProfile;
import com.numbericsuserportal.registration.repo.BusinessProfileRepository;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Taalr onboarding: load and save questionnaire to business_profiles (same row, update never delete).
 */
@Service
public class OnboardingService {

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * GET onboarding: return current user's business profile for pre-fill; completed = has key data.
     */
    public OnboardingResponseDto getOnboarding(User currentUser) {
        Long userId = currentUser.getUserId();
        Optional<BusinessProfile> opt = businessProfileRepository.findByUserId(userId);
        OnboardingResponseDto dto = new OnboardingResponseDto();
        dto.setUserId(userId);
        if (opt.isEmpty()) {
            dto.setCompleted(false);
            return dto;
        }
        BusinessProfile p = opt.get();
        mapToResponse(p, dto);
        dto.setCompleted(isOnboardingCompleted(p));
        return dto;
    }

    /**
     * POST onboarding: create profile if missing, then update same row with questionnaire answers.
     */
    @Transactional
    public OnboardingResponseDto saveOnboarding(User currentUser, OnboardingRequestDto request) {
        Long userId = currentUser.getUserId();
        BusinessProfile profile = businessProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    BusinessProfile newProfile = new BusinessProfile();
                    newProfile.setUserId(userId);
                    String audit = currentUser.getEmail() != null ? currentUser.getEmail() : String.valueOf(userId);
                    newProfile.setCreatedBy(audit);
                    newProfile.setModifiedBy(audit);
                    java.util.Date now = new java.util.Date();
                    newProfile.setCreatedOn(now);
                    newProfile.setModifiedOn(now);
                    return newProfile;
                });

        if (request.getBusinessName() != null) {
            profile.setCompanyName(request.getBusinessName());
            if (profile.getBusinessLegalName() == null) {
                profile.setBusinessLegalName(request.getBusinessName());
            }
        }
        profile.setRegistrationState(request.getRegistrationState());
        profile.setRevenueLastYear(request.getRevenueLastYear());
        profile.setRevenueExpected(request.getRevenueExpected());
        profile.setCollectsSalesTax(request.getCollectsSalesTax());
        profile.setMultiState(request.getMultiState());
        profile.setHasEmployees(request.getHasEmployees());
        profile.setTeamSize(request.getTeamSize());
        profile.setFilesQuarterlyTaxes(request.getFilesQuarterlyTaxes());
        profile.setSalesTaxFrequency(request.getSalesTaxFrequency());
        profile.setTaxFilingsUpToDate(request.getTaxFilingsUpToDate());
        profile.setAccountingSoftware(request.getAccountingSoftware());
        profile.setConnectBankAccounts(request.getConnectBankAccounts());
        profile.setGoals(request.getGoals());
        profile.setLocation(request.getLocation());
        if (request.getIndustry() != null) {
            profile.setIndustry(request.getIndustry());
        }
        if (request.getEntityType() != null) {
            profile.setEntityType(request.getEntityType());
            try {
                String normalized = request.getEntityType().toLowerCase().replace("-", "_").replace(" ", "_");
                BusinessProfile.BusinessType bt = mapEntityTypeToBusinessType(normalized);
                if (bt != null) {
                    profile.setBusinessType(bt);
                }
            } catch (Exception ignored) {
                // keep existing businessType if invalid
            }
        }

        if (request.getOnboardingTrack() != null) {
            profile.setOnboardingTrack(request.getOnboardingTrack());
        }
        if (request.getFilingStatus() != null) {
            profile.setFilingStatus(request.getFilingStatus());
        }
        if (request.getIncomeSourceCodes() != null) {
            profile.setIncomeSourceCodes(request.getIncomeSourceCodes());
        }
        if (request.getPainPointCode() != null) {
            profile.setPainPointCode(request.getPainPointCode());
        }
        if (request.getOperationsCodes() != null) {
            profile.setOperationsCodes(request.getOperationsCodes());
        }
        if (request.getTaxProRelationship() != null) {
            profile.setTaxProRelationship(request.getTaxProRelationship());
        }
        if (request.getBusinessTierChoice() != null) {
            profile.setBusinessTierChoice(request.getBusinessTierChoice());
        }
        if (request.getTaxProCredential() != null) {
            profile.setTaxProCredential(request.getTaxProCredential());
        }
        if (request.getPracticeClientBand() != null) {
            profile.setPracticeClientBand(request.getPracticeClientBand());
        }
        if (request.getPracticeTaxSoftware() != null) {
            profile.setPracticeTaxSoftware(request.getPracticeTaxSoftware());
        }
        if (request.getPracticePainCodes() != null) {
            profile.setPracticePainCodes(request.getPracticePainCodes());
        }
        if (request.getOnboardingAnswersJson() != null) {
            profile.setOnboardingAnswersJson(request.getOnboardingAnswersJson());
        }

        try {
            profile.setOnboardingPayloadJson(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize onboarding request to JSON", e);
        }

        BusinessProfile saved = businessProfileRepository.save(profile);
        OnboardingResponseDto dto = new OnboardingResponseDto();
        mapToResponse(saved, dto);
        dto.setCompleted(isOnboardingCompleted(saved));
        return dto;
    }

    private void mapToResponse(BusinessProfile p, OnboardingResponseDto dto) {
        dto.setBusinessProfileId(p.getBusinessProfileId());
        dto.setUserId(p.getUserId());
        dto.setCompanyName(p.getCompanyName());
        dto.setBusinessLegalName(p.getBusinessLegalName());
        dto.setBusinessName(p.getCompanyName() != null ? p.getCompanyName() : p.getBusinessLegalName());
        dto.setEntityType(p.getEntityType() != null ? p.getEntityType() : (p.getBusinessType() != null ? p.getBusinessType().name() : null));
        dto.setRegistrationState(p.getRegistrationState());
        dto.setIndustry(p.getIndustry());
        dto.setRevenueLastYear(p.getRevenueLastYear());
        dto.setRevenueExpected(p.getRevenueExpected());
        dto.setCollectsSalesTax(p.getCollectsSalesTax());
        dto.setMultiState(p.getMultiState());
        dto.setHasEmployees(p.getHasEmployees());
        dto.setTeamSize(p.getTeamSize());
        dto.setFilesQuarterlyTaxes(p.getFilesQuarterlyTaxes());
        dto.setSalesTaxFrequency(p.getSalesTaxFrequency());
        dto.setTaxFilingsUpToDate(p.getTaxFilingsUpToDate());
        dto.setAccountingSoftware(p.getAccountingSoftware());
        dto.setConnectBankAccounts(p.getConnectBankAccounts());
        dto.setGoals(p.getGoals());
        dto.setLocation(p.getLocation());
        dto.setOnboardingTrack(p.getOnboardingTrack());
        dto.setFilingStatus(p.getFilingStatus());
        dto.setIncomeSourceCodes(p.getIncomeSourceCodes());
        dto.setPainPointCode(p.getPainPointCode());
        dto.setOperationsCodes(p.getOperationsCodes());
        dto.setTaxProRelationship(p.getTaxProRelationship());
        dto.setBusinessTierChoice(p.getBusinessTierChoice());
        dto.setTaxProCredential(p.getTaxProCredential());
        dto.setPracticeClientBand(p.getPracticeClientBand());
        dto.setPracticeTaxSoftware(p.getPracticeTaxSoftware());
        dto.setPracticePainCodes(p.getPracticePainCodes());
        dto.setOnboardingAnswersJson(p.getOnboardingAnswersJson());
        dto.setOnboardingPayloadJson(p.getOnboardingPayloadJson());
    }

    private boolean isOnboardingCompleted(BusinessProfile p) {
        if (p.getOnboardingPayloadJson() != null && !p.getOnboardingPayloadJson().isBlank()) {
            return true;
        }
        if (p.getOnboardingAnswersJson() != null && !p.getOnboardingAnswersJson().isBlank()) {
            return true;
        }
        if (p.getOnboardingTrack() != null && !p.getOnboardingTrack().isBlank()) {
            return true;
        }
        return (p.getCompanyName() != null && !p.getCompanyName().isBlank())
                || (p.getBusinessLegalName() != null && !p.getBusinessLegalName().isBlank())
                || (p.getIndustry() != null && !p.getIndustry().isBlank());
    }

    private BusinessProfile.BusinessType mapEntityTypeToBusinessType(String normalized) {
        if (normalized == null) return null;
        switch (normalized) {
            case "s_corp":
            case "scorp":
            case "c_corp":
            case "ccorp":
                return BusinessProfile.BusinessType.corp;
            case "sole_prop":
            case "soleproprietor":
                return BusinessProfile.BusinessType.sole_proprietor;
            case "llc":
            case "corp":
            case "partnership":
            case "nonprofit":
            case "other":
                return BusinessProfile.BusinessType.valueOf(normalized);
            default:
                try {
                    return BusinessProfile.BusinessType.valueOf(normalized);
                } catch (Exception e) {
                    return null;
                }
        }
    }
}
