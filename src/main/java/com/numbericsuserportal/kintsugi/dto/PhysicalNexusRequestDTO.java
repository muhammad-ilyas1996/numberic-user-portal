package com.numbericsuserportal.kintsugi.dto;

import lombok.Data;

@Data
public class PhysicalNexusRequestDTO {
    private String stateCode;
    private String countryCode;
    private String startDate;
    private String endDate;
    private String category;
    private String externalId;
    private String street1;
    private String street2;
    private String city;
    private String postalCode;
}
