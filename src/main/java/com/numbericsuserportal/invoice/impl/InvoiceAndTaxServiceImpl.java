package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.common.validation.DateValidation;
import com.numbericsuserportal.commonpersistence.dto.SearchDate;
import com.numbericsuserportal.commonpersistence.utils.SpecificationUtility;
import com.numbericsuserportal.invoice.converter.InvoiceAndTaxConverter;
import com.numbericsuserportal.invoice.dto.InvoiceAndTaxDTO;
import com.numbericsuserportal.invoice.dto.InvoiceSearch;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.repo.InvoiceAndTaxRepo;
import com.numbericsuserportal.invoice.service.InvoiceAndTaxService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvoiceAndTaxServiceImpl implements InvoiceAndTaxService {

    @Autowired
    private InvoiceAndTaxRepo invoiceAndTaxRepo;
    @Autowired
    private DateValidation dateValidation;
    @Override
    public InvoiceAndTaxDTO createInvoiceAndTax(InvoiceAndTaxDTO dto, User currentUser) {
        InvoiceAndTaxEntity entity = InvoiceAndTaxConverter.toEntity(dto,currentUser);
        InvoiceAndTaxEntity savedEntity = invoiceAndTaxRepo.save(entity);
        return InvoiceAndTaxConverter.toDTO(savedEntity,currentUser);
    }

    @Override
    public Page<InvoiceAndTaxEntity> searchInvoice(InvoiceSearch request) {
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<InvoiceAndTaxEntity> spec = SpecificationUtility.equalsValue("isActive", true);

        if (request.getFromDate() != null) {
            spec = spec.and(SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (request.getToDate() != null) {
            spec = spec.and(SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        return invoiceAndTaxRepo.findAll(spec,
                PageRequest.of(request.getPageNumber()-1,
                        request.getPageSize(),
                        Sort.Direction.DESC,
                        "createdOn")
        );
    }

    @Override
    public InvoiceAndTaxEntity getInvoiceDetail(Long id) {
        Optional<InvoiceAndTaxEntity> invoiceAndTaxEntity = invoiceAndTaxRepo.findByIdAndIsActiveTrue(id);
        if (invoiceAndTaxEntity.isEmpty()) {
            return new InvoiceAndTaxEntity();
        } else {
            return invoiceAndTaxEntity.get();
        }
    }
    @Override
    public InvoiceAndTaxEntity getInvoiceDetailByCustomerName(String customerName) {
        Optional<InvoiceAndTaxEntity> invoiceAndTaxEntity = invoiceAndTaxRepo.findByCustomerName(customerName);
        if (invoiceAndTaxEntity.isEmpty()) {
            return new InvoiceAndTaxEntity();
        } else {
            return invoiceAndTaxEntity.get();
        }
    }
    @Override
    public InvoiceAndTaxEntity getInvoiceDetailByInvoiceNumber(String invoiceNum) {
        Optional<InvoiceAndTaxEntity> invoiceAndTaxEntity = invoiceAndTaxRepo.findByInvoiceNum(invoiceNum);
        if (invoiceAndTaxEntity.isEmpty()) {
            return new InvoiceAndTaxEntity();
        } else {
            return invoiceAndTaxEntity.get();
        }
    }

    @Override
    public InvoiceAndTaxDTO updateInvoice(Long id, InvoiceAndTaxDTO dto, User currentUser) {
        Optional<InvoiceAndTaxEntity> existingInvoice = invoiceAndTaxRepo.findByIdAndIsActiveTrue(id);
        if (existingInvoice.isEmpty()) {
            throw new RuntimeException("Invoice not found with id: " + id);
        }
        
        InvoiceAndTaxEntity entity = existingInvoice.get();
        
        // Update fields
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
        
        // Update audit fields
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new java.util.Date());
        
        InvoiceAndTaxEntity savedEntity = invoiceAndTaxRepo.save(entity);
        return InvoiceAndTaxConverter.toDTO(savedEntity, currentUser);
    }

    @Override
    public void deleteInvoice(Long id, User currentUser) {
        Optional<InvoiceAndTaxEntity> existingInvoice = invoiceAndTaxRepo.findByIdAndIsActiveTrue(id);
        if (existingInvoice.isEmpty()) {
            throw new RuntimeException("Invoice not found with id: " + id);
        }
        
        InvoiceAndTaxEntity entity = existingInvoice.get();
        
        // Soft delete - set isActive to false
        entity.setIsActive(false);
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new java.util.Date());
        
        invoiceAndTaxRepo.save(entity);
    }
}
