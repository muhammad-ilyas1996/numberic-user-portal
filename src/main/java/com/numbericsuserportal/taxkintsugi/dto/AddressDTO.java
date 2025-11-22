package com.numbericsuserportal.taxkintsugi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {
    private String type;
    private String street1;
    private String street2;
    private String city;
    private String county;
    private String state;
    private String postalCode;
    private String country;
    private String status;
}