package com.numbericsuserportal.invoiceproduct.converter;

import com.numbericsuserportal.invoiceproduct.dto.InvoiceProductDTO;
import com.numbericsuserportal.invoiceproduct.entity.InvoiceProductEntity;

public class InvoiceProductConverter {

    public static InvoiceProductDTO toDTO(InvoiceProductEntity entity) {
        if (entity == null) return null;

        InvoiceProductDTO dto = new InvoiceProductDTO();
        dto.setId(entity.getId());
        dto.setProductName(entity.getProductName());
        dto.setQuantity(entity.getQuantity());
        dto.setAmount(entity.getAmount());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    public static InvoiceProductEntity toEntity(InvoiceProductDTO dto) {
        if (dto == null) return null;

        InvoiceProductEntity entity = new InvoiceProductEntity();
        entity.setId(dto.getId());
        entity.setProductName(dto.getProductName());
        entity.setQuantity(dto.getQuantity());
        entity.setAmount(dto.getAmount());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
