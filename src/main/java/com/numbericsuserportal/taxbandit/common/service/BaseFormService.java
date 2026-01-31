package com.numbericsuserportal.taxbandit.common.service;

import com.numbericsuserportal.taxbandit.service.TaxBanditsApiService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base abstract service for all TaxBandits form services.
 * Provides common functionality and enforces consistent pattern across all forms.
 * 
 * @param <TRequest> The request DTO type
 * @param <TResponse> The response DTO type
 */
public abstract class BaseFormService<TRequest, TResponse> {
    
    @Autowired
    protected TaxBanditsApiService taxBanditsApiService;
    
    /**
     * Main entry point for processing a form request.
     * Follows the pattern: validate -> set defaults -> call API
     * 
     * @param request The form request DTO
     * @return The form response DTO
     */
    public TResponse processForm(TRequest request) {
        validateRequest(request);
        setDefaultValues(request);
        return callTaxBanditsAPI(request);
    }
    
    /**
     * Validate the request before processing.
     * Override this method to add form-specific validations.
     * 
     * @param request The form request DTO
     */
    protected void validateRequest(TRequest request) {
        // Default implementation - override in subclasses if needed
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
    }
    
    /**
     * Set default values for required fields that cannot be null.
     * Override this method to set form-specific defaults.
     * 
     * @param request The form request DTO
     */
    protected abstract void setDefaultValues(TRequest request);
    
    /**
     * Call the TaxBandits API for this specific form type.
     * Override this method to call the appropriate API method.
     * 
     * @param request The form request DTO
     * @return The form response DTO
     */
    protected abstract TResponse callTaxBanditsAPI(TRequest request);
}



