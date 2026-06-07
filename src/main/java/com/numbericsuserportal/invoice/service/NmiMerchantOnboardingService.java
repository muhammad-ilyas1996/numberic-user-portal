package com.numbericsuserportal.invoice.service;

import com.numbericsuserportal.invoice.dto.NmiFeeScheduleListDto;
import com.numbericsuserportal.invoice.dto.NmiMerchantOnboardingRequestDto;
import com.numbericsuserportal.invoice.dto.NmiMerchantOnboardingStatusDto;

import java.util.Map;

public interface NmiMerchantOnboardingService {

    NmiMerchantOnboardingStatusDto submit(Long userId, NmiMerchantOnboardingRequestDto request);

    NmiMerchantOnboardingStatusDto getLatestStatus(Long userId);

    NmiMerchantOnboardingStatusDto handleWebhook(Map<String, Object> payload);

    NmiFeeScheduleListDto listFeeSchedules();
}
