package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.invoice.dto.MerchantNmiConfigDto;
import com.numbericsuserportal.invoice.entity.MerchantNmiConfig;
import com.numbericsuserportal.invoice.repo.MerchantNmiConfigRepo;
import com.numbericsuserportal.invoice.service.MerchantNmiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MerchantNmiConfigServiceImpl implements MerchantNmiConfigService {

    @Autowired
    private MerchantNmiConfigRepo merchantNmiConfigRepo;

    private static final String MASK = "****";
    private static final int MASK_TAIL = 4;

    @Override
    public MerchantNmiConfigDto getByUserId(Long userId) {
        Optional<MerchantNmiConfig> opt = merchantNmiConfigRepo.findByUserId(userId);
        if (opt.isEmpty()) {
            MerchantNmiConfigDto dto = new MerchantNmiConfigDto();
            dto.setUserId(userId);
            dto.setConfigured(false);
            return dto;
        }
        return toDto(opt.get(), true);
    }

    @Override
    @Transactional
    public MerchantNmiConfigDto save(Long userId, MerchantNmiConfigDto dto) {
        MerchantNmiConfig entity = merchantNmiConfigRepo.findByUserId(userId)
            .orElse(new MerchantNmiConfig());
        entity.setUserId(userId);
        entity.setAuthMethod(dto.getAuthMethod() != null ? dto.getAuthMethod().trim() : "api_key");
        if (dto.getSecurityKey() != null && !dto.getSecurityKey().trim().isEmpty() && !dto.getSecurityKey().startsWith(MASK)) {
            entity.setSecurityKey(dto.getSecurityKey().trim());
        }
        if (dto.getNmiUsername() != null) {
            entity.setNmiUsername(dto.getNmiUsername().trim().isEmpty() ? null : dto.getNmiUsername().trim());
        }
        if (dto.getNmiPassword() != null && !dto.getNmiPassword().trim().isEmpty() && !dto.getNmiPassword().startsWith(MASK)) {
            entity.setNmiPassword(dto.getNmiPassword().trim());
        }
        if (dto.getTransactionUrl() != null) {
            entity.setTransactionUrl(dto.getTransactionUrl().trim().isEmpty() ? null : dto.getTransactionUrl().trim());
        }
        if (dto.getNmiMerchantId() != null) {
            entity.setNmiMerchantId(dto.getNmiMerchantId().trim().isEmpty() ? null : dto.getNmiMerchantId().trim());
        }
        if (dto.getBoardingStatus() != null) {
            entity.setBoardingStatus(dto.getBoardingStatus().trim().isEmpty() ? null : dto.getBoardingStatus().trim());
        }
        if (dto.getBoardingApplicationId() != null) {
            entity.setBoardingApplicationId(dto.getBoardingApplicationId().trim().isEmpty() ? null : dto.getBoardingApplicationId().trim());
        }
        MerchantNmiConfig saved = merchantNmiConfigRepo.save(entity);
        return toDto(saved, true);
    }

    @Override
    public Optional<MerchantNmiConfig> getEntityByUserId(Long userId) {
        return merchantNmiConfigRepo.findByUserId(userId);
    }

    private MerchantNmiConfigDto toDto(MerchantNmiConfig e, boolean maskSecrets) {
        MerchantNmiConfigDto dto = new MerchantNmiConfigDto();
        dto.setUserId(e.getUserId());
        dto.setAuthMethod(e.getAuthMethod());
        dto.setNmiUsername(e.getNmiUsername());
        dto.setTransactionUrl(e.getTransactionUrl());
        dto.setNmiMerchantId(e.getNmiMerchantId());
        dto.setBoardingStatus(e.getBoardingStatus());
        dto.setBoardingApplicationId(e.getBoardingApplicationId());
        if (maskSecrets) {
            dto.setSecurityKey(mask(e.getSecurityKey()));
            dto.setNmiPassword(e.getNmiPassword() != null ? MASK : null);
        } else {
            dto.setSecurityKey(e.getSecurityKey());
            dto.setNmiPassword(e.getNmiPassword());
        }
        dto.setConfigured(isConfigured(e));
        return dto;
    }

    private static boolean isConfigured(MerchantNmiConfig e) {
        if (e.getSecurityKey() != null && !e.getSecurityKey().isEmpty()) return true;
        if (e.getNmiUsername() != null && !e.getNmiUsername().isEmpty()
            && e.getNmiPassword() != null && !e.getNmiPassword().isEmpty()) return true;
        return false;
    }

    private static String mask(String value) {
        if (value == null || value.length() <= MASK_TAIL) return value != null ? MASK : null;
        return MASK + value.substring(value.length() - MASK_TAIL);
    }
}
