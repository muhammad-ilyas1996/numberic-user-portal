package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateNorthwestShoppingCartJsonRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep1StateRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep2NameRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep3DetailsRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationMember;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class LlcFormationService {

    @Autowired
    private LlcFormationRepository repo;

    @Autowired
    private LlcFormationPricingService pricingService;

    @Autowired
    private LlcFormationMemberService memberService;

    @Autowired
    private LlcFormationStateCatalogService stateCatalogService;

    @Transactional
    public LlcFormation createDraft(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        LlcFormation f = new LlcFormation();
        f.setUserId(user.getUserId());
        f.setStatus("DRAFT");
        f.setEntityType("LLC");
        // reasonable defaults matching HTML demo
        f.setJurisdiction("TX");
        f.setOwnershipType("single");
        f.setOperatesInFormationState(true);
        f.setAddonEin(true);
        f.setAddonScorp(false);
        f.setFilingSpeed("standard");
        return repo.save(f);
    }

    @Transactional(readOnly = true)
    public LlcFormation getForUserOrThrow(Long formationId, User user) {
        if (formationId == null) throw new IllegalArgumentException("formationId is required");
        if (user == null || user.getUserId() == null) throw new IllegalArgumentException("User is required");
        Optional<LlcFormation> found = repo.findByIdAndUserId(formationId, user.getUserId());
        return found.orElseThrow(() -> new IllegalArgumentException("Formation not found"));
    }

    @Transactional
    public LlcFormation updateStep1(Long formationId, User user, UpdateStep1StateRequestDTO req) {
        LlcFormation f = getForUserOrThrow(formationId, user);
        if (req.getJurisdiction() != null) {
            String jurisdiction = req.getJurisdiction().trim().toUpperCase();
            if (stateCatalogService.hasActiveCatalog()) {
                stateCatalogService.validateStateCode(jurisdiction);
            }
            f.setJurisdiction(jurisdiction);
        }
        if (req.getOperatesInFormationState() != null) f.setOperatesInFormationState(req.getOperatesInFormationState());
        if (req.getOwnershipType() != null) f.setOwnershipType(req.getOwnershipType().trim().toLowerCase());

        if (Boolean.FALSE.equals(f.getOperatesInFormationState())) {
            if (isBlank(req.getOperatingBusinessStreet())
                    || isBlank(req.getOperatingBusinessCity())
                    || isBlank(req.getOperatingBusinessState())
                    || isBlank(req.getOperatingBusinessZip())) {
                throw new IllegalArgumentException("Operating business address is required when you operate elsewhere");
            }
            f.setOperatingBusinessStreet(req.getOperatingBusinessStreet());
            f.setOperatingBusinessCity(req.getOperatingBusinessCity());
            f.setOperatingBusinessState(req.getOperatingBusinessState());
            f.setOperatingBusinessZip(req.getOperatingBusinessZip());
        } else {
            f.setOperatingBusinessStreet(null);
            f.setOperatingBusinessCity(null);
            f.setOperatingBusinessState(null);
            f.setOperatingBusinessZip(null);
        }

        // refresh pricing snapshot
        pricingService.applySnapshotToFormation(f, pricingService.calculate(f));
        return repo.save(f);
    }

    @Transactional
    public LlcFormation updateStep2(Long formationId, User user, UpdateStep2NameRequestDTO req) {
        LlcFormation f = getForUserOrThrow(formationId, user);
        if (req.getLlcName() != null) f.setLlcName(req.getLlcName().trim());
        if (req.getAltName() != null) f.setAltName(req.getAltName().trim());
        if (req.getIndustry() != null) f.setIndustry(req.getIndustry().trim());
        if (req.getBusinessPurpose() != null) f.setBusinessPurpose(req.getBusinessPurpose().trim());
        return repo.save(f);
    }

    @Transactional
    public LlcFormation updateStep3(Long formationId, User user, UpdateStep3DetailsRequestDTO req) {
        LlcFormation f = getForUserOrThrow(formationId, user);
        if (req.getOwnerFirstName() != null) f.setOwnerFirstName(req.getOwnerFirstName().trim());
        if (req.getOwnerLastName() != null) f.setOwnerLastName(req.getOwnerLastName().trim());
        if (req.getOwnerDob() != null && !req.getOwnerDob().trim().isEmpty()) {
            f.setOwnerDob(LocalDate.parse(req.getOwnerDob().trim()));
        }
        if (req.getOwnerSsnLast4() != null) f.setOwnerSsnLast4Enc(req.getOwnerSsnLast4().trim());
        if (req.getOwnershipPct() != null) f.setOwnershipPct(req.getOwnershipPct());
        if (req.getOwnerTitle() != null) f.setOwnerTitle(req.getOwnerTitle().trim());

        if (req.getManagementType() != null) f.setManagementType(req.getManagementType().trim().toLowerCase());

        if (req.getAddressSameAsHome() != null) f.setAddressSameAsHome(req.getAddressSameAsHome());
        if (req.getBusinessStreet() != null) f.setBusinessStreet(req.getBusinessStreet().trim());
        if (req.getBusinessCity() != null) f.setBusinessCity(req.getBusinessCity().trim());
        if (req.getBusinessState() != null) f.setBusinessState(req.getBusinessState().trim().toUpperCase());
        if (req.getBusinessZip() != null) f.setBusinessZip(req.getBusinessZip().trim());

        if (req.getFilingSpeed() != null) f.setFilingSpeed(req.getFilingSpeed().trim().toLowerCase());
        if (req.getAddonEin() != null) f.setAddonEin(req.getAddonEin());
        if (req.getAddonScorp() != null) f.setAddonScorp(req.getAddonScorp());

        if (req.getMembers() != null && !req.getMembers().isEmpty()) {
            java.util.List<LlcFormationMember> savedMembers = memberService.replaceMembers(formationId, f, req.getMembers());
            // Keep backward-compatible owner snapshot in llc_formation from primary member.
            LlcFormationMember primary = savedMembers.get(0);
            f.setOwnerFirstName(primary.getFirstName());
            f.setOwnerLastName(primary.getLastName());
            f.setOwnerDob(primary.getDob());
            f.setOwnerSsnLast4Enc(primary.getSsnLast4Enc());
            f.setOwnershipPct(primary.getOwnershipPct());
            f.setOwnerTitle(primary.getTitle());
        }

        pricingService.applySnapshotToFormation(f, pricingService.calculate(f));
        return repo.save(f);
    }

    @Transactional
    public LlcFormation updateNorthwestShoppingCartJson(Long formationId, User user, UpdateNorthwestShoppingCartJsonRequestDTO req) {
        LlcFormation f = getForUserOrThrow(formationId, user);
        if (req.getNorthwestShoppingCartJson() != null) {
            f.setNorthwestShoppingCartJson(req.getNorthwestShoppingCartJson().trim());
        }
        return repo.save(f);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

