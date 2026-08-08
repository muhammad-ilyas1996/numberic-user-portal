package com.numbericsuserportal.taxintake.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxSoftwareDto {
    private String code;
    private String displayName;
    private String vendorName;
    private boolean supportsCsv;
    private boolean supportsPdf;
    private String integrationMode;
    private String apiStatus;
    private String notes;
    private List<String> templateHeaders;
}
