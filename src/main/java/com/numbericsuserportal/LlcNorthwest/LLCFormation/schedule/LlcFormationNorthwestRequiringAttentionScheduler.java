package com.numbericsuserportal.LlcNorthwest.LLCFormation.schedule;

import com.fasterxml.jackson.databind.JsonNode;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Step 7 — polls Corporate Tools every 12 hours for formations awaiting attention.
 */
@Component
public class LlcFormationNorthwestRequiringAttentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(LlcFormationNorthwestRequiringAttentionScheduler.class);

    @Autowired
    private LlcFormationRepository formationRepository;

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Scheduled(fixedRateString = "${llc.northwest.requiring-attention-poll-ms:43200000}")
    public void pollRequiringAttention() {
        List<String> statuses = List.of("SUBMITTED", "PROCESSING");
        formationRepository.findByStatusIn(statuses).stream()
                .filter(f -> f.getCompanyId() != null && !f.getCompanyId().isBlank())
                .forEach(f -> {
                    try {
                        UUID cid = UUID.fromString(f.getCompanyId().trim());
                        JsonNode n = corporateToolsApiService.getOrderItemsRequiringAttention(cid);
                        log.info("Northwest requiring-attention for formation id={} companyId={} response={}",
                                f.getId(), cid, n);
                    } catch (Exception e) {
                        log.warn("requiring-attention poll failed for formation {}", f.getId(), e);
                    }
                });
    }
}
