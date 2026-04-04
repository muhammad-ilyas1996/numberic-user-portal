package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.invoice.dto.RecurringInvoiceCreateRequestDto;
import com.numbericsuserportal.invoice.dto.RecurringInvoiceDto;
import com.numbericsuserportal.invoice.dto.RecurringInvoiceItemDto;
import com.numbericsuserportal.invoice.dto.RecurringInvoiceSearch;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.entity.RecurringInvoice;
import com.numbericsuserportal.invoice.entity.RecurringInvoiceItem;
import com.numbericsuserportal.invoice.repo.InvoiceAndTaxRepo;
import com.numbericsuserportal.invoice.repo.RecurringInvoiceRepo;
import com.numbericsuserportal.invoice.service.RecurringInvoiceService;
import com.numbericsuserportal.invoiceproduct.entity.InvoiceProductEntity;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.service.UserDataScopeContext;
import com.numbericsuserportal.usermanagement.service.UserDataScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RecurringInvoiceServiceImpl implements RecurringInvoiceService {

    private static final String SYSTEM_USER = "RECURRING_SCHEDULER";

    @Autowired
    private RecurringInvoiceRepo recurringInvoiceRepo;
    @Autowired
    private InvoiceAndTaxRepo invoiceAndTaxRepo;
    @Autowired
    private UserDataScopeService userDataScopeService;

    @Override
    @Transactional
    public RecurringInvoiceDto create(RecurringInvoiceCreateRequestDto request, User currentUser) {
        validateFrequency(request.getFrequency());
        if (request.getStartDate() == null) {
            throw new RuntimeException("Start date is required");
        }
        RecurringInvoice entity = mapCreateToEntity(request);
        entity.setNextRunOn(request.getStartDate());
        entity.setStatus(RecurringInvoice.Status.ACTIVE.name());
        entity.setRunCount(0);
        entity.setCreatedBy(currentUser != null ? currentUser.getUserId().toString() : null);
        entity.setCreatedOn(new Date());
        entity.setModifiedBy(entity.getCreatedBy());
        entity.setModifiedOn(entity.getCreatedOn());
        if (request.getDueDays() != null) entity.setDueDays(request.getDueDays());

        if (request.getItems() != null) {
            for (RecurringInvoiceItemDto itemDto : request.getItems()) {
                RecurringInvoiceItem item = mapItemDtoToEntity(itemDto, entity);
                entity.getItems().add(item);
            }
        }
        RecurringInvoice saved = recurringInvoiceRepo.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public RecurringInvoiceDto update(Long id, RecurringInvoiceCreateRequestDto request, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        RecurringInvoice existing = recurringInvoiceRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Recurring invoice not found: " + id));
        assertRecurringAccess(existing, scope);
        validateFrequency(request.getFrequency());

        mapCreateToEntity(request, existing);
        existing.setModifiedBy(currentUser != null ? currentUser.getUserId().toString() : null);
        existing.setModifiedOn(new Date());
        if (request.getDueDays() != null) existing.setDueDays(request.getDueDays());

        existing.getItems().clear();
        if (request.getItems() != null) {
            for (RecurringInvoiceItemDto itemDto : request.getItems()) {
                RecurringInvoiceItem item = mapItemDtoToEntity(itemDto, existing);
                existing.getItems().add(item);
            }
        }
        RecurringInvoice saved = recurringInvoiceRepo.save(existing);
        return toDto(saved);
    }

    @Override
    public Page<RecurringInvoiceDto> list(RecurringInvoiceSearch search, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        int page = search.getPageNumber() != null && search.getPageNumber() > 0 ? search.getPageNumber() - 1 : 0;
        int size = search.getPageSize() != null && search.getPageSize() > 0 ? search.getPageSize() : 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"));

        Page<RecurringInvoice> pageResult;
        if (scope.ownerCreatedByKey().isEmpty()) {
            if (search.getStatus() != null && !search.getStatus().trim().isEmpty()) {
                pageResult = recurringInvoiceRepo.findByStatusOrderByCreatedOnDesc(search.getStatus().trim(), pageable);
            } else {
                pageResult = recurringInvoiceRepo.findAllByOrderByCreatedOnDesc(pageable);
            }
        } else {
            String ob = scope.ownerCreatedByKey().get();
            if (search.getStatus() != null && !search.getStatus().trim().isEmpty()) {
                pageResult = recurringInvoiceRepo.findByStatusAndCreatedByOrderByCreatedOnDesc(search.getStatus().trim(), ob, pageable);
            } else {
                pageResult = recurringInvoiceRepo.findByCreatedByOrderByCreatedOnDesc(ob, pageable);
            }
        }
        return pageResult.map(this::toDto);
    }

    @Override
    public RecurringInvoiceDto getById(Long id, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        RecurringInvoice entity = recurringInvoiceRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Recurring invoice not found: " + id));
        assertRecurringAccess(entity, scope);
        return toDto(entity);
    }

    @Override
    @Transactional
    public void pause(Long id, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        RecurringInvoice entity = recurringInvoiceRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Recurring invoice not found: " + id));
        assertRecurringAccess(entity, scope);
        entity.setStatus(RecurringInvoice.Status.PAUSED.name());
        entity.setModifiedBy(currentUser != null ? currentUser.getUserId().toString() : null);
        entity.setModifiedOn(new Date());
        recurringInvoiceRepo.save(entity);
    }

    @Override
    @Transactional
    public void resume(Long id, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        RecurringInvoice entity = recurringInvoiceRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Recurring invoice not found: " + id));
        assertRecurringAccess(entity, scope);
        entity.setStatus(RecurringInvoice.Status.ACTIVE.name());
        entity.setModifiedBy(currentUser != null ? currentUser.getUserId().toString() : null);
        entity.setModifiedOn(new Date());
        recurringInvoiceRepo.save(entity);
    }

    @Override
    @Transactional
    public void stop(Long id, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);
        RecurringInvoice entity = recurringInvoiceRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Recurring invoice not found: " + id));
        assertRecurringAccess(entity, scope);
        entity.setStatus(RecurringInvoice.Status.ENDED.name());
        entity.setModifiedBy(currentUser != null ? currentUser.getUserId().toString() : null);
        entity.setModifiedOn(new Date());
        recurringInvoiceRepo.save(entity);
    }

    @Override
    @Transactional
    public void runScheduledGeneration() {
        LocalDate today = LocalDate.now();
        List<RecurringInvoice> due = recurringInvoiceRepo
            .findByStatusAndNextRunOnLessThanEqualOrderByNextRunOnAsc(RecurringInvoice.Status.ACTIVE.name(), today);
        for (RecurringInvoice recurring : due) {
            try {
                generateOneInvoice(recurring);
            } catch (Exception e) {
                // log and continue with next
                System.err.println("Recurring invoice generation failed for id=" + recurring.getId() + ": " + e.getMessage());
            }
        }
    }

    private void generateOneInvoice(RecurringInvoice recurring) {
        LocalDate runDate = recurring.getNextRunOn();
        int dueDays = recurring.getDueDays() != null ? recurring.getDueDays() : 30;
        LocalDate dueDate = runDate.plusDays(dueDays);

        InvoiceAndTaxEntity invoice = new InvoiceAndTaxEntity();
        invoice.setRecurringInvoiceId(recurring.getId());
        invoice.setTotalTaxAmountCalculated(recurring.getTotalTaxAmountCalculated());
        invoice.setTaxableAmount(recurring.getTaxableAmount());
        invoice.setNexusMet(recurring.getNexusMet());
        invoice.setTaxRateCalculated(recurring.getTaxRateCalculated());
        invoice.setHasActiveRegistration(recurring.getHasActiveRegistration());
        invoice.setTransactionItems(recurring.getTransactionItems());
        invoice.setInvoiceDate(runDate);
        invoice.setExternalId(recurring.getExternalId());
        invoice.setCurrency(recurring.getCurrency());
        invoice.setDescription(recurring.getDescription());
        invoice.setCustomerName(recurring.getCustomerName());
        invoice.setCustomerEmail(recurring.getCustomerEmail());
        invoice.setCustomerStreet(recurring.getCustomerStreet());
        invoice.setCustomerCity(recurring.getCustomerCity());
        invoice.setCustomerState(recurring.getCustomerState());
        invoice.setCustomerPostalCode(recurring.getCustomerPostalCode());
        invoice.setCustomerCountry(recurring.getCustomerCountry());
        invoice.setShipStreet(recurring.getShipStreet());
        invoice.setShipCity(recurring.getShipCity());
        invoice.setShipState(recurring.getShipState());
        invoice.setShipPostalCode(recurring.getShipPostalCode());
        invoice.setShipCountry(recurring.getShipCountry());

        int nextSeq = recurring.getRunCount() + 1;
        invoice.setInvoiceNum("REC-" + recurring.getId() + "-" + nextSeq);
        invoice.setInvoiceIssueDate(runDate);
        invoice.setInvoiceDueDate(dueDate);
        invoice.setInvoiceStatus("DRAFT");

        String invoiceOwner = recurring.getCreatedBy() != null ? recurring.getCreatedBy() : SYSTEM_USER;
        invoice.setCreatedBy(invoiceOwner);
        invoice.setCreatedOn(new Date());
        invoice.setModifiedBy(SYSTEM_USER);
        invoice.setModifiedOn(new Date());

        List<InvoiceProductEntity> products = new ArrayList<>();
        if (recurring.getItems() != null) {
            for (RecurringInvoiceItem ri : recurring.getItems()) {
                InvoiceProductEntity pe = new InvoiceProductEntity();
                pe.setProductName(ri.getProductName());
                pe.setQuantity(ri.getQuantity());
                pe.setAmount(ri.getAmount());
                pe.setDescription(ri.getDescription());
                pe.setInvoiceAndTaxEntity(invoice);
                products.add(pe);
            }
        }
        invoice.setInvoiceProductEntity(products);

        invoiceAndTaxRepo.save(invoice);

        recurring.setRunCount(nextSeq);
        recurring.setNextRunOn(computeNextRunDate(runDate, recurring.getFrequency()));
        if (recurring.getEndDate() != null && recurring.getNextRunOn().isAfter(recurring.getEndDate())) {
            recurring.setStatus(RecurringInvoice.Status.ENDED.name());
        }
        recurring.setModifiedBy(SYSTEM_USER);
        recurring.setModifiedOn(new Date());
        recurringInvoiceRepo.save(recurring);
    }

    private LocalDate computeNextRunDate(LocalDate current, String frequency) {
        if (frequency == null) return current.plusMonths(1);
        switch (frequency.toUpperCase()) {
            case "WEEKLY": return current.plusWeeks(1);
            case "YEARLY": return current.plusYears(1);
            case "MONTHLY":
            default: return current.plusMonths(1);
        }
    }

    private static void assertRecurringAccess(RecurringInvoice entity, UserDataScopeContext scope) {
        if (scope.isPlatformWideDataAccess()) {
            return;
        }
        String key = scope.ownerCreatedByKey().orElseThrow();
        if (entity.getCreatedBy() != null && entity.getCreatedBy().equals(key)) {
            return;
        }
        throw new AccessDeniedException("You do not have access to this recurring invoice");
    }

    private void validateFrequency(String frequency) {
        if (frequency == null || frequency.trim().isEmpty()) {
            throw new RuntimeException("Frequency is required (WEEKLY, MONTHLY, YEARLY)");
        }
        String f = frequency.toUpperCase();
        if (!f.equals("WEEKLY") && !f.equals("MONTHLY") && !f.equals("YEARLY")) {
            throw new RuntimeException("Invalid frequency. Use WEEKLY, MONTHLY, or YEARLY");
        }
    }

    private RecurringInvoice mapCreateToEntity(RecurringInvoiceCreateRequestDto request) {
        return mapCreateToEntity(request, new RecurringInvoice());
    }

    private RecurringInvoice mapCreateToEntity(RecurringInvoiceCreateRequestDto request, RecurringInvoice entity) {
        entity.setName(request.getName());
        entity.setTotalTaxAmountCalculated(request.getTotalTaxAmountCalculated());
        entity.setTaxableAmount(request.getTaxableAmount());
        entity.setNexusMet(request.getNexusMet());
        entity.setTaxRateCalculated(request.getTaxRateCalculated());
        entity.setHasActiveRegistration(request.getHasActiveRegistration());
        entity.setTransactionItems(request.getTransactionItems());
        entity.setExternalId(request.getExternalId());
        entity.setCurrency(request.getCurrency());
        entity.setDescription(request.getDescription());
        entity.setCustomerName(request.getCustomerName());
        entity.setCustomerEmail(request.getCustomerEmail());
        entity.setCustomerStreet(request.getCustomerStreet());
        entity.setCustomerCity(request.getCustomerCity());
        entity.setCustomerState(request.getCustomerState());
        entity.setCustomerPostalCode(request.getCustomerPostalCode());
        entity.setCustomerCountry(request.getCustomerCountry());
        entity.setShipStreet(request.getShipStreet());
        entity.setShipCity(request.getShipCity());
        entity.setShipState(request.getShipState());
        entity.setShipPostalCode(request.getShipPostalCode());
        entity.setShipCountry(request.getShipCountry());
        entity.setFrequency(request.getFrequency() != null ? request.getFrequency().toUpperCase() : null);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        return entity;
    }

    private RecurringInvoiceItem mapItemDtoToEntity(RecurringInvoiceItemDto dto, RecurringInvoice recurring) {
        RecurringInvoiceItem item = new RecurringInvoiceItem();
        item.setProductName(dto.getProductName());
        item.setQuantity(dto.getQuantity());
        item.setAmount(dto.getAmount());
        item.setDescription(dto.getDescription());
        item.setRecurringInvoice(recurring);
        return item;
    }

    private RecurringInvoiceDto toDto(RecurringInvoice entity) {
        RecurringInvoiceDto dto = new RecurringInvoiceDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setTotalTaxAmountCalculated(entity.getTotalTaxAmountCalculated());
        dto.setTaxableAmount(entity.getTaxableAmount());
        dto.setNexusMet(entity.getNexusMet());
        dto.setTaxRateCalculated(entity.getTaxRateCalculated());
        dto.setHasActiveRegistration(entity.getHasActiveRegistration());
        dto.setTransactionItems(entity.getTransactionItems());
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
        dto.setFrequency(entity.getFrequency());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setNextRunOn(entity.getNextRunOn());
        dto.setStatus(entity.getStatus());
        dto.setRunCount(entity.getRunCount());
        dto.setDueDays(entity.getDueDays());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setModifiedOn(entity.getModifiedOn());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        List<RecurringInvoiceItemDto> itemDtos = new ArrayList<>();
        if (entity.getItems() != null) {
            for (RecurringInvoiceItem item : entity.getItems()) {
                RecurringInvoiceItemDto idto = new RecurringInvoiceItemDto();
                idto.setId(item.getId());
                idto.setProductName(item.getProductName());
                idto.setQuantity(item.getQuantity());
                idto.setAmount(item.getAmount());
                idto.setDescription(item.getDescription());
                itemDtos.add(idto);
            }
        }
        dto.setItems(itemDtos);
        return dto;
    }
}
