package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.CalculatePaymentResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRate;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LlcFormationPricingService {

    @Autowired
    private LlcFormationRateRepository rateRepository;

    @Autowired
    private LlcFormationRateService rateService;

    public CalculatePaymentResponseDTO calculate(LlcFormation formation) {
        rateService.seedDefaultsIfEmpty();

        String state = formation.getJurisdiction() != null ? formation.getJurisdiction().trim().toUpperCase() : "TX";
        int numbricsFee = resolveRate("NUMBRICS_FEE", null, null, 9900);
        int einFee = resolveRate("EIN_FEE", null, null, 4900);
        int scorpFee = resolveRate("SCORP_FEE", null, null, 14900);
        int stateFee = resolveRate("STATE_FEE", state, null, 0);

        String speed = formation.getFilingSpeed() != null ? formation.getFilingSpeed().trim().toLowerCase() : "standard";
        int speedFee = resolveRate("SPEED_FEE", state, speed, 0);

        boolean ein = Boolean.TRUE.equals(formation.getAddonEin());
        boolean scorp = Boolean.TRUE.equals(formation.getAddonScorp());

        List<CalculatePaymentResponseDTO.LineItem> items = new ArrayList<>();
        items.add(new CalculatePaymentResponseDTO.LineItem("Numbrics formation service", numbricsFee));
        items.add(new CalculatePaymentResponseDTO.LineItem(state + " state filing fee", stateFee));
        if (ein) items.add(new CalculatePaymentResponseDTO.LineItem("EIN application", einFee));
        if (scorp) items.add(new CalculatePaymentResponseDTO.LineItem("S-Corp election", scorpFee));
        if (speedFee > 0) items.add(new CalculatePaymentResponseDTO.LineItem("Filing speed (" + speed + ")", speedFee));

        int total = items.stream().mapToInt(CalculatePaymentResponseDTO.LineItem::getCents).sum();
        return new CalculatePaymentResponseDTO(total, items);
    }

    public void applySnapshotToFormation(LlcFormation formation, CalculatePaymentResponseDTO calc) {
        rateService.seedDefaultsIfEmpty();

        formation.setNumbricsFeeCents(resolveRate("NUMBRICS_FEE", null, null, 9900));
        String state = formation.getJurisdiction() != null ? formation.getJurisdiction().trim().toUpperCase() : "TX";
        formation.setStateFeeCents(resolveRate("STATE_FEE", state, null, 0));
        String speed = formation.getFilingSpeed() != null ? formation.getFilingSpeed().trim().toLowerCase() : "standard";
        formation.setSpeedFeeCents(resolveRate("SPEED_FEE", state, speed, 0));
        formation.setEinFeeCents(Boolean.TRUE.equals(formation.getAddonEin()) ? resolveRate("EIN_FEE", null, null, 4900) : 0);
        formation.setScorpFeeCents(Boolean.TRUE.equals(formation.getAddonScorp()) ? resolveRate("SCORP_FEE", null, null, 14900) : 0);
        formation.setTotalCents(calc.getTotalCents());
    }

    private int resolveRate(String rateType, String stateCode, String speedCode, int defaultValue) {
        Optional<LlcFormationRate> rate = rateRepository.findByRateTypeAndStateCodeAndSpeedCode(rateType, stateCode, speedCode);
        return rate.filter(r -> Boolean.TRUE.equals(r.getActive()))
                .map(LlcFormationRate::getAmountCents)
                .orElse(defaultValue);
    }
}

