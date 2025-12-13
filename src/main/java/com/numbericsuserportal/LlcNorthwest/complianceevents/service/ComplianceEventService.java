package com.numbericsuserportal.LlcNorthwest.complianceevents.service;

import com.numbericsuserportal.LlcNorthwest.complianceevents.dto.ComplianceEventsResponseDTO;

import java.util.UUID;

public interface ComplianceEventService {
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
}

