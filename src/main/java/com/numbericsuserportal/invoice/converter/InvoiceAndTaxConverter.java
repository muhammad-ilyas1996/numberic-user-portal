package com.numbericsuserportal.invoice.converter;

import com.numbericsuserportal.invoice.dto.InvoiceAndTaxDTO;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoiceproduct.converter.InvoiceProductConverter;
import com.numbericsuserportal.invoiceproduct.dto.InvoiceProductDTO;
import com.numbericsuserportal.invoiceproduct.entity.InvoiceProductEntity;
import com.numbericsuserportal.usermanagement.domain.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceAndTaxConverter {

    public static InvoiceAndTaxDTO toDTO(InvoiceAndTaxEntity entity, User currentUser) {
        if (entity == null) return null;

        InvoiceAndTaxDTO dto = new InvoiceAndTaxDTO();

        dto.setId(entity.getId());
        dto.setTotalTaxAmountCalculated(entity.getTotalTaxAmountCalculated());
        dto.setTaxableAmount(entity.getTaxableAmount());
        dto.setNexusMet(entity.getNexusMet());
        dto.setTaxRateCalculated(entity.getTaxRateCalculated());
        dto.setHasActiveRegistration(entity.getHasActiveRegistration());
        dto.setTransactionItems(entity.getTransactionItems());

        dto.setInvoiceDate(entity.getInvoiceDate());
        dto.setExternalId(entity.getExternalId());
        dto.setCurrency(entity.getCurrency());
        dto.setDescription(entity.getDescription());

        dto.setCustomerName(entity.getCustomerName());
        dto.setCustomerEmail(entity.getCustomerEmail());
        dto.setCustomerStreet(entity.getCustomerStreet());
        dto.setCustomerCity(entity.getCustomerCity());
        dto.setCustomerState(entity.getCustomerState());
        dto.setCustomerPostalCode(entity.getCustomerPostalCode());
        dto.setCustomerCountry(entity.getCustomerCountry());

        dto.setShipStreet(entity.getShipStreet());
        dto.setShipCity(entity.getShipCity());
        dto.setShipState(entity.getShipState());
        dto.setShipPostalCode(entity.getShipPostalCode());
        dto.setShipCountry(entity.getShipCountry());

        dto.setInvoiceNum(entity.getInvoiceNum());
        dto.setInvoiceIssueDate(entity.getInvoiceIssueDate());
        dto.setInvoiceDueDate(entity.getInvoiceDueDate());
        dto.setInvoiceStatus(entity.getInvoiceStatus());

        // Convert child products
        List<InvoiceProductDTO> productDTOs = new ArrayList<>();
        for (InvoiceProductEntity productEntity : entity.getInvoiceProductEntity()) {
            productDTOs.add(InvoiceProductConverter.toDTO(productEntity));
        }
        dto.setInvoiceProductList(productDTOs);

        //Audit
        dto.setCreatedBy(currentUser.getUserId().toString());
        dto.setCreatedOn(new Date());
        dto.setModifiedBy(currentUser.getUserId().toString());
        dto.setModifiedOn(new Date());

        return dto;
    }

    public static InvoiceAndTaxEntity toEntity(InvoiceAndTaxDTO dto, User currentUser) {
        if (dto == null) return null;

        InvoiceAndTaxEntity entity = new InvoiceAndTaxEntity();

        entity.setId(dto.getId());
        entity.setTotalTaxAmountCalculated(dto.getTotalTaxAmountCalculated());
        entity.setTaxableAmount(dto.getTaxableAmount());
        entity.setNexusMet(dto.getNexusMet());
        entity.setTaxRateCalculated(dto.getTaxRateCalculated());
        entity.setHasActiveRegistration(dto.getHasActiveRegistration());
        entity.setTransactionItems(dto.getTransactionItems());

        entity.setInvoiceDate(dto.getInvoiceDate());
        entity.setExternalId(dto.getExternalId());
        entity.setCurrency(dto.getCurrency());
        entity.setDescription(dto.getDescription());

        entity.setCustomerName(dto.getCustomerName());
        entity.setCustomerEmail(dto.getCustomerEmail());
        entity.setCustomerStreet(dto.getCustomerStreet());
        entity.setCustomerCity(dto.getCustomerCity());
        entity.setCustomerState(dto.getCustomerState());
        entity.setCustomerPostalCode(dto.getCustomerPostalCode());
        entity.setCustomerCountry(dto.getCustomerCountry());

        entity.setShipStreet(dto.getShipStreet());
        entity.setShipCity(dto.getShipCity());
        entity.setShipState(dto.getShipState());
        entity.setShipPostalCode(dto.getShipPostalCode());
        entity.setShipCountry(dto.getShipCountry());

        entity.setInvoiceNum(dto.getInvoiceNum());
        entity.setInvoiceIssueDate(dto.getInvoiceIssueDate());
        entity.setInvoiceDueDate(dto.getInvoiceDueDate());
        entity.setInvoiceStatus(dto.getInvoiceStatus());

        // Convert child product list
        List<InvoiceProductEntity> productEntities = new ArrayList<>();
        for (InvoiceProductDTO productDTO : dto.getInvoiceProductList()) {
            InvoiceProductEntity productEntity = InvoiceProductConverter.toEntity(productDTO);
            productEntity.setInvoiceAndTaxEntity(entity);
            productEntities.add(productEntity);
        }
        entity.setInvoiceProductEntity(productEntities);

        //Audit
        entity.setCreatedBy(currentUser.getUserId().toString());
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new Date());

        return entity;
    }
}
