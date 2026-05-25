package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.common.validation.DateValidation;
import com.numbericsuserportal.commonpersistence.dto.SearchDate;
import com.numbericsuserportal.commonpersistence.utils.SpecificationUtility;
import com.numbericsuserportal.invoice.dto.PaymentTransactionDto;
import com.numbericsuserportal.invoice.dto.PaymentTransactionSearch;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.entity.PaymentTransaction;
import com.numbericsuserportal.invoice.repo.PaymentTransactionRepo;
import com.numbericsuserportal.invoice.service.PaymentTransactionService;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.service.UserDataScopeContext;
import com.numbericsuserportal.usermanagement.service.UserDataScopeService;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    @Autowired
    private PaymentTransactionRepo paymentTransactionRepo;
    @Autowired
    private DateValidation dateValidation;
    @Autowired
    private UserDataScopeService userDataScopeService;

    @Override
    @SuppressWarnings("unchecked")
    public Page<PaymentTransactionDto> list(PaymentTransactionSearch search, User currentUser) {
        UserDataScopeContext scope = userDataScopeService.resolve(currentUser);

        Specification<PaymentTransaction> spec = null;

        if (search.getInvoiceId() != null) {
            spec = (Specification<PaymentTransaction>) SpecificationUtility.equalsValue("invoiceId", search.getInvoiceId());
        }
        if (search.getStatus() != null && !search.getStatus().trim().isEmpty()) {
            Specification<PaymentTransaction> statusSpec = (Specification<PaymentTransaction>) SpecificationUtility.equalsValue("status", search.getStatus().trim());
            spec = spec == null ? statusSpec : spec.and(statusSpec);
        }
        if (search.getFromDate() != null && search.getToDate() != null) {
            try {
                SearchDate searchDate = dateValidation.validateDates(search.getFromDate(), search.getToDate());
                if (searchDate.getFromDate() != null) {
                    Specification<PaymentTransaction> fromSpec = (Specification<PaymentTransaction>) SpecificationUtility.greaterThanOrEqualTo("paidAt", searchDate.getFromDate());
                    spec = spec == null ? fromSpec : spec.and(fromSpec);
                }
                if (searchDate.getToDate() != null) {
                    Specification<PaymentTransaction> toSpec = (Specification<PaymentTransaction>) SpecificationUtility.lessThanOrEqualTo("paidAt", searchDate.getToDate());
                    spec = spec == null ? toSpec : spec.and(toSpec);
                }
            } catch (Exception ignored) {
                // invalid date range skipped
            }
        }

        if (scope.ownerCreatedByKey().isPresent()) {
            Specification<PaymentTransaction> ownerSpec = forInvoiceOwner(scope.ownerCreatedByKey().get());
            spec = spec == null ? ownerSpec : spec.and(ownerSpec);
        }

        int page = search.getPageNumber() != null && search.getPageNumber() > 0 ? search.getPageNumber() - 1 : 0;
        int size = search.getPageSize() != null && search.getPageSize() > 0 ? search.getPageSize() : 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paidAt"));

        Page<PaymentTransaction> pageResult = spec == null
                ? paymentTransactionRepo.findAll(pageable)
                : paymentTransactionRepo.findAll(spec, pageable);

        return pageResult.map(this::toDto);
    }

    private static Specification<PaymentTransaction> forInvoiceOwner(String createdBy) {
        return (root, query, cb) -> {
            query.distinct(true);
            Subquery<Long> sq = query.subquery(Long.class);
            Root<InvoiceAndTaxEntity> inv = sq.from(InvoiceAndTaxEntity.class);
            sq.select(inv.get("id"));
            sq.where(
                    cb.equal(inv.get("createdBy"), createdBy),
                    cb.isTrue(inv.get("isActive"))
            );
            return cb.in(root.get("invoiceId")).value(sq);
        };
    }

    @Override
    public void saveSuccess(Long invoiceId, String invoiceNum, Double amount, String currency,
                            String gateway, String gatewayTransactionId, String authCode,
                            String payerEmail, String description) {
        PaymentTransaction t = new PaymentTransaction();
        t.setInvoiceId(invoiceId);
        t.setInvoiceNum(invoiceNum);
        t.setAmount(amount);
        t.setCurrency(currency != null ? currency : "USD");
        t.setGateway(gateway);
        t.setGatewayTransactionId(gatewayTransactionId);
        t.setAuthCode(authCode);
        t.setStatus("SUCCESS");
        t.setPaidAt(new Date());
        t.setPayerEmail(payerEmail);
        t.setDescription(description);
        t.setCreatedOn(new Date());
        paymentTransactionRepo.save(t);
    }

    @Override
    public void saveFailure(Long invoiceId, String invoiceNum, Double amount, String currency,
                            String gateway, String gatewayTransactionId, String payerEmail,
                            String description) {
        PaymentTransaction t = new PaymentTransaction();
        t.setInvoiceId(invoiceId);
        t.setInvoiceNum(invoiceNum);
        t.setAmount(amount);
        t.setCurrency(currency != null ? currency : "USD");
        t.setGateway(gateway);
        t.setGatewayTransactionId(gatewayTransactionId);
        t.setStatus("FAILED");
        t.setPaidAt(new Date());
        t.setPayerEmail(payerEmail);
        t.setDescription(truncate(description, 500));
        t.setCreatedOn(new Date());
        paymentTransactionRepo.save(t);
    }

    @Override
    public boolean updateStatusByGatewayTransactionId(String gateway, String gatewayTransactionId,
                                                      String status, String description) {
        if (gatewayTransactionId == null || gatewayTransactionId.trim().isEmpty()) {
            return false;
        }
        return paymentTransactionRepo
                .findFirstByGatewayAndGatewayTransactionIdOrderByIdDesc(gateway, gatewayTransactionId.trim())
                .map(t -> {
                    t.setStatus(status);
                    t.setDescription(truncate(description, 500));
                    paymentTransactionRepo.save(t);
                    return true;
                })
                .orElse(false);
    }

    private PaymentTransactionDto toDto(PaymentTransaction t) {
        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setId(t.getId());
        dto.setInvoiceId(t.getInvoiceId());
        dto.setInvoiceNum(t.getInvoiceNum());
        dto.setAmount(t.getAmount());
        dto.setCurrency(t.getCurrency());
        dto.setGateway(t.getGateway());
        dto.setGatewayTransactionId(t.getGatewayTransactionId());
        dto.setAuthCode(t.getAuthCode());
        dto.setStatus(t.getStatus());
        dto.setPaidAt(t.getPaidAt());
        dto.setPayerEmail(t.getPayerEmail());
        dto.setDescription(t.getDescription());
        return dto;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
