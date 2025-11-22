package com.numbericsuserportal.taxkintsugi.converter;

import com.numbericsuserportal.taxkintsugi.dto.TransactionDTO;
import com.numbericsuserportal.taxkintsugi.entity.AddressEntity;
import com.numbericsuserportal.taxkintsugi.entity.TransactionEntity;
import com.numbericsuserportal.taxkintsugi.entity.TransactionItemEntity;

import java.util.stream.Collectors;

public class TransactionConverter {
    public static TransactionEntity toEntity(TransactionDTO dto) {
        if (dto == null) return null;
        TransactionEntity e = new TransactionEntity();
        e.setDate(dto.getDate());
        e.setExternalId(dto.getExternalId());
        e.setCurrency(dto.getCurrency());
        e.setDescription(dto.getDescription());
        e.setSource(dto.getSource());
        e.setMarketplace(dto.getMarketplace());
        e.setNexusMet(dto.getNexusMet());
        e.setHasActiveRegistration(dto.getHasActiveRegistration());
        e.setTotalTaxAmountCalculated(dto.getTotalTaxAmountCalculated());
        e.setTaxableAmount(dto.getTaxableAmount());
        e.setTaxRateCalculated(dto.getTaxRateCalculated());

        if (dto.getAddresses() != null) {
            e.setAddresses(dto.getAddresses().stream()
                    .map(adto -> {
                        AddressEntity ae = AddressConverter.toEntity(adto);
                        ae.setTransaction(e);
                        return ae;
                    }).collect(Collectors.toList()));
        }
        if (dto.getTransactionItems() != null) {
            e.setTransactionItems(dto.getTransactionItems().stream()
                    .map(itemDto -> {
                        TransactionItemEntity tie = TransactionItemConverter.toEntity(itemDto);
                        tie.setTransaction(e);
                        return tie;
                    }).collect(Collectors.toList()));
        }
        return e;
    }

    public static TransactionDTO toDto(TransactionEntity e) {
        if (e == null) return null;
        TransactionDTO dto = new TransactionDTO();
        dto.setDate(e.getDate());
        dto.setExternalId(e.getExternalId());
        dto.setCurrency(e.getCurrency());
        dto.setDescription(e.getDescription());
        dto.setSource(e.getSource());
        dto.setMarketplace(e.getMarketplace());
        dto.setNexusMet(e.getNexusMet());
        dto.setHasActiveRegistration(e.getHasActiveRegistration());
        dto.setTotalTaxAmountCalculated(e.getTotalTaxAmountCalculated());
        dto.setTaxableAmount(e.getTaxableAmount());
        dto.setTaxRateCalculated(e.getTaxRateCalculated());

        if (e.getAddresses() != null) {
            dto.setAddresses(e.getAddresses().stream()
                    .map(AddressConverter::toDto)
                    .collect(Collectors.toList()));
        }
        if (e.getTransactionItems() != null) {
            dto.setTransactionItems(e.getTransactionItems().stream()
                    .map(TransactionItemConverter::toDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
