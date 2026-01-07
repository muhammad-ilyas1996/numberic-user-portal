package com.numbericsuserportal.LlcNorthwest.paymentmethod.service;

import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.CreatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodActionResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.UpdatePaymentMethodRequestDTO;

import java.util.UUID;

/**
 * Service interface for Payment Method operations
 */
public interface PaymentMethodService {
    
    /**
     * Get all saved card payment methods for the authorized account
     * 
     * @return PaymentMethodsResponseDTO containing list of payment methods
     */
    PaymentMethodsResponseDTO getPaymentMethods();
    
    /**
     * Create a new saved card payment method
     * Only valid, active cards are accepted. Test cards are not supported.
     * 
     * @param request CreatePaymentMethodRequestDTO with card details
     * @return PaymentMethodActionResponseDTO with success status
     */
    PaymentMethodActionResponseDTO createPaymentMethod(CreatePaymentMethodRequestDTO request);
    
    /**
     * Update an existing saved card payment method
     * 
     * @param id UUID of the payment method to update
     * @param request UpdatePaymentMethodRequestDTO with updated card details
     * @return PaymentMethodActionResponseDTO with success status
     */
    PaymentMethodActionResponseDTO updatePaymentMethod(UUID id, UpdatePaymentMethodRequestDTO request);
    
    /**
     * Delete a saved card payment method
     * 
     * @param id UUID of the payment method to delete
     * @return PaymentMethodActionResponseDTO with success status
     */
    PaymentMethodActionResponseDTO deletePaymentMethod(UUID id);
}

