package com.numbericsuserportal.federaltax.dto;

import com.numbericsuserportal.federaltax.domain.ClientTaxStatus;
import com.numbericsuserportal.federaltax.domain.FederalTaxReturnType;
import com.numbericsuserportal.federaltax.domain.TaxServiceOption;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class FederalTaxCaseDto {
    private String publicCaseId;
    private FederalTaxReturnType returnType;
    private TaxServiceOption serviceOption;
    private ClientTaxStatus clientStatus;
    private Integer taxYear;
    private Map<String, Object> organizer;
    private Map<String, Object> extracted;
    private List<String> missingItems;
    private Map<String, Object> validation;
    private boolean clarificationOpen;
    private String clarificationNote;
    private boolean form8879Signed;
    private boolean spouse8879Required;
    private boolean spouse8879Signed;
    private boolean reviewerApproved;
    private boolean taxpayerConsent;
    private String exportPackagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FederalTaxDocumentDto> documents;
}
