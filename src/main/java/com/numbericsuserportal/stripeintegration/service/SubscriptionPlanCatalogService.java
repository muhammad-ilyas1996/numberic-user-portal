package com.numbericsuserportal.stripeintegration.service;

import com.numbericsuserportal.stripeintegration.dto.SubscriptionPlanCatalogDto;
import com.numbericsuserportal.stripeintegration.dto.UpdateSubscriptionPlanRequest;
import com.numbericsuserportal.stripeintegration.entity.SubscriptionPlanCatalogEntity;
import com.numbericsuserportal.stripeintegration.repo.SubscriptionPlanCatalogRepository;
import com.numbericsuserportal.usermanagement.domain.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SubscriptionPlanCatalogService {

    @Autowired
    private SubscriptionPlanCatalogRepository catalogRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Transactional
    public void seedDefaultsIfEmpty() {
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY subscription_plan VARCHAR(50)");
        } catch (Exception e) {
            log.warn("Could not alter users table: {}", e.getMessage());
        }
        try {
            if (catalogRepository.count() > 0) {
                return;
            }
            log.info("Seeding default subscription plan catalog...");
            catalogRepository.save(plan("SOLOPRENEUR", "Solopreneur (founder)",
                    "Freelancers, founders (50K base)", "Freelancers / founders",
                    1900L, 1000L, 7, "NUMBRICS_BUSINESS_OWNER", false, 1,
                    "[\"One-click scans\",\"Basic IRS letter parsing\",\"Growth tips\",\"Multilingual\"]"));
            catalogRepository.save(plan("BUSINESS_OWNER", "Business Owner",
                    "SMB — e-comm & real estate owners", "SMB owners",
                    7900L, 1000L, 7, "NUMBRICS_BUSINESS_OWNER", false, 2,
                    "[\"Everything in Solopreneur\",\"Team collab\",\"Business Formation / Sales Tax\",\"Advanced audit flags\",\"Invoicing\"]"));
            catalogRepository.save(plan("ACCOUNTANT_PRO", "Accountant Pro",
                    "For accountants managing multiple clients", "Accountants / firms",
                    19900L, 1000L, 7, "NUMBRICS_ACCOUNTANT_PRO", true, 3,
                    "[\"Everything in Business Owner\",\"Bulk uploads\",\"Memo drafting\",\"Client intelligence\",\"SOC 2 priority\",\"White-labeling\"]"));
        } catch (Exception e) {
            log.warn("Could not seed subscription plan catalog (table may be missing): {}", e.getMessage());
        }
    }

    public List<SubscriptionPlanCatalogDto> listPublicPlans() {
        return catalogRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toDto)
                .toList();
    }

    public List<SubscriptionPlanCatalogDto> listAllPlansAdmin() {
        return catalogRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDto)
                .toList();
    }

    public SubscriptionPlanCatalogEntity requireByCode(String planCode) {
        return catalogRepository.findByPlanCodeIgnoreCase(planCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown plan: " + planCode));
    }

    public SubscriptionPlanCatalogEntity requireActiveByCode(String planCode) {
        SubscriptionPlanCatalogEntity plan = requireByCode(planCode);
        if (!Boolean.TRUE.equals(plan.getActive())) {
            throw new IllegalArgumentException("Plan is inactive: " + planCode);
        }
        return plan;
    }

    @Transactional
    public SubscriptionPlanCatalogDto updatePlan(Long id, UpdateSubscriptionPlanRequest req) {
        SubscriptionPlanCatalogEntity plan = catalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + id));
        applyUpdate(plan, req);
        return toDto(catalogRepository.save(plan));
    }

    @Transactional
    public SubscriptionPlanCatalogDto updatePlanByCode(String planCode, UpdateSubscriptionPlanRequest req) {
        SubscriptionPlanCatalogEntity plan = requireByCode(planCode);
        applyUpdate(plan, req);
        return toDto(catalogRepository.save(plan));
    }

    @Transactional
    public SubscriptionPlanCatalogDto setActive(Long id, boolean active) {
        SubscriptionPlanCatalogEntity plan = catalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + id));
        plan.setActive(active);
        return toDto(catalogRepository.save(plan));
    }

    public long resolveChargeCents(SubscriptionPlanCatalogEntity plan, boolean hybridAddOn, int seats) {
        long base = plan.getAmountCents() != null ? plan.getAmountCents() : 0L;
        if (hybridAddOn && plan.getHybridAddonCents() != null) {
            base += plan.getHybridAddonCents();
        }
        int seatCount = Math.max(1, seats);
        if (Boolean.TRUE.equals(plan.getPerSeat())) {
            return base * seatCount;
        }
        return base;
    }

    public User.SubscriptionPlan toEnum(String planCode) {
        return User.SubscriptionPlan.fromCode(planCode);
    }

    private void applyUpdate(SubscriptionPlanCatalogEntity plan, UpdateSubscriptionPlanRequest req) {
        if (req == null) {
            return;
        }
        if (req.getDisplayName() != null && !req.getDisplayName().isBlank()) {
            plan.setDisplayName(req.getDisplayName().trim());
        }
        if (req.getDescription() != null) {
            plan.setDescription(req.getDescription());
        }
        if (req.getAudience() != null) {
            plan.setAudience(req.getAudience());
        }
        if (req.getAmountCents() != null) {
            plan.setAmountCents(req.getAmountCents());
        } else if (req.getAmount() != null) {
            plan.setAmountCents(Math.round(req.getAmount() * 100));
        }
        if (req.getHybridAddonCents() != null) {
            plan.setHybridAddonCents(req.getHybridAddonCents());
        } else if (req.getHybridAddonAmount() != null) {
            plan.setHybridAddonCents(Math.round(req.getHybridAddonAmount() * 100));
        }
        if (req.getTrialDays() != null) {
            if (req.getTrialDays() < 0 || req.getTrialDays() > 365) {
                throw new IllegalArgumentException("trialDays must be between 0 and 365");
            }
            plan.setTrialDays(req.getTrialDays());
        }
        if (req.getDefaultRoleCode() != null && !req.getDefaultRoleCode().isBlank()) {
            plan.setDefaultRoleCode(req.getDefaultRoleCode().trim());
        }
        if (req.getPerSeat() != null) {
            plan.setPerSeat(req.getPerSeat());
        }
        if (req.getSortOrder() != null) {
            plan.setSortOrder(req.getSortOrder());
        }
        if (req.getActive() != null) {
            plan.setActive(req.getActive());
        }
        if (req.getFeaturesJson() != null) {
            plan.setFeaturesJson(req.getFeaturesJson());
        }
    }

    private SubscriptionPlanCatalogDto toDto(SubscriptionPlanCatalogEntity e) {
        SubscriptionPlanCatalogDto dto = new SubscriptionPlanCatalogDto();
        dto.setId(e.getId());
        dto.setPlanCode(e.getPlanCode());
        dto.setDisplayName(e.getDisplayName());
        dto.setDescription(e.getDescription());
        dto.setAudience(e.getAudience());
        dto.setAmountCents(e.getAmountCents());
        dto.setAmount(e.amountDollars());
        dto.setHybridAddonCents(e.getHybridAddonCents());
        dto.setHybridAddonAmount(e.hybridAddonDollars());
        dto.setTrialDays(e.getTrialDays());
        dto.setDefaultRoleCode(e.getDefaultRoleCode());
        dto.setPerSeat(e.getPerSeat());
        dto.setSortOrder(e.getSortOrder());
        dto.setActive(e.getActive());
        dto.setFeaturesJson(e.getFeaturesJson());
        return dto;
    }

    private static SubscriptionPlanCatalogEntity plan(
            String code, String name, String description, String audience,
            long amountCents, long hybridCents, int trialDays, String role,
            boolean perSeat, int sort, String featuresJson) {
        SubscriptionPlanCatalogEntity e = new SubscriptionPlanCatalogEntity();
        e.setPlanCode(code);
        e.setDisplayName(name);
        e.setDescription(description);
        e.setAudience(audience);
        e.setAmountCents(amountCents);
        e.setHybridAddonCents(hybridCents);
        e.setTrialDays(trialDays);
        e.setDefaultRoleCode(role);
        e.setPerSeat(perSeat);
        e.setSortOrder(sort);
        e.setActive(true);
        e.setFeaturesJson(featuresJson);
        return e;
    }
}
