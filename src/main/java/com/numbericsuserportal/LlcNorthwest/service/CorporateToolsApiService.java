package com.numbericsuserportal.LlcNorthwest.service;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompaniesResponseDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.CreateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.companies.dto.UpdateCompanyRequestDTO;
import com.numbericsuserportal.LlcNorthwest.complianceevents.dto.ComplianceEventsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.document.dto.*;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodSchemaResponseDTO;
import com.numbericsuserportal.LlcNorthwest.filingmethod.dto.FilingMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.CreatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodActionResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.PaymentMethodsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.paymentmethod.dto.UpdatePaymentMethodRequestDTO;
import com.numbericsuserportal.LlcNorthwest.registeredagent.dto.RegisteredAgentProductsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.signedforms.dto.SignedFormsResponseDTO;

import java.util.UUID;

public interface CorporateToolsApiService {
    FilingProductsResponseDTO getFilingProducts(String websiteUrl, String jurisdiction, String entityType);
    FilingProductsResponseDTO getFilingProductsOfferings(String companyId, String jurisdiction);
    
    // Companies API methods
    CompaniesResponseDTO getCompanies(Integer limit, Integer offset, String[] names);
    CompaniesResponseDTO getCompanyById(UUID companyId);
    CompaniesResponseDTO createCompanies(CreateCompanyRequestDTO request);
    CompaniesResponseDTO updateCompanies(UpdateCompanyRequestDTO request);
    
    // Filing Methods API methods
    FilingMethodsResponseDTO getFilingMethods(UUID companyId, UUID filingProductId, String jurisdiction);
    FilingMethodSchemaResponseDTO getFilingMethodSchemas(UUID companyId, UUID filingMethodId);
    
    // Compliance Events API methods
    ComplianceEventsResponseDTO getComplianceEvents(
        Integer limit,
        Integer offset,
        String company,
        UUID companyId,
        String startDate,
        String endDate,
        String[] jurisdictions,
        UUID[] jurisdictionIds
    );
    
    // Registered Agent Products API methods
    RegisteredAgentProductsResponseDTO getRegisteredAgentProducts(String url);
    
    // Signed Forms API methods
    SignedFormsResponseDTO getSignedForms(UUID filingMethodId, UUID websiteId);
    
    // Payment Methods API methods
    PaymentMethodsResponseDTO getPaymentMethods();
    PaymentMethodActionResponseDTO createPaymentMethod(CreatePaymentMethodRequestDTO request);
    PaymentMethodActionResponseDTO updatePaymentMethod(UUID id, UpdatePaymentMethodRequestDTO request);
    PaymentMethodActionResponseDTO deletePaymentMethod(UUID id);
    
    // Documents API methods
    DocumentsResponseDTO getDocuments(
        Integer limit,
        Integer offset,
        String status,
        String start,
        String stop,
        String jurisdiction,
        UUID companyId
    );
    DocumentResponseDTO getDocumentById(UUID id);
    byte[] getDocumentPage(UUID id, Integer pageNumber, Integer dpi);
    byte[] downloadDocument(UUID id);
    PageUrlResponseDTO getDocumentPageUrl(UUID id, Integer pageNumber, Integer dpi);
    BulkDownloadResponseDTO bulkDownloadDocuments(UUID[] ids);
    UnlockDocumentResponseDTO unlockDocument(UUID id, UnlockDocumentRequestDTO request);
}

