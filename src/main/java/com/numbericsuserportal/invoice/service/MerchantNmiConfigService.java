package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.MerchantNmiConfigDto;
import com.numbericsuserportal.invoice.entity.MerchantNmiConfig;

import java.util.Optional;

public interface MerchantNmiConfigService {

    /** Get config for user (for Settings GET). Keys masked. */
    MerchantNmiConfigDto getByUserId(Long userId);

    /** Save/update config for user (from Settings page). */
    MerchantNmiConfigDto save(Long userId, MerchantNmiConfigDto dto);

    /** Get raw entity for payment processing (internal). */
    Optional<MerchantNmiConfig> getEntityByUserId(Long userId);
}
