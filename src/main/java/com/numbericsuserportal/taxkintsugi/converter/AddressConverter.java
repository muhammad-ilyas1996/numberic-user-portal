package com.numbericsuserportal.taxkintsugi.converter;

import com.numbericsuserportal.taxkintsugi.dto.AddressDTO;
import com.numbericsuserportal.taxkintsugi.entity.AddressEntity;

public class AddressConverter {
    public static AddressEntity toEntity(AddressDTO dto) {
        if (dto == null) return null;
        AddressEntity e = new AddressEntity();
        e.setType(dto.getType());
        e.setStreet1(dto.getStreet1());
        e.setStreet2(dto.getStreet2());
        e.setCity(dto.getCity());
        e.setCounty(dto.getCounty());
        e.setState(dto.getState());
        e.setPostalCode(dto.getPostalCode());
        e.setCountry(dto.getCountry());
        e.setStatus(dto.getStatus());
        return e;
    }

    public static AddressDTO toDto(AddressEntity e) {
        if (e == null) return null;
        AddressDTO dto = new AddressDTO();
        dto.setType(e.getType());
        dto.setStreet1(e.getStreet1());
        dto.setStreet2(e.getStreet2());
        dto.setCity(e.getCity());
        dto.setCounty(e.getCounty());
        dto.setState(e.getState());
        dto.setPostalCode(e.getPostalCode());
        dto.setCountry(e.getCountry());
        dto.setStatus(e.getStatus());
        return dto;
    }
}
