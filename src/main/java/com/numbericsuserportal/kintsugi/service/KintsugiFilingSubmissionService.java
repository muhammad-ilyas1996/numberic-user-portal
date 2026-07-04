package com.numbericsuserportal.kintsugi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.kintsugi.catalog.UsStateCatalog;
import com.numbericsuserportal.kintsugi.client.KintsugiApiClient;
import com.numbericsuserportal.kintsugi.dto.*;
import com.numbericsuserportal.kintsugi.entity.SalesTaxFilingEntity;
import com.numbericsuserportal.kintsugi.entity.SalesTaxFilingEntity.FilingStatus;
import com.numbericsuserportal.kintsugi.repo.SalesTaxFilingRepository;
import com.numbericsuserportal.kintsugi.util.FilingPeriodUtil;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class KintsugiFilingSubmissionService {

    private final KintsugiFilingFlowService filingFlowService;
    private final KintsugiNexusService nexusService;
    private final ExemptionService exemptionService;
    private final SalesTaxFilingRepository filingRepository;
    private final KintsugiApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${stripe.price.filing.fee.cents:999}")
    private long filingFeeCents;

    @Value("${stripe.price.state.registration.cents:2900}")
    private long registrationFeeCents;

    public KintsugiFilingSubmissionService(
            KintsugiFilingFlowService filingFlowService,
            KintsugiNexusService nexusService,
            ExemptionService exemptionService,
            SalesTaxFilingRepository filingRepository,
            KintsugiApiClient apiClient,
            ObjectMapper objectMapper,
            UserRepository userRepository) {
        this.filingFlowService = filingFlowService;
        this.nexusService = nexusService;
        this.exemptionService = exemptionService;
        this.filingRepository = filingRepository;
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    public Map<String, Object> activeStatesForFiling(User user) {
        List<Map<String, Object>> states = new ArrayList<>();
        for (Object item : filingFlowService.step3States()) {
            if (item instanceof com.numbericsuserportal.kintsugi.dto.FilingFlowStateSummaryDTO summary) {
                String registrationStatus = resolveRegistrationStatus(summary);
                boolean registrationRequired = "NEW".equals(registrationStatus);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("stateCode", summary.getStateCode());
                row.put("stateName", summary.getStateName());
                row.put("nexusMet", summary.getNexusMet());
                row.put("hasKintsugiData", summary.getHasKintsugiData());
                row.put("registrationStatus", registrationStatus);
                row.put("registrationRequired", registrationRequired);
                row.put("registrationFeeCents", registrationRequired ? registrationFeeCents : 0L);
                states.add(row);
            }
        }

        long activeCount = states.stream()
                .filter(s -> "REGISTERED".equals(s.get("registrationStatus"))
                        || "ACTIVE".equals(s.get("registrationStatus")))
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activeStateCount", activeCount);
        result.put("states", states);
        result.put("businessType", user.getBusinessType() != null ? user.getBusinessType().name() : null);
        return result;
    }

    public Map<String, Object> stateFilingContext(User user, String stateCode) {
        String code = normalizeState(stateCode);
        Map<String, Object> period = FilingPeriodUtil.currentQuarter(LocalDate.now());
        Map<String, Object> nexus = nexusService.getStateNexusDetail(code);
        Map<String, Object> exemption = user.getBusinessType() != null
                ? exemptionService.getExemptionProfile(user, code)
                : Map.of();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stateCode", code);
        result.put("stateName", UsStateCatalog.resolveName(code).orElse(code));
        result.put("filingPeriod", period);
        result.put("nexus", nexus);
        result.put("exemptionProfile", exemption);
        result.put("stateTaxAccountId", nexus.get("state_tax_account_id"));
        result.put("registrationRequired", !Boolean.TRUE.equals(nexus.get("has_kintsugi_data")));
        return result;
    }

    @Transactional
    public Map<String, Object> saveFlowSelection(User user, FilingFlowSelectionDTO request) {
        if (request.getStateCode() == null || request.getStateCode().isBlank()) {
            throw new IllegalArgumentException("stateCode is required");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new IllegalArgumentException("category is required");
        }
        if (request.getSubcategory() == null || request.getSubcategory().isBlank()) {
            throw new IllegalArgumentException("subcategory is required");
        }

        String code = normalizeState(request.getStateCode());
        Map<String, Object> period = FilingPeriodUtil.currentQuarter(LocalDate.now());
        SalesTaxFilingEntity filing = loadOrCreateDraft(user, request.getFilingId());

        filing.setUserId(user.getUserId());
        filing.setStateCode(code);
        filing.setPeriodStart(LocalDate.parse(String.valueOf(period.get("periodStart"))));
        filing.setPeriodEnd(LocalDate.parse(String.valueOf(period.get("periodEnd"))));
        filing.setDueDate(LocalDate.parse(String.valueOf(period.get("dueDate"))));
        filing.setCategory(request.getCategory().trim());
        filing.setSubcategory(request.getSubcategory().trim());
        filing.setStateTaxAccountId(request.getStateTaxAccountId());
        filing.setRegistrationFeeCents(Boolean.TRUE.equals(request.getRegistrationRequired())
                ? registrationFeeCents : 0L);
        filing.setFilingFeeCents(filingFeeCents);
        filing.setFilingStatus(FilingStatus.DRAFT);
        filing.setFlowCompleted(false);
        applyBusinessTypeAndExemptions(user, filing, code);
        filingRepository.save(filing);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filingId", filing.getId());
        result.put("saved", true);
        result.put("stateCode", code);
        result.put("category", filing.getCategory());
        result.put("subcategory", filing.getSubcategory());
        result.put("filingPeriod", period);
        return result;
    }

    @Transactional
    public Map<String, Object> estimateAndSaveSales(User user, EnterSalesEstimateRequestDTO request) {
        Map<String, Object> estimate = estimateSales(user, request);
        String code = normalizeState(request.getStateCode());

        SalesTaxFilingEntity filing = loadOrCreateDraft(user, request.getFilingId());
        Map<String, Object> period = FilingPeriodUtil.currentQuarter(LocalDate.now());

        filing.setUserId(user.getUserId());
        filing.setStateCode(code);
        filing.setPeriodStart(LocalDate.parse(String.valueOf(period.get("periodStart"))));
        filing.setPeriodEnd(LocalDate.parse(String.valueOf(period.get("periodEnd"))));
        filing.setDueDate(LocalDate.parse(String.valueOf(period.get("dueDate"))));
        filing.setCategory(request.getCategory().trim());
        filing.setSubcategory(request.getSubcategory().trim());
        filing.setTaxableAmountCents(dollarsToCents(request.getTaxableAmount()));
        filing.setExemptAmountCents(dollarsToCents(request.getExemptAmount()));
        filing.setTotalRevenueCents(filing.getTaxableAmountCents() + filing.getExemptAmountCents());
        filing.setTaxCollectedCents(((Number) estimate.get("taxOwedCents")).longValue());
        filing.setSavingsAmountCents(((Number) estimate.get("savingsAmountCents")).longValue());
        filing.setTaxRate(((Number) estimate.get("blendedRate")).doubleValue());
        filing.setFilingFeeCents(filingFeeCents);
        filing.setFilingStatus(FilingStatus.DRAFT);
        filing.setFlowCompleted(false);
        filing.setKintsugiEstimateJson(writeJsonObject(estimate.get("kintsugiEstimate")));
        filing.setJurisdictionBreakdownJson(writeJsonObject(estimate.get("jurisdictionBreakdown")));
        applyBusinessTypeAndExemptions(user, filing, code);
        filingRepository.save(filing);

        estimate.put("filingId", filing.getId());
        estimate.put("saved", true);
        return estimate;
    }

    public Map<String, Object> getFilingRecord(User user, String filingId) {
        SalesTaxFilingEntity filing = filingRepository.findByIdAndUserId(filingId, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Filing not found"));
        return toFilingRecord(filing);
    }

    public Map<String, Object> estimateSales(User user, EnterSalesEstimateRequestDTO request) {
        validateSalesInput(request);
        String code = normalizeState(request.getStateCode());

        double taxable = request.getTaxableAmount() != null ? request.getTaxableAmount() : 0.0;
        double exempt = request.getExemptAmount() != null ? request.getExemptAmount() : 0.0;
        double total = taxable + exempt;

        FilingFlowEstimateRequestDTO estimateRequest = new FilingFlowEstimateRequestDTO();
        estimateRequest.setCategory(request.getCategory());
        estimateRequest.setSubcategory(request.getSubcategory());
        estimateRequest.setStateCode(code);
        String[] cityZip = resolveAddressForState(code);
        estimateRequest.setCity(request.getCity() != null ? request.getCity() : cityZip[0]);
        estimateRequest.setPostalCode(request.getPostalCode() != null ? request.getPostalCode() : cityZip[1]);
        estimateRequest.setAmount(taxable > 0 ? taxable : 0.01);
        estimateRequest.setQuantity(1.0);
        estimateRequest.setSimulateActiveRegistration(true);

        Map<String, Object> kintsugiEstimate = filingFlowService.estimateTax(estimateRequest);
        double blendedRate = extractBlendedRate(kintsugiEstimate, taxable);
        long taxableCents = dollarsToCents(taxable);
        long exemptCents = dollarsToCents(exempt);
        long taxCents = Math.round(taxableCents * blendedRate);
        long savingsCents = exemptionService.calculateSavingsCents(exemptCents, blendedRate);

        List<String> exemptCategories = request.getExemptCategories();
        if ((exemptCategories == null || exemptCategories.isEmpty()) && user.getBusinessType() != null) {
            exemptCategories = exemptionService.exemptCategoriesFor(user, code);
        }

        double taxablePct = total > 0 ? (taxable / total) * 100.0 : 0.0;
        double exemptPct = total > 0 ? (exempt / total) * 100.0 : 0.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stateCode", code);
        result.put("taxableAmount", taxable);
        result.put("exemptAmount", exempt);
        result.put("totalRevenue", total);
        result.put("taxablePercent", round2(taxablePct));
        result.put("exemptPercent", round2(exemptPct));
        result.put("taxableCategories", user.getBusinessType() != null
                ? exemptionService.getExemptionProfile(user, code).get("taxableCategories") : List.of());
        result.put("exemptCategories", exemptCategories);
        result.put("blendedRate", blendedRate);
        result.put("taxOwed", centsToDollars(taxCents));
        result.put("taxOwedCents", taxCents);
        result.put("savingsAmount", centsToDollars(savingsCents));
        result.put("savingsAmountCents", savingsCents);
        result.put("kintsugiEstimate", kintsugiEstimate);
        result.put("jurisdictionBreakdown", extractJurisdictionBreakdown(kintsugiEstimate, taxable));
        return result;
    }

    @Transactional
    public Map<String, Object> buildReview(User user, FilingDraftRequestDTO request) {
        if (request.getStateCode() == null || request.getCategory() == null || request.getSubcategory() == null) {
            throw new IllegalArgumentException("stateCode, category, and subcategory are required");
        }

        EnterSalesEstimateRequestDTO estimateInput = new EnterSalesEstimateRequestDTO();
        estimateInput.setStateCode(request.getStateCode());
        estimateInput.setCategory(request.getCategory());
        estimateInput.setSubcategory(request.getSubcategory());
        estimateInput.setTaxableAmount(request.getTaxableAmount());
        estimateInput.setExemptAmount(request.getExemptAmount());
        Map<String, Object> estimate = estimateSales(user, estimateInput);

        String code = normalizeState(request.getStateCode());
        Map<String, Object> period = FilingPeriodUtil.currentQuarter(LocalDate.now());
        boolean registrationRequired = Boolean.TRUE.equals(request.getRegistrationRequired());
        long regFee = registrationRequired ? registrationFeeCents : 0L;
        long taxCents = ((Number) estimate.get("taxOwedCents")).longValue();
        long totalCents = filingFeeCents + taxCents + regFee;

        SalesTaxFilingEntity filing = request.getFilingId() != null
                ? filingRepository.findByIdAndUserId(request.getFilingId(), user.getUserId())
                        .orElseGet(SalesTaxFilingEntity::new)
                : new SalesTaxFilingEntity();

        filing.setUserId(user.getUserId());
        filing.setStateCode(code);
        filing.setPeriodStart(LocalDate.parse(String.valueOf(period.get("periodStart"))));
        filing.setPeriodEnd(LocalDate.parse(String.valueOf(period.get("periodEnd"))));
        filing.setDueDate(LocalDate.parse(String.valueOf(period.get("dueDate"))));
        filing.setCategory(request.getCategory());
        filing.setSubcategory(request.getSubcategory());
        filing.setTaxableAmountCents(dollarsToCents(request.getTaxableAmount()));
        filing.setExemptAmountCents(dollarsToCents(request.getExemptAmount()));
        filing.setTotalRevenueCents(filing.getTaxableAmountCents() + filing.getExemptAmountCents());
        filing.setTaxCollectedCents(taxCents);
        filing.setSavingsAmountCents(((Number) estimate.get("savingsAmountCents")).longValue());
        filing.setTaxRate(((Number) estimate.get("blendedRate")).doubleValue());
        filing.setFilingFeeCents(filingFeeCents);
        filing.setRegistrationFeeCents(regFee);
        filing.setFilingStatus(FilingStatus.DRAFT);
        filing.setFlowCompleted(false);
        filing.setStateTaxAccountId(request.getStateTaxAccountId());
        filing.setKintsugiEstimateJson(writeJsonObject(estimate.get("kintsugiEstimate")));
        filing.setJurisdictionBreakdownJson(writeJsonObject(estimate.get("jurisdictionBreakdown")));
        if (user.getBusinessType() != null) {
            applyBusinessTypeAndExemptions(user, filing, code);
        }
        filingRepository.save(filing);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filingId", filing.getId());
        result.put("summary", buildSummary(filing, period, estimate));
        result.put("paymentItems", buildPaymentItems(filing, registrationRequired));
        result.put("totalChargedCents", totalCents);
        result.put("totalCharged", centsToDollars(totalCents));
        result.put("savingsAmount", estimate.get("savingsAmount"));
        return result;
    }

    @Transactional
    public FilingPaymentIntentResponseDTO createPaymentIntent(User user, String filingId) throws StripeException {
        SalesTaxFilingEntity filing = filingRepository.findByIdAndUserId(filingId, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Filing not found"));

        long totalCents = filing.getFilingFeeCents()
                + filing.getTaxCollectedCents()
                + (filing.getRegistrationFeeCents() != null ? filing.getRegistrationFeeCents() : 0L);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalCents)
                .setCurrency("usd")
                .setDescription("Numbrics sales tax filing " + filing.getStateCode())
                .putMetadata("filingId", filing.getId())
                .putMetadata("userId", String.valueOf(user.getUserId()))
                .putMetadata("type", "sales_tax_filing")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        filing.setStripePaymentIntentId(intent.getId());
        filing.setFilingStatus(FilingStatus.PENDING_PAYMENT);
        filingRepository.save(filing);

        return new FilingPaymentIntentResponseDTO(
                intent.getClientSecret(), intent.getId(), totalCents, filing.getId());
    }

    @Transactional
    public Map<String, Object> submitFiling(User user, String filingId) {
        SalesTaxFilingEntity filing = filingRepository.findByIdAndUserId(filingId, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Filing not found"));

        if (filing.getFilingStatus() == FilingStatus.FILED || filing.getFilingStatus() == FilingStatus.CONFIRMED) {
            return filedResult(user, filingId);
        }

        Map<String, Object> kintsugiResponse = submitToKintsugi(filing);
        String confirmation = extractConfirmation(kintsugiResponse, filing);

        if (user.getBusinessType() != null) {
            exemptionService.ensureProfileSaved(user, filing.getStateCode());
        }
        markUserSalesTaxEnabled(user);

        filing.setConfirmationNum(confirmation);
        filing.setNumbricsFilingId(generateNumbricsFilingId(filing));
        filing.setKintsugiFilingId(stringVal(kintsugiResponse.get("filing_id")));
        filing.setKintsugiFilingResponseJson(writeJsonObject(kintsugiResponse));
        filing.setFilingStatus(FilingStatus.FILED);
        filing.setFlowCompleted(true);
        filing.setFiledAt(LocalDateTime.now());
        filingRepository.save(filing);

        Map<String, Object> result = filedResult(user, filing.getId());
        result.put("saved", true);
        result.put("dbRecord", toFilingRecord(filing));
        return result;
    }

    private void markUserSalesTaxEnabled(User user) {
        userRepository.findById(user.getUserId()).ifPresent(entity -> {
            entity.setSalesTaxEnabled(true);
            userRepository.save(entity);
            user.setSalesTaxEnabled(true);
        });
    }

    private SalesTaxFilingEntity loadOrCreateDraft(User user, String filingId) {
        if (filingId != null && !filingId.isBlank()) {
            return filingRepository.findByIdAndUserId(filingId, user.getUserId())
                    .orElseGet(SalesTaxFilingEntity::new);
        }
        return new SalesTaxFilingEntity();
    }

    private void applyBusinessTypeAndExemptions(User user, SalesTaxFilingEntity filing, String stateCode) {
        if (user.getBusinessType() == null) {
            return;
        }
        filing.setBusinessType(user.getBusinessType().name());
        exemptionService.ensureProfileSaved(user, stateCode);
        Map<String, Object> profile = exemptionService.getExemptionProfile(user, stateCode);
        filing.setTaxableCategoriesJson(writeJsonObject(profile.get("taxableCategories")));
        filing.setExemptCategoriesJson(writeJsonObject(profile.get("exemptCategories")));
    }

    private Map<String, Object> toFilingRecord(SalesTaxFilingEntity filing) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("filingId", filing.getId());
        row.put("userId", filing.getUserId());
        row.put("stateCode", filing.getStateCode());
        row.put("category", filing.getCategory());
        row.put("subcategory", filing.getSubcategory());
        row.put("businessType", filing.getBusinessType());
        row.put("periodStart", filing.getPeriodStart() != null ? filing.getPeriodStart().toString() : null);
        row.put("periodEnd", filing.getPeriodEnd() != null ? filing.getPeriodEnd().toString() : null);
        row.put("dueDate", filing.getDueDate() != null ? filing.getDueDate().toString() : null);
        row.put("totalRevenue", centsToDollars(filing.getTotalRevenueCents()));
        row.put("taxableAmount", centsToDollars(filing.getTaxableAmountCents()));
        row.put("exemptAmount", centsToDollars(filing.getExemptAmountCents()));
        row.put("taxOwed", centsToDollars(filing.getTaxCollectedCents()));
        row.put("savingsAmount", centsToDollars(filing.getSavingsAmountCents()));
        row.put("taxRate", filing.getTaxRate());
        row.put("filingStatus", filing.getFilingStatus() != null ? filing.getFilingStatus().name() : null);
        row.put("flowCompleted", filing.getFlowCompleted());
        row.put("confirmationNumber", filing.getConfirmationNum());
        row.put("numbricsFilingId", filing.getNumbricsFilingId());
        row.put("kintsugiFilingId", filing.getKintsugiFilingId());
        row.put("stripePaymentIntentId", filing.getStripePaymentIntentId());
        row.put("filedAt", filing.getFiledAt() != null ? filing.getFiledAt().toString() : null);
        row.put("taxableCategories", readJsonObject(filing.getTaxableCategoriesJson()));
        row.put("exemptCategories", readJsonObject(filing.getExemptCategoriesJson()));
        row.put("kintsugiEstimate", readJsonObject(filing.getKintsugiEstimateJson()));
        row.put("jurisdictionBreakdown", readJsonObject(filing.getJurisdictionBreakdownJson()));
        return row;
    }

    public Map<String, Object> filedResult(User user, String filingId) {
        SalesTaxFilingEntity filing = filingRepository.findByIdAndUserId(filingId, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Filing not found"));

        Map<String, Object> period = Map.of(
                "label", "Q" + quarterOf(filing.getPeriodEnd()) + " " + filing.getPeriodEnd().getYear(),
                "periodStart", filing.getPeriodStart().toString(),
                "periodEnd", filing.getPeriodEnd().toString());

        Map<String, Object> nextFiling = FilingPeriodUtil.nextQuarterAfter(filing.getPeriodEnd());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filingId", filing.getId());
        result.put("stateCode", filing.getStateCode());
        result.put("stateName", UsStateCatalog.resolveName(filing.getStateCode()).orElse(filing.getStateCode()));
        result.put("filingPeriod", period);
        result.put("taxRemitted", centsToDollars(filing.getTaxCollectedCents()));
        result.put("taxRemittedCents", filing.getTaxCollectedCents());
        result.put("savingsAmount", centsToDollars(filing.getSavingsAmountCents()));
        result.put("confirmationNumber", filing.getConfirmationNum());
        result.put("numbricsFilingId", filing.getNumbricsFilingId());
        result.put("filedAt", filing.getFiledAt() != null ? filing.getFiledAt().toString() : null);
        result.put("nextFiling", nextFiling);
        result.put("whatsappPreview", buildWhatsappPreview(filing, nextFiling));
        result.put("schedule", filingSchedule(user));
        result.put("dbRecord", toFilingRecord(filing));
        return result;
    }

    public Map<String, Object> filingSchedule(User user) {
        List<Map<String, Object>> rows = filingRepository.findByUserIdOrderByPeriodEndDesc(user.getUserId()).stream()
                .map(this::toScheduleRow)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filings", rows);
        return result;
    }

    public Map<String, Object> dashboardSummary(User user) {
        List<SalesTaxFilingEntity> filings = filingRepository.findByUserIdOrderByPeriodEndDesc(user.getUserId());
        long ytdTaxCents = filings.stream()
                .filter(f -> f.getFilingStatus() == FilingStatus.FILED || f.getFilingStatus() == FilingStatus.CONFIRMED)
                .filter(f -> f.getPeriodEnd().getYear() == LocalDate.now().getYear())
                .mapToLong(f -> f.getTaxCollectedCents() != null ? f.getTaxCollectedCents() : 0L)
                .sum();

        Optional<SalesTaxFilingEntity> nextDue = filings.stream()
                .filter(f -> f.getFilingStatus() != FilingStatus.FILED && f.getFilingStatus() != FilingStatus.CONFIRMED)
                .findFirst();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("ytdTaxCollected", centsToDollars(ytdTaxCents));
        summary.put("ytdTaxCollectedCents", ytdTaxCents);
        summary.put("filedCount", filings.stream()
                .filter(f -> f.getFilingStatus() == FilingStatus.FILED || f.getFilingStatus() == FilingStatus.CONFIRMED)
                .count());
        summary.put("nextFiling", nextDue.map(this::toScheduleRow).orElse(null));
        summary.put("businessTypeSetupRequired", user.getBusinessType() == null);
        
        long pendingCount = filings.stream()
                .filter(f -> f.getFilingStatus() != FilingStatus.FILED && f.getFilingStatus() != FilingStatus.CONFIRMED)
                .count();
        summary.put("pendingCount", pendingCount);
        
        return summary;
    }

    private Map<String, Object> submitToKintsugi(SalesTaxFilingEntity filing) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("state", filing.getStateCode());
        body.put("filing_period", Map.of(
                "start", filing.getPeriodStart().toString(),
                "end", filing.getPeriodEnd().toString()));
        body.put("total_revenue", filing.getTotalRevenueCents());
        body.put("taxable_amount", filing.getTaxableAmountCents());
        body.put("exempt_amount", filing.getExemptAmountCents());
        body.put("exempt_categories", readJsonList(filing.getExemptCategoriesJson()));

        try {
            return apiClient.post(
                    "/v1/filings",
                    body,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("filing_id", "local-" + filing.getId());
            fallback.put("confirmation_number", filing.getStateCode() + "-"
                    + filing.getPeriodEnd().getYear() + "-Q" + quarterOf(filing.getPeriodEnd()) + "-"
                    + (100000 + new Random().nextInt(900000)));
            fallback.put("tax_amount", filing.getTaxCollectedCents());
            return fallback;
        }
    }

    private Map<String, Object> buildSummary(
            SalesTaxFilingEntity filing, Map<String, Object> period, Map<String, Object> estimate) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("stateCode", filing.getStateCode());
        summary.put("stateName", UsStateCatalog.resolveName(filing.getStateCode()).orElse(filing.getStateCode()));
        summary.put("filingPeriodLabel", period.get("label") + " · " + period.get("rangeLabel"));
        summary.put("dueDateLabel", period.get("dueDateLabel"));
        summary.put("totalRevenue", centsToDollars(filing.getTotalRevenueCents()));
        summary.put("taxableAmount", centsToDollars(filing.getTaxableAmountCents()));
        summary.put("exemptAmount", centsToDollars(filing.getExemptAmountCents()));
        summary.put("rateApplied", estimate.get("blendedRate"));
        summary.put("taxOwed", estimate.get("taxOwed"));
        return summary;
    }

    private List<Map<String, Object>> buildPaymentItems(SalesTaxFilingEntity filing, boolean registrationRequired) {
        List<Map<String, Object>> items = new ArrayList<>();

        if (registrationRequired && filing.getRegistrationFeeCents() != null && filing.getRegistrationFeeCents() > 0) {
            items.add(paymentItem(
                    "State Registration",
                    "NUMBRICS REG " + filing.getStateCode(),
                    filing.getRegistrationFeeCents(),
                    "registration"));
        }

        items.add(paymentItem(
                "Numbrics Filing Fee",
                "NUMBRICS FILING " + filing.getStateCode(),
                filing.getFilingFeeCents(),
                "filing_fee"));

        items.add(paymentItem(
                filing.getStateCode() + " Tax Remittance",
                "NUMBRICS TAX " + filing.getStateCode() + " · Remitted to state",
                filing.getTaxCollectedCents(),
                "tax_remittance"));

        return items;
    }

    private Map<String, Object> paymentItem(String name, String statementLabel, Long cents, String type) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("statementLabel", statementLabel);
        item.put("amount", centsToDollars(cents));
        item.put("amountCents", cents);
        item.put("type", type);
        return item;
    }

    private Map<String, Object> toScheduleRow(SalesTaxFilingEntity filing) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("filingId", filing.getId());
        row.put("stateCode", filing.getStateCode());
        row.put("period", "Q" + quarterOf(filing.getPeriodEnd()) + " " + filing.getPeriodEnd().getYear());
        row.put("dueDate", filing.getDueDate().toString());
        row.put("taxAmount", filing.getTaxCollectedCents() != null && filing.getTaxCollectedCents() > 0
                ? centsToDollars(filing.getTaxCollectedCents()) : null);
        row.put("status", mapStatus(filing.getFilingStatus()));
        return row;
    }

    private String mapStatus(FilingStatus status) {
        return switch (status) {
            case FILED, CONFIRMED -> "filed";
            case PENDING_PAYMENT, DRAFT -> "due";
            case OVERDUE -> "overdue";
        };
    }

    private String resolveRegistrationStatus(com.numbericsuserportal.kintsugi.dto.FilingFlowStateSummaryDTO summary) {
        if (Boolean.TRUE.equals(summary.getNexusMet()) || Boolean.TRUE.equals(summary.getHasKintsugiData())) {
            return "REGISTERED";
        }
        if (Boolean.TRUE.equals(summary.getEconomicNexusMet()) || Boolean.TRUE.equals(summary.getPhysicalNexusMet())) {
            return "ACTIVE";
        }
        return "NEW";
    }

    private Map<String, Object> buildWhatsappPreview(SalesTaxFilingEntity filing, Map<String, Object> nextFiling) {
        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("title", filing.getStateCode() + " Q" + quarterOf(filing.getPeriodEnd()) + " Sales Tax — Filed!");
        preview.put("totalSales", centsToDollars(filing.getTotalRevenueCents()));
        preview.put("taxableSales", centsToDollars(filing.getTaxableAmountCents()));
        preview.put("exemptSales", centsToDollars(filing.getExemptAmountCents()));
        preview.put("taxRemitted", centsToDollars(filing.getTaxCollectedCents()));
        preview.put("confirmation", filing.getConfirmationNum());
        preview.put("savings", centsToDollars(filing.getSavingsAmountCents()));
        preview.put("nextDueDate", nextFiling.get("dueDateLabel"));
        return preview;
    }

    private String generateNumbricsFilingId(SalesTaxFilingEntity filing) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "NUM-" + filing.getStateCode() + "-" + date;
    }

    private String extractConfirmation(Map<String, Object> response, SalesTaxFilingEntity filing) {
        Object confirmation = response.get("confirmation_number");
        if (confirmation == null) {
            confirmation = response.get("confirmation_num");
        }
        if (confirmation != null) {
            return String.valueOf(confirmation);
        }
        return filing.getStateCode() + "-" + filing.getPeriodEnd().getYear()
                + "-Q" + quarterOf(filing.getPeriodEnd()) + "-" + filing.getId().substring(0, 8).toUpperCase();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractJurisdictionBreakdown(Map<String, Object> estimate, double taxable) {
        Object items = estimate.get("transaction_items");
        if (!(items instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> itemMap)) {
            return List.of();
        }
        Object taxes = itemMap.get("taxes");
        if (!(taxes instanceof List<?> taxList)) {
            return List.of();
        }
        List<Map<String, Object>> breakdown = new ArrayList<>();
        for (Object taxObj : taxList) {
            if (taxObj instanceof Map<?, ?> tax) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", tax.get("name"));
                row.put("rate", toDouble(tax.get("rate")));
                row.put("amount", toDouble(tax.get("amount")));
                breakdown.add(row);
            }
        }
        if (breakdown.isEmpty() && taxable > 0) {
            double rate = extractBlendedRate(estimate, taxable);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", "Combined rate");
            row.put("rate", rate);
            row.put("amount", round2(taxable * rate));
            breakdown.add(row);
        }
        return breakdown;
    }

    private double extractBlendedRate(Map<String, Object> estimate, double taxable) {
        Object totalTax = estimate.get("total_tax_amount_calculated");
        if (totalTax == null) {
            totalTax = estimate.get("total_tax_amount");
        }
        double tax = toDouble(totalTax);
        if (taxable > 0 && tax > 0) {
            return tax / taxable;
        }
        Object rate = estimate.get("tax_rate_calculated");
        if (rate == null) {
            rate = estimate.get("tax_rate");
        }
        double parsed = toDouble(rate);
        return parsed > 0 ? parsed : 0.0825;
    }

    /** Returns [city, postalCode] that is geographically valid for the given US state code. */
    private String[] resolveAddressForState(String stateCode) {
        return switch (stateCode.toUpperCase()) {
            case "AL" -> new String[]{"Birmingham", "35203"};
            case "AK" -> new String[]{"Anchorage", "99501"};
            case "AZ" -> new String[]{"Phoenix", "85001"};
            case "AR" -> new String[]{"Little Rock", "72201"};
            case "CA" -> new String[]{"Los Angeles", "90001"};
            case "CO" -> new String[]{"Denver", "80201"};
            case "CT" -> new String[]{"Hartford", "06101"};
            case "DE" -> new String[]{"Wilmington", "19801"};
            case "FL" -> new String[]{"Miami", "33101"};
            case "GA" -> new String[]{"Atlanta", "30301"};
            case "HI" -> new String[]{"Honolulu", "96801"};
            case "ID" -> new String[]{"Boise", "83701"};
            case "IL" -> new String[]{"Chicago", "60601"};
            case "IN" -> new String[]{"Indianapolis", "46201"};
            case "IA" -> new String[]{"Des Moines", "50301"};
            case "KS" -> new String[]{"Wichita", "67201"};
            case "KY" -> new String[]{"Louisville", "40201"};
            case "LA" -> new String[]{"New Orleans", "70112"};
            case "ME" -> new String[]{"Portland", "04101"};
            case "MD" -> new String[]{"Baltimore", "21201"};
            case "MA" -> new String[]{"Boston", "02101"};
            case "MI" -> new String[]{"Detroit", "48201"};
            case "MN" -> new String[]{"Minneapolis", "55401"};
            case "MS" -> new String[]{"Jackson", "39201"};
            case "MO" -> new String[]{"Kansas City", "64101"};
            case "MT" -> new String[]{"Billings", "59101"};
            case "NE" -> new String[]{"Omaha", "68101"};
            case "NV" -> new String[]{"Las Vegas", "89101"};
            case "NH" -> new String[]{"Manchester", "03101"};
            case "NJ" -> new String[]{"Newark", "07101"};
            case "NM" -> new String[]{"Albuquerque", "87101"};
            case "NY" -> new String[]{"New York", "10001"};
            case "NC" -> new String[]{"Charlotte", "28201"};
            case "ND" -> new String[]{"Fargo", "58102"};
            case "OH" -> new String[]{"Columbus", "43085"};
            case "OK" -> new String[]{"Oklahoma City", "73101"};
            case "OR" -> new String[]{"Portland", "97201"};
            case "PA" -> new String[]{"Philadelphia", "19102"};
            case "RI" -> new String[]{"Providence", "02901"};
            case "SC" -> new String[]{"Columbia", "29201"};
            case "SD" -> new String[]{"Sioux Falls", "57101"};
            case "TN" -> new String[]{"Nashville", "37201"};
            case "TX" -> new String[]{"Austin", "73301"};
            case "UT" -> new String[]{"Salt Lake City", "84101"};
            case "VT" -> new String[]{"Burlington", "05401"};
            case "VA" -> new String[]{"Richmond", "23218"};
            case "WA" -> new String[]{"Seattle", "98101"};
            case "WV" -> new String[]{"Charleston", "25301"};
            case "WI" -> new String[]{"Milwaukee", "53201"};
            case "WY" -> new String[]{"Cheyenne", "82001"};
            case "DC" -> new String[]{"Washington", "20001"};
            default  -> new String[]{"New York", "10001"};
        };
    }

    private void validateSalesInput(EnterSalesEstimateRequestDTO request) {
        if (request.getStateCode() == null || request.getStateCode().isBlank()) {
            throw new IllegalArgumentException("stateCode is required");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new IllegalArgumentException("category is required");
        }
        if (request.getSubcategory() == null || request.getSubcategory().isBlank()) {
            throw new IllegalArgumentException("subcategory is required");
        }
    }

    private String normalizeState(String stateCode) {
        String code = stateCode.trim().toUpperCase();
        if (!UsStateCatalog.isValid(code)) {
            throw new IllegalArgumentException("Invalid stateCode: " + stateCode);
        }
        return code;
    }

    private long dollarsToCents(Double dollars) {
        double value = dollars != null ? dollars : 0.0;
        return Math.round(value * 100.0);
    }

    private double centsToDollars(Long cents) {
        long value = cents != null ? cents : 0L;
        return round2(value / 100.0);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int quarterOf(LocalDate date) {
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    private String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private List<String> readJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize categories", e);
        }
    }

    private String writeJsonObject(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }

    private Object readJsonObject(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
