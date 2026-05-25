package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.*;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationStateCatalog;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationStateCatalogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LlcFormationStateCatalogService {

    public static final int DEFAULT_NW_SERVICE_FEE_CENTS = 3900;
    public static final int DEFAULT_NW_RA_RENEWAL_CENTS = 12500;
    public static final int DEFAULT_NUMBRICS_SERVICE_FEE_CENTS = 9900;
    public static final int DEFAULT_NW_RA_YR1_PASS_THROUGH_CENTS = 4900;

    @Autowired
    private LlcFormationStateCatalogRepository catalogRepository;

    @Autowired
    private LlcFormationRateService rateService;

    @Transactional(readOnly = true)
    public StatesCatalogResponseDTO getActiveCatalog() {
        List<LlcFormationStateCatalog> rows = catalogRepository.findAllByActiveTrueOrderByStateNameAsc();
        int numbricsFee = rateService.resolveGlobalRateCents("NUMBRICS_FEE", DEFAULT_NUMBRICS_SERVICE_FEE_CENTS);
        int nwRaYr1 = rateService.resolveGlobalRateCents("NW_RA_YR1", DEFAULT_NW_RA_YR1_PASS_THROUGH_CENTS);

        List<StateCatalogItemDTO> states = new ArrayList<>();
        for (LlcFormationStateCatalog row : rows) {
            states.add(toItemDto(row, numbricsFee, nwRaYr1));
        }

        StateCatalogMetaDTO meta = new StateCatalogMetaDTO(
                numbricsFee,
                nwRaYr1,
                DEFAULT_NW_RA_RENEWAL_CENTS,
                DEFAULT_NW_SERVICE_FEE_CENTS,
                "NW State Filing Fees 2026",
                states.size()
        );
        return new StatesCatalogResponseDTO(states, meta);
    }

    @Transactional(readOnly = true)
    public Optional<StateCatalogItemDTO> getByStateCode(String stateCode) {
        if (stateCode == null || stateCode.isBlank()) {
            return Optional.empty();
        }
        return catalogRepository.findByStateCodeIgnoreCase(stateCode.trim())
                .filter(r -> Boolean.TRUE.equals(r.getActive()))
                .map(r -> toItemDto(r,
                        rateService.resolveGlobalRateCents("NUMBRICS_FEE", DEFAULT_NUMBRICS_SERVICE_FEE_CENTS),
                        rateService.resolveGlobalRateCents("NW_RA_YR1", DEFAULT_NW_RA_YR1_PASS_THROUGH_CENTS)));
    }

    @Transactional(readOnly = true)
    public int resolveStateFilingFeeCents(String stateCode) {
        if (stateCode == null || stateCode.isBlank()) {
            return 0;
        }
        return catalogRepository.findByStateCodeIgnoreCase(stateCode.trim())
                .filter(r -> Boolean.TRUE.equals(r.getActive()))
                .map(LlcFormationStateCatalog::getFilingFeeCents)
                .orElseGet(() -> rateService.resolveStateFeeCents(stateCode, 0));
    }

    @Transactional(readOnly = true)
    public String resolveFullStateName(String stateCode) {
        if (stateCode == null || stateCode.isBlank()) {
            return null;
        }
        return catalogRepository.findByStateCodeIgnoreCase(stateCode.trim())
                .map(LlcFormationStateCatalog::getStateName)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveCatalog() {
        return !catalogRepository.findAllByActiveTrueOrderByStateNameAsc().isEmpty();
    }

    @Transactional(readOnly = true)
    public void validateStateCode(String stateCode) {
        if (stateCode == null || stateCode.isBlank()) {
            throw new IllegalArgumentException("jurisdiction (state code) is required");
        }
        if (!catalogRepository.existsByStateCodeIgnoreCaseAndActiveTrue(stateCode.trim())) {
            throw new IllegalArgumentException("Unknown or inactive state code: " + stateCode);
        }
    }

    @Transactional
    public BulkSeedStateCatalogResponseDTO bulkSeed(BulkSeedStateCatalogRequestDTO req) {
        if (req.getStates() == null || req.getStates().isEmpty()) {
            throw new IllegalArgumentException("states array is required");
        }

        boolean syncRates = req.getSyncRates() == null || Boolean.TRUE.equals(req.getSyncRates());
        int upserted = 0;
        int ratesSynced = 0;

        for (StateCatalogSeedItemDTO item : req.getStates()) {
            LlcFormationStateCatalog row = upsertCatalogRow(item);
            upserted++;
            if (syncRates) {
                rateService.upsertStateFee(row.getStateCode(), row.getFilingFeeCents());
                ratesSynced++;
            }
        }

        boolean globalRatesSynced = false;
        if (syncRates) {
            rateService.ensureGlobalFormationRates();
            globalRatesSynced = true;
        }

        return new BulkSeedStateCatalogResponseDTO(upserted, ratesSynced, globalRatesSynced);
    }

    private LlcFormationStateCatalog upsertCatalogRow(StateCatalogSeedItemDTO item) {
        if (item.getAbbreviation() == null || item.getAbbreviation().isBlank()) {
            throw new IllegalArgumentException("abbreviation is required for each state");
        }
        if (item.getState() == null || item.getState().isBlank()) {
            throw new IllegalArgumentException("state name is required for each state");
        }
        if (item.getFilingFeeCents() == null || item.getFilingFeeCents() < 0) {
            throw new IllegalArgumentException("filingFeeCents must be >= 0 for " + item.getAbbreviation());
        }

        String code = item.getAbbreviation().trim().toUpperCase();
        LlcFormationStateCatalog row = catalogRepository.findByStateCodeIgnoreCase(code)
                .orElseGet(LlcFormationStateCatalog::new);

        row.setStateCode(code);
        row.setStateName(item.getState().trim());
        row.setFilingFeeCents(item.getFilingFeeCents());
        row.setAnnualReportFee(item.getAnnualReportFee());
        row.setProcessingTime(item.getProcessingTime());
        row.setSpeed(normalizeSpeed(item.getSpeed()));
        row.setNotes(emptyToNull(item.getNotes()));
        row.setNwServiceFeeCents(item.getNwServiceFeeCents() != null ? item.getNwServiceFeeCents() : DEFAULT_NW_SERVICE_FEE_CENTS);
        row.setNwRaYear1Cents(item.getNwRaYear1Cents() != null ? item.getNwRaYear1Cents() : 0);
        row.setNwRaRenewalCents(item.getNwRaRenewalCents() != null ? item.getNwRaRenewalCents() : DEFAULT_NW_RA_RENEWAL_CENTS);
        row.setActive(item.getActive() == null || item.getActive());

        return catalogRepository.save(row);
    }

    private StateCatalogItemDTO toItemDto(LlcFormationStateCatalog row, int numbricsFeeCents, int nwRaYr1PassThroughCents) {
        int nwService = row.getNwServiceFeeCents() != null ? row.getNwServiceFeeCents() : DEFAULT_NW_SERVICE_FEE_CENTS;
        int nwRaYr1 = row.getNwRaYear1Cents() != null ? row.getNwRaYear1Cents() : 0;
        int filing = row.getFilingFeeCents();
        int year1Nw = filing + nwService + nwRaYr1;
        int year1Numbrics = filing + numbricsFeeCents + nwRaYr1PassThroughCents;

        return new StateCatalogItemDTO(
                row.getStateName(),
                row.getStateCode(),
                filing,
                nwService,
                nwRaYr1,
                row.getNwRaRenewalCents(),
                year1Nw,
                year1Numbrics,
                row.getAnnualReportFee(),
                row.getProcessingTime(),
                row.getSpeed(),
                row.getNotes()
        );
    }

    private static String normalizeSpeed(String speed) {
        if (speed == null || speed.isBlank()) {
            return null;
        }
        return speed.trim().toLowerCase();
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
