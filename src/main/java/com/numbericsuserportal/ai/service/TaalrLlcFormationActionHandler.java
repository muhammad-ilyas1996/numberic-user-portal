package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.NorthwestPrepareResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep1StateRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep2NameRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep3DetailsRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.corporatetools.NameCheckRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.corporatetools.NameCheckResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationNorthwestIntegrationService;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrLlcDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class TaalrLlcFormationActionHandler {

    @Autowired
    private LlcFormationService llcFormationService;

    @Autowired
    private LlcFormationRepository llcFormationRepository;

    @Autowired
    private LlcFormationNorthwestIntegrationService northwestIntegrationService;

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Autowired
    private TaalrActionSessionService sessionService;

    public TaalrActionResult handleStartOrContinue(TaalrActionRequest request, User user,
            TaalrIntentParseResult parsed, TaalrActionSessionEntity existingSession, String rawMessage) {
        TaalrSessionContext ctx = existingSession != null
                ? sessionService.loadContext(existingSession)
                : new TaalrSessionContext();
        if (ctx.getLlcDraft() == null) {
            ctx.setLlcDraft(new TaalrLlcDraft());
        }
        mergeDraft(ctx.getLlcDraft(), parsed.getLlc());
        mergeFromText(ctx.getLlcDraft(), rawMessage);

        String missing = missingField(ctx.getLlcDraft());
        if (missing != null) {
            if (existingSession != null) {
                sessionService.updateSession(existingSession, TaalrPendingAction.LLC_DRAFT, ctx);
            } else {
                sessionService.saveSession(request, TaalrPendingAction.LLC_DRAFT, ctx);
            }
            return TaalrActionResult.handled(questionFor(missing));
        }

        try {
            LlcFormation formation = ensureDraftPersisted(user, ctx.getLlcDraft());
            NameCheckResponseDTO nameCheck = runNameCheck(formation);
            NorthwestPrepareResponseDTO prepared = northwestIntegrationService.prepare(formation, user);
            sessionService.clearSession(request);
            return TaalrActionResult.handled(buildSuccessReply(formation, prepared, nameCheck));
        } catch (Exception e) {
            log.warn("LLC automation failed: {}", e.getMessage());
            if (existingSession != null) {
                sessionService.updateSession(existingSession, TaalrPendingAction.LLC_DRAFT, ctx);
            } else {
                sessionService.saveSession(request, TaalrPendingAction.LLC_DRAFT, ctx);
            }
            return TaalrActionResult.handled(
                    "LLC draft save ho gaya, lekin final prepare mein issue aya: " + safe(e.getMessage())
                            + ". Aap 'continue llc' bhej kar resume kar sakte hain.");
        }
    }

    public TaalrActionResult handleStatus(User user) {
        List<LlcFormation> rows = llcFormationService.getFormationsForUser(user);
        if (rows.isEmpty()) {
            return TaalrActionResult.handled("Aap ke paas koi LLC formation draft nahi hai. 'Start LLC formation' likhein.");
        }
        LlcFormation latest = rows.get(0);
        String filingStatus = latest.getFilingStatus() != null ? latest.getFilingStatus() : "not-submitted";
        return TaalrActionResult.handled(
                "Latest LLC formation #" + latest.getId()
                        + " | status: " + latest.getStatus()
                        + " | filing: " + filingStatus
                        + " | updated: " + formatUpdated(latest.getUpdatedAt()));
    }

    public TaalrActionResult cancel(TaalrActionRequest request) {
        sessionService.clearSession(request);
        return TaalrActionResult.handled("LLC draft session cancel kar di gayi.");
    }

    private LlcFormation ensureDraftPersisted(User user, TaalrLlcDraft draft) {
        LlcFormation formation;
        if (draft.getFormationId() != null) {
            Optional<LlcFormation> existing = llcFormationRepository.findByIdAndUserId(draft.getFormationId(), user.getUserId());
            formation = existing.orElseGet(() -> llcFormationService.createDraft(user));
        } else {
            formation = llcFormationService.createDraft(user);
        }

        UpdateStep1StateRequestDTO step1 = new UpdateStep1StateRequestDTO();
        step1.setJurisdiction(draft.getJurisdiction());
        step1.setOwnershipType("single");
        step1.setOperatesInFormationState(Boolean.TRUE);
        formation = llcFormationService.updateStep1(formation.getId(), user, step1);

        UpdateStep2NameRequestDTO step2 = new UpdateStep2NameRequestDTO();
        step2.setLlcName(draft.getLlcName());
        formation = llcFormationService.updateStep2(formation.getId(), user, step2);

        UpdateStep3DetailsRequestDTO step3 = new UpdateStep3DetailsRequestDTO();
        step3.setOwnerFirstName(draft.getOwnerFirstName());
        step3.setOwnerLastName(draft.getOwnerLastName());
        step3.setFilingSpeed(defaultValue(draft.getFilingSpeed(), "standard"));
        step3.setAddonEin(draft.getAddonEin() != null ? draft.getAddonEin() : Boolean.TRUE);
        step3.setManagementType("member");
        formation = llcFormationService.updateStep3(formation.getId(), user, step3);

        draft.setFormationId(formation.getId());
        return formation;
    }

    private NameCheckResponseDTO runNameCheck(LlcFormation formation) {
        try {
            NameCheckRequestDTO req = new NameCheckRequestDTO();
            req.setName(formation.getLlcName());
            req.setJurisdiction(formation.getJurisdiction());
            NameCheckResponseDTO res = corporateToolsApiService.nameCheck(req);

            String status = "UNCHECKED";
            if (res != null && res.getResult() != null && Boolean.TRUE.equals(res.getResult().getAvailable())) {
                status = "AVAILABLE";
            } else if (res != null && res.getResult() != null && Boolean.FALSE.equals(res.getResult().getAvailable())) {
                status = "TAKEN";
            }
            formation.setNameCheckStatus(status);
            formation.setNameCheckLastCheckedAt(OffsetDateTime.now());
            llcFormationRepository.save(formation);
            return res;
        } catch (Exception e) {
            formation.setNameCheckStatus("ERROR");
            formation.setNameCheckLastCheckedAt(OffsetDateTime.now());
            llcFormationRepository.save(formation);
            log.warn("LLC name check failed for formation {}: {}", formation.getId(), e.getMessage());
            return null;
        }
    }

    private static void mergeDraft(TaalrLlcDraft target, TaalrLlcDraft incoming) {
        if (incoming == null) {
            return;
        }
        if (incoming.getFormationId() != null) target.setFormationId(incoming.getFormationId());
        if (notBlank(incoming.getJurisdiction())) target.setJurisdiction(incoming.getJurisdiction().trim().toUpperCase(Locale.ROOT));
        if (notBlank(incoming.getLlcName())) target.setLlcName(incoming.getLlcName().trim());
        if (notBlank(incoming.getOwnerFirstName())) target.setOwnerFirstName(incoming.getOwnerFirstName().trim());
        if (notBlank(incoming.getOwnerLastName())) target.setOwnerLastName(incoming.getOwnerLastName().trim());
        if (notBlank(incoming.getFilingSpeed())) target.setFilingSpeed(incoming.getFilingSpeed().trim().toLowerCase(Locale.ROOT));
        if (incoming.getAddonEin() != null) target.setAddonEin(incoming.getAddonEin());
    }

    private static void mergeFromText(TaalrLlcDraft draft, String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return;
        }
        String msg = rawMessage.trim();
        if (!notBlank(draft.getJurisdiction())) {
            java.util.regex.Matcher state = java.util.regex.Pattern.compile("\\b([A-Z]{2})\\b").matcher(msg.toUpperCase(Locale.ROOT));
            if (state.find()) {
                draft.setJurisdiction(state.group(1));
            }
        }
        if (!notBlank(draft.getLlcName())) {
            java.util.regex.Matcher name = java.util.regex.Pattern.compile("(?i)(?:name|named)\\s+([A-Za-z0-9&'\\- ]{3,60})").matcher(msg);
            if (name.find()) {
                draft.setLlcName(name.group(1).trim());
            }
        }
        if (!notBlank(draft.getOwnerFirstName()) || !notBlank(draft.getOwnerLastName())) {
            java.util.regex.Matcher owner = java.util.regex.Pattern.compile("(?i)(?:owner|member)\\s+([A-Za-z]+)\\s+([A-Za-z]+)").matcher(msg);
            if (owner.find()) {
                draft.setOwnerFirstName(owner.group(1).trim());
                draft.setOwnerLastName(owner.group(2).trim());
            }
        }
    }

    private static String missingField(TaalrLlcDraft draft) {
        if (!notBlank(draft.getJurisdiction())) return "jurisdiction";
        if (!notBlank(draft.getLlcName())) return "llcName";
        if (!notBlank(draft.getOwnerFirstName()) || !notBlank(draft.getOwnerLastName())) return "ownerName";
        return null;
    }

    private static String questionFor(String field) {
        return switch (field) {
            case "jurisdiction" -> "LLC kis state mein form karni hai? (e.g., TX, CA, FL)";
            case "llcName" -> "Aapki LLC ka exact legal name kya hoga?";
            case "ownerName" -> "Primary owner ka first aur last name bhej dein (e.g., Ali Ahmed).";
            default -> "Please LLC draft details continue karein.";
        };
    }

    private static String buildSuccessReply(LlcFormation formation, NorthwestPrepareResponseDTO prepared,
            NameCheckResponseDTO nameCheck) {
        StringBuilder sb = new StringBuilder();
        sb.append("LLC draft created successfully. ");
        sb.append("Formation #").append(formation.getId());
        sb.append(" | status: ").append(formation.getStatus());
        sb.append(" | state: ").append(formation.getJurisdiction());
        if (formation.getTotalCents() != null) {
            sb.append(" | estimated total: $").append(String.format(Locale.US, "%.2f", formation.getTotalCents() / 100.0));
        }
        if (nameCheck != null && nameCheck.getResult() != null && nameCheck.getResult().getAvailable() != null) {
            sb.append(" | name check: ").append(Boolean.TRUE.equals(nameCheck.getResult().getAvailable()) ? "AVAILABLE" : "TAKEN");
        } else {
            sb.append(" | name check: pending");
        }
        if (prepared != null && prepared.getCompanyId() != null) {
            sb.append("\nNorthwest prepare complete (companyId: ").append(prepared.getCompanyId()).append(").");
        }
        sb.append("\nAap 'llc status' bhej kar progress dekh sakte hain.");
        return sb.toString();
    }

    private static String formatUpdated(OffsetDateTime updatedAt) {
        return updatedAt != null ? updatedAt.toString() : "n/a";
    }

    private static String defaultValue(String value, String fallback) {
        return notBlank(value) ? value : fallback;
    }

    private static String safe(String value) {
        return value != null ? value : "unknown error";
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
