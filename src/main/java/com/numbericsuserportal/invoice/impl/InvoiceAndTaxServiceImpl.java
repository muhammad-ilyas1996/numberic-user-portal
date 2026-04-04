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
import com.numbericsuserportal.usermanagement.service.UserDataScopeContext;
import com.numbericsuserportal.usermanagement.service.UserDataScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class InvoiceAndTaxServiceImpl implements InvoiceAndTaxService {

    @Autowired
    private InvoiceAndTaxRepo invoiceAndTaxRepo;
    @Autowired
    private DateValidation dateValidation;
    @Autowired
    private UserDataScopeService userDataScopeService;

    @Override
    public InvoiceAndTaxDTO createInvoiceAndTax(InvoiceAndTaxDTO dto, User currentUser) {
        InvoiceAndTaxEntity entity = InvoiceAndTaxConverter.toEntity(dto, currentUser);
        InvoiceAndTaxEntity savedEntity = invoiceAndTaxRepo.save(entity);
        return InvoiceAndTaxConverter.toDTO(savedEntity, currentUser);
    }

    @Override
    public Page<InvoiceAndTaxEntity> searchInvoice(InvoiceSearch request, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        SearchDate searchDate = dateValidation.validateDates(request.getFromDate(), request.getToDate());
        Specification<InvoiceAndTaxEntity> spec = SpecificationUtility.equalsValue("isActive", true);
        if (scope.ownerCreatedByKey().isPresent()) {
            spec = spec.and(SpecificationUtility.equalsValue("createdBy", scope.ownerCreatedByKey().get()));
        }

        if (request.getFromDate() != null && searchDate.getFromDate() != null) {
            spec = spec.and(SpecificationUtility.greaterThanOrEqualTo("createdOn", searchDate.getFromDate()));
        }
        if (request.getToDate() != null && searchDate.getToDate() != null) {
            spec = spec.and(SpecificationUtility.lessThanOrEqualTo("createdOn", searchDate.getToDate()));
        }
        int pageNumber = (request.getPageNumber() != null && request.getPageNumber() > 0) ? request.getPageNumber() : 1;
        int pageSize = (request.getPageSize() != null && request.getPageSize() > 0) ? request.getPageSize() : 20;
        return invoiceAndTaxRepo.findAll(spec,
                PageRequest.of(pageNumber - 1, pageSize, Sort.Direction.DESC, "createdOn")
        );
    }

    @Override
    public InvoiceAndTaxEntity getInvoiceDetail(Long id, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        Optional<InvoiceAndTaxEntity> invoiceAndTaxEntity = invoiceAndTaxRepo.findByIdAndIsActiveTrue(id);
        if (invoiceAndTaxEntity.isEmpty()) {
            return new InvoiceAndTaxEntity();
        }
        InvoiceAndTaxEntity e = invoiceAndTaxEntity.get();
        assertInvoiceAccess(e, scope);
        return e;
    }

    @Override
    public InvoiceAndTaxEntity getInvoiceDetailByCustomerName(String customerName, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        Optional<InvoiceAndTaxEntity> invoiceAndTaxEntity = invoiceAndTaxRepo.findByCustomerName(customerName);
        if (invoiceAndTaxEntity.isEmpty()) {
            return new InvoiceAndTaxEntity();
        }
        InvoiceAndTaxEntity e = invoiceAndTaxEntity.get();
        assertInvoiceAccess(e, scope);
        return e;
    }

    @Override
    public InvoiceAndTaxEntity getInvoiceDetailByInvoiceNumber(String invoiceNum, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        Optional<InvoiceAndTaxEntity> invoiceAndTaxEntity = invoiceAndTaxRepo.findByInvoiceNum(invoiceNum);
        if (invoiceAndTaxEntity.isEmpty()) {
            return new InvoiceAndTaxEntity();
        }
        InvoiceAndTaxEntity e = invoiceAndTaxEntity.get();
        assertInvoiceAccess(e, scope);
        return e;
    }

    @Override
    public void requireAccessibleInvoice(Long invoiceId, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        Optional<InvoiceAndTaxEntity> opt = invoiceAndTaxRepo.findByIdAndIsActiveTrue(invoiceId);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found or inactive");
        }
        assertInvoiceAccess(opt.get(), scope);
    }

    @Override
    public InvoiceAndTaxDTO updateInvoice(Long id, InvoiceAndTaxDTO dto, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        Optional<InvoiceAndTaxEntity> existingInvoice = invoiceAndTaxRepo.findByIdAndIsActiveTrue(id);
        if (existingInvoice.isEmpty()) {
            throw new RuntimeException("Invoice not found with id: " + id);
        }

        InvoiceAndTaxEntity entity = existingInvoice.get();
        assertInvoiceAccess(entity, scope);

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

        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new java.util.Date());

        InvoiceAndTaxEntity savedEntity = invoiceAndTaxRepo.save(entity);
        return InvoiceAndTaxConverter.toDTO(savedEntity, currentUser);
    }

    @Override
    public void deleteInvoice(Long id, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        Optional<InvoiceAndTaxEntity> existingInvoice = invoiceAndTaxRepo.findByIdAndIsActiveTrue(id);
        if (existingInvoice.isEmpty()) {
            throw new RuntimeException("Invoice not found with id: " + id);
        }

        InvoiceAndTaxEntity entity = existingInvoice.get();
        assertInvoiceAccess(entity, scope);

        entity.setIsActive(false);
        entity.setModifiedBy(currentUser.getUserId().toString());
        entity.setModifiedOn(new java.util.Date());

        invoiceAndTaxRepo.save(entity);
    }

    private static void assertInvoiceAccess(InvoiceAndTaxEntity entity, UserDataScopeContext scope) {
        if (entity == null || entity.getId() == null) {
            return;
        }
        if (scope.isPlatformWideDataAccess()) {
            return;
        }
        String key = scope.ownerCreatedByKey().orElseThrow();
        if (entity.getCreatedBy() != null && entity.getCreatedBy().equals(key)) {
            return;
        }
        throw new AccessDeniedException("You do not have access to this invoice");
    }
}
