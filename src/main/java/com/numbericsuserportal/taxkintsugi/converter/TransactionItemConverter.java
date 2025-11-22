package com.numbericsuserportal.taxkintsugi.converter;

import com.numbericsuserportal.taxkintsugi.dto.TransactionItemDTO;
import com.numbericsuserportal.taxkintsugi.entity.TaxItemEntity;
import com.numbericsuserportal.taxkintsugi.entity.TransactionItemEntity;

import java.util.stream.Collectors;

public class TransactionItemConverter {
    public static TransactionItemEntity toEntity(TransactionItemDTO dto) {
        if (dto == null) return null;
        TransactionItemEntity e = new TransactionItemEntity();
        e.setExternalId(dto.getExternalId());
        e.setDate(dto.getDate());
        e.setDescription(dto.getDescription());
        e.setExternalProductId(dto.getExternalProductId());
        e.setProductName(dto.getProductName());
        e.setProductDescription(dto.getProductDescription());
        e.setProductSource(dto.getProductSource());
        e.setProductSubcategory(dto.getProductSubcategory());
        e.setProductCategory(dto.getProductCategory());
        e.setQuantity(dto.getQuantity());
        e.setAmount(dto.getAmount());
        e.setExempt(dto.getExempt());
        e.setTaxAmount(dto.getTaxAmount());
        e.setTaxableAmount(dto.getTaxableAmount());
        e.setTaxRate(dto.getTaxRate());
        if (dto.getTaxItems() != null) {
            e.setTaxItems(dto.getTaxItems().stream()
                    .map(tdto -> {
                        TaxItemEntity te = TaxItemConverter.toEntity(tdto);
                        te.setTransactionItem(e);
                        return te;
                    }).collect(Collectors.toList()));
        }
        return e;
    }

    public static TransactionItemDTO toDto(TransactionItemEntity e) {
        if (e == null) return null;
        TransactionItemDTO dto = new TransactionItemDTO();
        dto.setExternalId(e.getExternalId());
        dto.setDate(e.getDate());
        dto.setDescription(e.getDescription());
        dto.setExternalProductId(e.getExternalProductId());
        dto.setProductName(e.getProductName());
        dto.setProductDescription(e.getProductDescription());
        dto.setProductSource(e.getProductSource());
        dto.setProductSubcategory(e.getProductSubcategory());
        dto.setProductCategory(e.getProductCategory());
        dto.setQuantity(e.getQuantity());
        dto.setAmount(e.getAmount());
        dto.setExempt(e.getExempt());
        dto.setTaxAmount(e.getTaxAmount());
        dto.setTaxableAmount(e.getTaxableAmount());
        dto.setTaxRate(e.getTaxRate());
        if (e.getTaxItems() != null) {
            dto.setTaxItems(e.getTaxItems().stream()
                    .map(TaxItemConverter::toDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
