package com.numbericsuserportal.taxkintsugi.converter;

import com.numbericsuserportal.taxkintsugi.dto.TaxItemDTO;
import com.numbericsuserportal.taxkintsugi.entity.TaxItemEntity;

public class TaxItemConverter {
    public static TaxItemEntity toEntity(TaxItemDTO dto) {
        if (dto == null) return null;
        TaxItemEntity e = new TaxItemEntity();
        e.setName(dto.getName());
        e.setRate(dto.getRate());
        e.setAmount(dto.getAmount());
        e.setExempt(dto.getExempt());
        return e;
    }

    public static TaxItemDTO toDto(TaxItemEntity e) {
        if (e == null) return null;
        TaxItemDTO dto = new TaxItemDTO();
        dto.setName(e.getName());
        dto.setRate(e.getRate());
        dto.setAmount(e.getAmount());
        dto.setExempt(e.getExempt());
        return dto;
    }
}
