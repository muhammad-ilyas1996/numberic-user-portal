package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateRateRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRate;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LlcFormationRateService {

    @Autowired
    private LlcFormationRateRepository rateRepository;

    @Transactional(readOnly = true)
    public List<LlcFormationRate> getAllRates() {
        return rateRepository.findAll();
    }

    @Transactional
    public LlcFormationRate upsert(UpdateRateRequestDTO req) {
        if (req.getRateType() == null || req.getRateType().trim().isEmpty()) {
            throw new IllegalArgumentException("rateType is required");
        }
        if (req.getAmountCents() == null || req.getAmountCents() < 0) {
            throw new IllegalArgumentException("amountCents must be >= 0");
        }

        String rateType = req.getRateType().trim().toUpperCase();
        String stateCode = req.getStateCode() == null ? null : req.getStateCode().trim().toUpperCase();
        String speedCode = req.getSpeedCode() == null ? null : req.getSpeedCode().trim().toLowerCase();

        if ("STATE_FEE".equals(rateType) && (stateCode == null || stateCode.isEmpty())) {
            throw new IllegalArgumentException("stateCode is required for STATE_FEE");
        }
        if ("SPEED_FEE".equals(rateType)) {
            if (stateCode == null || stateCode.isEmpty()) {
                throw new IllegalArgumentException("stateCode is required for SPEED_FEE");
            }
            if (speedCode == null || speedCode.isEmpty()) {
                throw new IllegalArgumentException("speedCode is required for SPEED_FEE");
            }
        }

        LlcFormationRate row = rateRepository
                .findByRateTypeAndStateCodeAndSpeedCode(rateType, stateCode, speedCode)
                .orElseGet(LlcFormationRate::new);

        row.setRateType(rateType);
        row.setStateCode(stateCode);
        row.setSpeedCode(speedCode);
        row.setAmountCents(req.getAmountCents());
        if (req.getActive() != null) {
            row.setActive(req.getActive());
        }

        return rateRepository.save(row);
    }

    @Transactional
    public void upsertStateFee(String stateCode, int amountCents) {
        UpdateRateRequestDTO req = new UpdateRateRequestDTO();
        req.setRateType("STATE_FEE");
        req.setStateCode(stateCode);
        req.setAmountCents(amountCents);
        req.setActive(true);
        upsert(req);
    }

    @Transactional
    public void ensureGlobalFormationRates() {
        upsertGlobal("NUMBRICS_FEE", LlcFormationStateCatalogService.DEFAULT_NUMBRICS_SERVICE_FEE_CENTS);
        upsertGlobal("NW_RA_YR1", LlcFormationStateCatalogService.DEFAULT_NW_RA_YR1_PASS_THROUGH_CENTS);
        upsertGlobal("EIN_FEE", 4900);
        upsertGlobal("SCORP_FEE", 14900);
    }

    @Transactional(readOnly = true)
    public int resolveGlobalRateCents(String rateType, int defaultValue) {
        return resolveRate(rateType, null, null, defaultValue);
    }

    @Transactional(readOnly = true)
    public int resolveStateFeeCents(String stateCode, int defaultValue) {
        return resolveRate("STATE_FEE", stateCode, null, defaultValue);
    }

    @Transactional
    public void seedDefaultsIfEmpty() {
        if (rateRepository.count() > 0) {
            ensureGlobalFormationRates();
            return;
        }

        ensureGlobalFormationRates();

        upsertRow("SPEED_FEE", "TX", "standard", 0);
        upsertRow("SPEED_FEE", "TX", "expedited", 20000);
        upsertRow("SPEED_FEE", "TX", "sameday", 40000);
    }

    private void upsertGlobal(String rateType, int amountCents) {
        Optional<LlcFormationRate> existing = rateRepository.findByRateTypeAndStateCodeAndSpeedCode(rateType, null, null);
        if (existing.isPresent()) {
            LlcFormationRate row = existing.get();
            row.setAmountCents(amountCents);
            row.setActive(true);
            rateRepository.save(row);
        } else {
            upsertRow(rateType, null, null, amountCents);
        }
    }

    private void upsertRow(String rateType, String stateCode, String speedCode, int amount) {
        LlcFormationRate r = new LlcFormationRate();
        r.setRateType(rateType);
        r.setStateCode(stateCode);
        r.setSpeedCode(speedCode);
        r.setAmountCents(amount);
        r.setActive(true);
        rateRepository.save(r);
    }

    private int resolveRate(String rateType, String stateCode, String speedCode, int defaultValue) {
        Optional<LlcFormationRate> rate = rateRepository.findByRateTypeAndStateCodeAndSpeedCode(rateType, stateCode, speedCode);
        return rate.filter(r -> Boolean.TRUE.equals(r.getActive()))
                .map(LlcFormationRate::getAmountCents)
                .orElse(defaultValue);
    }
}
