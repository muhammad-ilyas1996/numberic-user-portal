package com.numbericsuserportal.LlcNorthwest.paymentmethod.impl;

import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.CreatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodActionResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.UpdatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.service.PaymentMethodService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service implementation for Payment Method operations
 */
@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    public PaymentMethodsResponseDTO getPaymentMethods() {
        return corporateToolsApiService.getPaymentMethods();
    }

    @Override
    public PaymentMethodActionResponseDTO createPaymentMethod(CreatePaymentMethodRequestDTO request) {
        return corporateToolsApiService.createPaymentMethod(request);
    }

    @Override
    public PaymentMethodActionResponseDTO updatePaymentMethod(UUID id, UpdatePaymentMethodRequestDTO request) {
        return corporateToolsApiService.updatePaymentMethod(id, request);
    }

    @Override
    public PaymentMethodActionResponseDTO deletePaymentMethod(UUID id) {
        return corporateToolsApiService.deletePaymentMethod(id);
    }
}

