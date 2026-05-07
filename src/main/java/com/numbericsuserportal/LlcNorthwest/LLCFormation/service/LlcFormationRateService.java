package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateRateRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRate;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public void seedDefaultsIfEmpty() {
        if (rateRepository.count() > 0) return;

        upsertRow("NUMBRICS_FEE", null, null, 9900);
        upsertRow("EIN_FEE", null, null, 4900);
        upsertRow("SCORP_FEE", null, null, 14900);

        upsertRow("STATE_FEE", "TX", null, 5000);
        upsertRow("STATE_FEE", "FL", null, 12500);
        upsertRow("STATE_FEE", "CA", null, 7000);
        upsertRow("STATE_FEE", "NY", null, 20000);
        upsertRow("STATE_FEE", "WY", null, 10000);
        upsertRow("STATE_FEE", "DE", null, 9000);
        upsertRow("STATE_FEE", "WA", null, 20000);
        upsertRow("STATE_FEE", "CO", null, 5000);
        upsertRow("STATE_FEE", "GA", null, 10000);
        upsertRow("STATE_FEE", "IL", null, 15000);
        upsertRow("STATE_FEE", "NV", null, 7500);
        upsertRow("STATE_FEE", "AZ", null, 5000);

        upsertRow("SPEED_FEE", "TX", "standard", 0);
        upsertRow("SPEED_FEE", "TX", "expedited", 20000);
        upsertRow("SPEED_FEE", "TX", "sameday", 40000);
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
}

