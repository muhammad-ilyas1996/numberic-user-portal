package com.numbericsuserportal.LlcNorthwest.complianceevents.impl;

import com.numbericsuserportal.LlcNorthwest.complianceevents.dto.ComplianceEventsResponseDTO;
import com.numbericsuserportal.LlcNorthwest.complianceevents.service.ComplianceEventService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ComplianceEventServiceImpl implements ComplianceEventService {

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Override
    public ComplianceEventsResponseDTO getComplianceEvents(
            Integer limit,
            Integer offset,
            String company,
            UUID companyId,
            String startDate,
            String endDate,
            String[] jurisdictions,
            UUID[] jurisdictionIds) {
        
        // Validate: Either company OR company_id, but NOT both
        boolean hasCompany = company != null && !company.trim().isEmpty();
        boolean hasCompanyId = companyId != null;
        
        if (hasCompany && hasCompanyId) {
            throw new IllegalArgumentException(
                "Either 'company' or 'company_id' must be provided, but not both"
            );
        }
        
        // Validate: Either jurisdictions OR jurisdiction_ids, but NOT both
        boolean hasJurisdictions = jurisdictions != null && jurisdictions.length > 0;
        boolean hasJurisdictionIds = jurisdictionIds != null && jurisdictionIds.length > 0;
        
        if (hasJurisdictions && hasJurisdictionIds) {
            throw new IllegalArgumentException(
                "Either 'jurisdictions' or 'jurisdiction_ids' must be provided, but not both"
            );
        }
        
        // Validate required fields
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException("start_date is required");
        }
        if (endDate == null || endDate.trim().isEmpty()) {
            throw new IllegalArgumentException("end_date is required");
        }
        
        return corporateToolsApiService.getComplianceEvents(
            limit,
            offset,
            company,
            companyId,
            startDate,
            endDate,
            jurisdictions,
            jurisdictionIds
        );
    }
}

