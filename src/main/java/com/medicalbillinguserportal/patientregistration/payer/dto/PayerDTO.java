package com.medicalbillinguserportal.patientregistration.payer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayerDTO {
    private String payer;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String countryCode;
    private String defaultLab;
    private String phone;
    private String fax;
    private String payerType;
    private String payerClass;
    private String claimFormat;
    private String Status;
}
