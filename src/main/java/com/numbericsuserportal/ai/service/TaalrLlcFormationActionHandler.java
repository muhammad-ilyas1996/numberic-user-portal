package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.FormationMemberDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.NorthwestPrepareResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep1StateRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep2NameRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep3DetailsRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep4RegisteredAgentRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.corporatetools.NameCheckRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.corporatetools.NameCheckResponseDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRepository;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationNorthwestIntegrationService;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationRegisteredAgentService;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.service.LlcFormationService;
import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import com.numbericsuserportal.ai.action.TaalrIntent;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrLlcDraft;
import com.numbericsuserportal.ai.action.dto.TaalrLlcMemberDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.ai.util.TaalrInputValidation;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private LlcFormationRegisteredAgentService registeredAgentService;

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

        if (ctx.getLlcDraft().isAwaitingPrepareConfirm()) {
            return handlePrepareConfirm(request, user, existingSession, ctx, parsed, rawMessage);
        }

        mergeDraft(ctx.getLlcDraft(), parsed != null ? parsed.getLlc() : null);
        if (rawMessage != null && !rawMessage.isBlank()) {
            if (TaalrInputValidation.isConfusion(rawMessage)) {
                persistDraftSession(request, existingSession, ctx);
                return TaalrActionResult.handled("No problem — let's continue step by step.\n\n"
                        + questionFor(missingField(ctx.getLlcDraft())));
            }
            String missingBefore = missingField(ctx.getLlcDraft());
            if (missingBefore != null) {
                String err = applyDirectAnswer(ctx.getLlcDraft(), missingBefore, rawMessage.trim());
                if (err != null) {
                    persistDraftSession(request, existingSession, ctx);
                    return TaalrActionResult.handled(err + "\n\n" + questionFor(missingBefore));
                }
            }
        }

        String missing = missingField(ctx.getLlcDraft());
        if (missing != null) {
            persistDraftSession(request, existingSession, ctx);
            return TaalrActionResult.handled(questionFor(missing));
        }

        ctx.getLlcDraft().setAwaitingPrepareConfirm(true);
        if (existingSession != null) {
            sessionService.updateSession(existingSession, TaalrPendingAction.LLC_PREPARE_CONFIRM, ctx);
        } else {
            sessionService.saveSession(request, TaalrPendingAction.LLC_PREPARE_CONFIRM, ctx);
        }
        return TaalrActionResult.handled(formatPreparePreview(ctx.getLlcDraft())
                + "\n\nReply YES to run name check + prepare filing, or NO to cancel.");
    }

    /** Re-ask next LLC field without treating resume text as an answer. */
    public TaalrActionResult promptContinue(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        if (ctx.getLlcDraft() == null) {
            ctx.setLlcDraft(new TaalrLlcDraft());
        }
        if (ctx.getLlcDraft().isAwaitingPrepareConfirm()) {
            return TaalrActionResult.handled("Please reply YES to prepare filing, or NO to cancel.");
        }
        String missing = missingField(ctx.getLlcDraft());
        persistDraftSession(request, session, ctx);
        if (missing != null) {
            return TaalrActionResult.handled(questionFor(missing));
        }
        ctx.getLlcDraft().setAwaitingPrepareConfirm(true);
        sessionService.updateSession(session, TaalrPendingAction.LLC_PREPARE_CONFIRM, ctx);
        return TaalrActionResult.handled(formatPreparePreview(ctx.getLlcDraft())
                + "\n\nReply YES to run name check + prepare filing, or NO to cancel.");
    }

    public TaalrActionResult confirmPrepare(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        if (ctx.getLlcDraft() == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Say \"start LLC formation\" to begin again.");
        }
        return runPrepare(request, user, ctx);
    }

    public TaalrActionResult handleStatus(User user) {
        List<LlcFormation> rows = llcFormationService.getFormationsForUser(user);
        if (rows.isEmpty()) {
            return TaalrActionResult.handled("No LLC formation drafts yet. Say \"start LLC formation\" to begin, or ask me to guide you.");
        }
        LlcFormation latest = rows.get(0);
        String filingStatus = latest.getFilingStatus() != null ? latest.getFilingStatus() : "not-submitted";
        StringBuilder sb = new StringBuilder();
        sb.append("Latest LLC formation #").append(latest.getId()).append('\n');
        sb.append("• Name: ").append(blank(latest.getLlcName())).append('\n');
        sb.append("• State: ").append(blank(latest.getJurisdiction())).append('\n');
        sb.append("• Status: ").append(blank(latest.getStatus())).append('\n');
        sb.append("• Filing: ").append(filingStatus).append('\n');
        if (latest.getTotalCents() != null) {
            sb.append("• Estimated total: $")
                    .append(String.format(Locale.US, "%.2f", latest.getTotalCents() / 100.0)).append('\n');
        }
        sb.append("• Updated: ").append(latest.getUpdatedAt() != null ? latest.getUpdatedAt() : "n/a");
        return TaalrActionResult.handled(sb.toString());
    }

    public TaalrActionResult cancel(TaalrActionRequest request) {
        sessionService.clearSession(request);
        return TaalrActionResult.handled("LLC draft session cancelled. Say \"start LLC formation\" anytime, or ask for guidance.");
    }

    private TaalrActionResult handlePrepareConfirm(TaalrActionRequest request, User user,
            TaalrActionSessionEntity session, TaalrSessionContext ctx,
            TaalrIntentParseResult parsed, String rawMessage) {
        if (parsed != null && parsed.getIntent() == TaalrIntent.CONFIRM_YES) {
            return runPrepare(request, user, ctx);
        }
        if (parsed != null && (parsed.getIntent() == TaalrIntent.CONFIRM_NO || parsed.getIntent() == TaalrIntent.CANCEL)) {
            return cancel(request);
        }
        if (TaalrInputValidation.isConfusion(rawMessage)) {
            return TaalrActionResult.handled("Reply YES to prepare filing now, or NO to cancel.");
        }
        // Allow renaming only when message looks like a business name (not yes/no/short confirm words).
        if (rawMessage != null && !rawMessage.isBlank()
                && TaalrInputValidation.isValidBusinessName(rawMessage)
                && TaalrInputValidation.parseYesNo(rawMessage) == null
                && looksLikeLlcName(rawMessage)) {
            ctx.getLlcDraft().setLlcName(rawMessage.trim());
            ctx.getLlcDraft().setAwaitingPrepareConfirm(true);
            sessionService.updateSession(session, TaalrPendingAction.LLC_PREPARE_CONFIRM, ctx);
            return TaalrActionResult.handled("Updated LLC name to \"" + rawMessage.trim()
                    + "\".\nReply YES to run name check + prepare, or NO to cancel.");
        }
        return TaalrActionResult.handled("Please reply YES to prepare, or NO to cancel.");
    }

    private static boolean looksLikeLlcName(String value) {
        String t = value.trim();
        if (t.length() < 5 || t.length() > 120) {
            return false;
        }
        String lower = t.toLowerCase(Locale.ROOT);
        if (lower.equals("yes") || lower.equals("no") || lower.equals("y") || lower.equals("n")
                || lower.equals("cancel") || lower.equals("sure") || lower.equals("ok")
                || lower.equals("okay") || lower.equals("standard") || lower.equals("confirm")
                || lower.equals("proceed") || lower.startsWith("go ahead")) {
            return false;
        }
        return !TaalrInputValidation.isValidEmail(t) && !TaalrInputValidation.isValidPhone(t);
    }

    private TaalrActionResult runPrepare(TaalrActionRequest request, User user, TaalrSessionContext ctx) {
        try {
            LlcFormation formation = ensureDraftPersisted(user, ctx.getLlcDraft());
            NameCheckResponseDTO nameCheck = runNameCheck(formation);

            if (nameCheck == null
                    || (formation.getNameCheckStatus() != null
                    && "ERROR".equalsIgnoreCase(formation.getNameCheckStatus()))) {
                ctx.getLlcDraft().setAwaitingPrepareConfirm(true);
                sessionService.saveSession(request, TaalrPendingAction.LLC_PREPARE_CONFIRM, ctx);
                return TaalrActionResult.handled(
                        "Name check could not be completed right now. Reply YES to retry prepare, "
                                + "or send a new LLC name to change it, or cancel.");
            }

            if (nameCheck.getResult() != null && Boolean.FALSE.equals(nameCheck.getResult().getAvailable())) {
                ctx.getLlcDraft().setLlcName(null);
                ctx.getLlcDraft().setAwaitingPrepareConfirm(false);
                sessionService.saveSession(request, TaalrPendingAction.LLC_DRAFT, ctx);
                String suggestions = "";
                if (nameCheck.getResult().getSuggestions() != null && !nameCheck.getResult().getSuggestions().isEmpty()) {
                    suggestions = "\nSuggestions: " + String.join(", ", nameCheck.getResult().getSuggestions());
                }
                return TaalrActionResult.handled(
                        "Name check says \"" + formation.getLlcName() + "\" may be unavailable in "
                                + formation.getJurisdiction() + "."
                                + suggestions
                                + "\nPlease send a different LLC legal name to continue (session kept).");
            }

            NorthwestPrepareResponseDTO prepared = northwestIntegrationService.prepare(formation, user);
            sessionService.clearSession(request);
            return TaalrActionResult.handled(buildSuccessReply(formation, prepared, nameCheck));
        } catch (Exception e) {
            log.warn("LLC automation failed: {}", e.getMessage());
            ctx.getLlcDraft().setAwaitingPrepareConfirm(true);
            sessionService.saveSession(request, TaalrPendingAction.LLC_PREPARE_CONFIRM, ctx);
            return TaalrActionResult.handled(
                    "LLC draft is saved, but prepare failed: " + safe(e.getMessage())
                            + ". Reply YES to retry, or cancel and ask for guidance.");
        }
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
        step1.setOwnershipType(defaultValue(draft.getOwnershipType(), "single"));
        step1.setOperatesInFormationState(Boolean.TRUE);
        formation = llcFormationService.updateStep1(formation.getId(), user, step1);

        UpdateStep2NameRequestDTO step2 = new UpdateStep2NameRequestDTO();
        step2.setLlcName(draft.getLlcName());
        step2.setAltName(draft.getAltName());
        step2.setIndustry(draft.getIndustry());
        step2.setBusinessPurpose(draft.getBusinessPurpose());
        formation = llcFormationService.updateStep2(formation.getId(), user, step2);

        syncPrimaryFromMembers(draft);
        UpdateStep3DetailsRequestDTO step3 = new UpdateStep3DetailsRequestDTO();
        step3.setOwnerFirstName(draft.getOwnerFirstName());
        step3.setOwnerLastName(draft.getOwnerLastName());
        step3.setOwnerDob(draft.getOwnerDob());
        step3.setOwnerSsnLast4(draft.getOwnerSsnLast4());
        step3.setOwnershipPct(draft.getOwnerOwnershipPct());
        step3.setOwnerTitle(draft.getOwnerTitle());
        step3.setFilingSpeed(defaultValue(draft.getFilingSpeed(), "standard"));
        step3.setAddonEin(draft.getAddonEin() != null ? draft.getAddonEin() : Boolean.TRUE);
        step3.setAddonScorp(draft.getAddonScorp() != null ? draft.getAddonScorp() : Boolean.FALSE);
        step3.setManagementType("member");
        step3.setMembers(toFormationMembers(draft));
        formation = llcFormationService.updateStep3(formation.getId(), user, step3);

        UpdateStep4RegisteredAgentRequestDTO step4 = new UpdateStep4RegisteredAgentRequestDTO();
        String agentType = defaultValue(draft.getAgentType(), "NUMBRICS_NW");
        step4.setAgentType(agentType);
        if ("OWN".equalsIgnoreCase(agentType)) {
            step4.setAgentName(draft.getOwnAgentName());
            step4.setStreet(draft.getOwnAgentStreet());
            step4.setCity(draft.getOwnAgentCity());
            step4.setState(draft.getOwnAgentState() != null ? draft.getOwnAgentState() : draft.getJurisdiction());
            step4.setZip(draft.getOwnAgentZip());
        } else {
            step4.setAgentNameSnapshot("Numbrics / Northwest Registered Agent");
            step4.setAgentAddressSnapshot(draft.getJurisdiction());
        }
        registeredAgentService.upsert(formation.getId(), formation, step4);

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

    private void persistDraftSession(TaalrActionRequest request, TaalrActionSessionEntity existingSession,
            TaalrSessionContext ctx) {
        if (existingSession != null) {
            sessionService.updateSession(existingSession, TaalrPendingAction.LLC_DRAFT, ctx);
        } else {
            sessionService.saveSession(request, TaalrPendingAction.LLC_DRAFT, ctx);
        }
    }

    private static void mergeDraft(TaalrLlcDraft target, TaalrLlcDraft incoming) {
        if (incoming == null) {
            return;
        }
        if (incoming.getFormationId() != null) {
            target.setFormationId(incoming.getFormationId());
        }
        String state = TaalrInputValidation.parseUsState(incoming.getJurisdiction());
        if (state != null) {
            target.setJurisdiction(state);
        }
        if (TaalrInputValidation.isValidBusinessName(incoming.getLlcName())) {
            target.setLlcName(incoming.getLlcName().trim());
        }
        if (notBlank(incoming.getAltName())) {
            target.setAltName(incoming.getAltName().trim());
        }
        if (notBlank(incoming.getIndustry()) && !TaalrInputValidation.isConfusion(incoming.getIndustry())) {
            target.setIndustry(incoming.getIndustry().trim());
        }
        if (notBlank(incoming.getBusinessPurpose()) && !TaalrInputValidation.isConfusion(incoming.getBusinessPurpose())) {
            target.setBusinessPurpose(incoming.getBusinessPurpose().trim());
        }
        if (TaalrInputValidation.isValidPersonName(incoming.getOwnerFirstName())) {
            target.setOwnerFirstName(incoming.getOwnerFirstName().trim());
        }
        if (TaalrInputValidation.isValidPersonName(incoming.getOwnerLastName())) {
            target.setOwnerLastName(incoming.getOwnerLastName().trim());
        }
        if (notBlank(incoming.getFilingSpeed())) {
            target.setFilingSpeed(incoming.getFilingSpeed().trim().toLowerCase(Locale.ROOT));
        }
        if (incoming.getAddonEin() != null) {
            target.setAddonEin(incoming.getAddonEin());
        }
        if (incoming.getAddonScorp() != null) {
            target.setAddonScorp(incoming.getAddonScorp());
        }
        if (notBlank(incoming.getAgentType())) {
            target.setAgentType(incoming.getAgentType().trim().toUpperCase(Locale.ROOT));
        }
    }

    private static String missingField(TaalrLlcDraft draft) {
        if (draft.getMembers() == null) {
            draft.setMembers(new ArrayList<>());
        }
        if (!notBlank(draft.getJurisdiction())) return "jurisdiction";
        if (!notBlank(draft.getLlcName())) return "llcName";
        if (!notBlank(draft.getIndustry())) return "industry";
        if (!notBlank(draft.getBusinessPurpose())) return "businessPurpose";
        if (!notBlank(draft.getOwnershipType())) return "ownershipType";

        String memberGap = missingMemberField(draft);
        if (memberGap != null) {
            return memberGap;
        }

        if (!notBlank(draft.getFilingSpeed())) return "filingSpeed";
        if (draft.getAddonEin() == null) return "addonEin";
        if (draft.getAddonScorp() == null) return "addonScorp";
        if (!notBlank(draft.getAgentType())) return "agentType";
        if ("OWN".equalsIgnoreCase(draft.getAgentType())) {
            if (!notBlank(draft.getOwnAgentName())) return "ownAgentName";
            if (!notBlank(draft.getOwnAgentStreet())) return "ownAgentStreet";
            if (!notBlank(draft.getOwnAgentCity())) return "ownAgentCity";
            if (!notBlank(draft.getOwnAgentZip())) return "ownAgentZip";
        }
        return null;
    }

    private static String missingMemberField(TaalrLlcDraft draft) {
        if (Boolean.TRUE.equals(draft.getAskingAddAnotherMember())) {
            return "addAnotherMember";
        }
        TaalrLlcMemberDraft pending = draft.getPendingMember();
        if (pending == null && draft.getMembers().isEmpty()) {
            return "memberName";
        }
        if (pending != null) {
            if (!notBlank(pending.getFirstName()) || !notBlank(pending.getLastName())) return "memberName";
            if (!notBlank(pending.getDob())) return "memberDob";
            if (!notBlank(pending.getSsnLast4())) return "memberSsn";
            if (pending.getOwnershipPct() == null) {
                if ("single".equalsIgnoreCase(draft.getOwnershipType())) {
                    pending.setOwnershipPct(100);
                } else {
                    return "memberPct";
                }
            }
            if (!notBlank(pending.getTitle())) return "memberTitle";
        }
        if (draft.getMembers().isEmpty()) {
            return "memberName";
        }
        int total = draft.getMembers().stream().mapToInt(m -> m.getOwnershipPct() != null ? m.getOwnershipPct() : 0).sum();
        if ("multi".equalsIgnoreCase(draft.getOwnershipType()) && draft.getMembers().size() < 2) {
            // Deadlock guard: first member already took 100% — force re-entry.
            if (total >= 100) {
                return "memberPctFix";
            }
            return "memberName";
        }
        if (total < 100) {
            return "memberName";
        }
        if (total > 100) {
            return "memberPctFix";
        }
        return null;
    }

    private static String applyDirectAnswer(TaalrLlcDraft draft, String field, String answer) {
        if (TaalrInputValidation.isConfusion(answer)) {
            return "Please provide a clear answer.";
        }
        if (draft.getMembers() == null) {
            draft.setMembers(new ArrayList<>());
        }
        return switch (field) {
            case "jurisdiction" -> {
                String state = TaalrInputValidation.parseUsState(answer);
                if (state == null) {
                    yield "Please enter a valid US state code (e.g. TX, CA, FL, NY).";
                }
                draft.setJurisdiction(state);
                yield null;
            }
            case "llcName" -> {
                if (!TaalrInputValidation.isValidBusinessName(answer)) {
                    yield "Please enter a valid LLC name (at least 3 characters).";
                }
                draft.setLlcName(answer.trim());
                yield null;
            }
            case "industry" -> {
                if (answer.trim().length() < 2) {
                    yield "Please enter your industry (e.g. Consulting, Retail, Software).";
                }
                draft.setIndustry(answer.trim());
                yield null;
            }
            case "businessPurpose" -> {
                if (TaalrInputValidation.isSkip(answer)) {
                    draft.setBusinessPurpose("Any lawful business purpose");
                    yield null;
                }
                if (answer.trim().length() < 5) {
                    yield "Please describe the business purpose, or reply skip.";
                }
                draft.setBusinessPurpose(answer.trim());
                yield null;
            }
            case "ownershipType" -> {
                String type = TaalrInputValidation.parseOwnershipType(answer);
                if (type == null) {
                    yield "Is this a single-member or multi-member LLC? Reply single or multi.";
                }
                draft.setOwnershipType(type);
                yield null;
            }
            case "memberName" -> {
                String[] parts = answer.trim().split("\\s+");
                if (parts.length < 2 || !TaalrInputValidation.isValidPersonName(parts[0])
                        || !TaalrInputValidation.isValidPersonName(parts[parts.length - 1])) {
                    yield "Please send member first and last name (e.g. Ali Ahmed).";
                }
                TaalrLlcMemberDraft pending = new TaalrLlcMemberDraft();
                pending.setFirstName(parts[0]);
                pending.setLastName(parts[parts.length - 1]);
                pending.setPrimaryMember(draft.getMembers().isEmpty());
                draft.setPendingMember(pending);
                draft.setAskingAddAnotherMember(false);
                yield null;
            }
            case "memberDob" -> {
                if (!TaalrInputValidation.isValidDob(answer)) {
                    yield "Please enter DOB as YYYY-MM-DD (member must be 18+).";
                }
                ensurePending(draft).setDob(answer.trim());
                yield null;
            }
            case "memberSsn" -> {
                if (!TaalrInputValidation.isValidSsnLast4(answer)) {
                    yield "Please enter last 4 digits of SSN (e.g. 1234).";
                }
                ensurePending(draft).setSsnLast4(answer.trim());
                yield null;
            }
            case "memberPct" -> {
                Integer pct = TaalrInputValidation.parseOwnershipPct(answer);
                if ("single".equalsIgnoreCase(draft.getOwnershipType())) {
                    pct = 100;
                }
                if (pct == null) {
                    yield "Please enter ownership % between 1 and 100.";
                }
                int used = draft.getMembers().stream().mapToInt(m -> m.getOwnershipPct() != null ? m.getOwnershipPct() : 0).sum();
                if ("multi".equalsIgnoreCase(draft.getOwnershipType()) && draft.getMembers().isEmpty() && pct >= 100) {
                    yield "Multi-member LLC needs at least 2 members. First member must be under 100% (e.g. 50).";
                }
                if (used + pct > 100) {
                    yield "Ownership would exceed 100%. Remaining available: " + Math.max(0, 100 - used) + "%.";
                }
                ensurePending(draft).setOwnershipPct(pct);
                yield null;
            }
            case "memberTitle" -> {
                if (TaalrInputValidation.isSkip(answer)) {
                    ensurePending(draft).setTitle("Member");
                } else if (answer.trim().length() < 2) {
                    yield "Please enter title (e.g. Member, Manager), or reply skip.";
                } else {
                    ensurePending(draft).setTitle(answer.trim());
                }
                if ("single".equalsIgnoreCase(draft.getOwnershipType())) {
                    ensurePending(draft).setOwnershipPct(100);
                }
                draft.getMembers().add(draft.getPendingMember());
                draft.setPendingMember(null);
                syncPrimaryFromMembers(draft);
                int total = draft.getMembers().stream().mapToInt(m -> m.getOwnershipPct() != null ? m.getOwnershipPct() : 0).sum();
                if ("single".equalsIgnoreCase(draft.getOwnershipType())) {
                    draft.setAskingAddAnotherMember(false);
                    if (total != 100 || draft.getMembers().size() != 1) {
                        draft.getMembers().clear();
                        yield "Single-member LLC needs exactly one member at 100%. Please re-enter the member.";
                    }
                } else if (total < 100) {
                    draft.setAskingAddAnotherMember(true);
                } else {
                    draft.setAskingAddAnotherMember(false);
                }
                yield null;
            }
            case "addAnotherMember" -> {
                Boolean yn = TaalrInputValidation.parseYesNo(answer);
                if (yn == null) {
                    yield "Add another member? Reply YES or NO.";
                }
                if (Boolean.TRUE.equals(yn)) {
                    draft.setAskingAddAnotherMember(false);
                    draft.setPendingMember(null);
                } else {
                    int total = draft.getMembers().stream().mapToInt(m -> m.getOwnershipPct() != null ? m.getOwnershipPct() : 0).sum();
                    if (total != 100) {
                        draft.setAskingAddAnotherMember(true);
                        yield "Ownership must equal 100% before continuing. Current total: " + total
                                + "%. Reply YES to add another member.";
                    }
                    if ("multi".equalsIgnoreCase(draft.getOwnershipType()) && draft.getMembers().size() < 2) {
                        draft.setAskingAddAnotherMember(true);
                        yield "Multi-member LLC needs at least 2 members. Reply YES to add another member.";
                    }
                    draft.setAskingAddAnotherMember(false);
                }
                yield null;
            }
            case "memberPctFix" -> {
                draft.getMembers().clear();
                draft.setPendingMember(null);
                draft.setAskingAddAnotherMember(false);
                yield "Ownership exceeded 100% (or first member took 100% on multi). Let's re-enter members from the start.";
            }
            case "filingSpeed" -> {
                String speed = TaalrInputValidation.parseFilingSpeed(answer);
                if (speed == null) {
                    yield "Please choose: standard, expedited, or same day.";
                }
                draft.setFilingSpeed(speed);
                yield null;
            }
            case "addonEin" -> {
                Boolean yn = TaalrInputValidation.parseYesNo(answer);
                if (yn == null) {
                    yield "Do you want EIN filing add-on? Reply YES or NO.";
                }
                draft.setAddonEin(yn);
                yield null;
            }
            case "addonScorp" -> {
                Boolean yn = TaalrInputValidation.parseYesNo(answer);
                if (yn == null) {
                    yield "Do you want S-Corp election add-on? Reply YES or NO.";
                }
                draft.setAddonScorp(yn);
                yield null;
            }
            case "agentType" -> {
                String type = TaalrInputValidation.parseAgentType(answer);
                if (type == null) {
                    yield "Registered agent: reply 1 for Numbrics/Northwest, or 2 for your own agent.";
                }
                draft.setAgentType(type);
                if ("OWN".equals(type)) {
                    draft.setOwnAgentState(draft.getJurisdiction());
                }
                yield null;
            }
            case "ownAgentName" -> {
                if (answer.trim().length() < 2) {
                    yield "Please enter registered agent full name.";
                }
                draft.setOwnAgentName(answer.trim());
                yield null;
            }
            case "ownAgentStreet" -> {
                String street = answer.trim().toLowerCase(Locale.ROOT);
                if (street.contains("p.o.") || street.contains("po box")) {
                    yield "Registered agent needs a physical street address (no P.O. Box).";
                }
                draft.setOwnAgentStreet(answer.trim());
                yield null;
            }
            case "ownAgentCity" -> {
                if (answer.trim().length() < 2) {
                    yield "Please enter city.";
                }
                draft.setOwnAgentCity(answer.trim());
                yield null;
            }
            case "ownAgentZip" -> {
                if (!answer.trim().matches("\\d{5}(-\\d{4})?")) {
                    yield "Please enter a valid ZIP code (e.g. 75001).";
                }
                draft.setOwnAgentZip(answer.trim());
                draft.setOwnAgentState(draft.getJurisdiction());
                yield null;
            }
            default -> null;
        };
    }

    private static TaalrLlcMemberDraft ensurePending(TaalrLlcDraft draft) {
        if (draft.getPendingMember() == null) {
            draft.setPendingMember(new TaalrLlcMemberDraft());
        }
        return draft.getPendingMember();
    }

    private static void syncPrimaryFromMembers(TaalrLlcDraft draft) {
        if (draft.getMembers() == null || draft.getMembers().isEmpty()) {
            return;
        }
        TaalrLlcMemberDraft primary = draft.getMembers().get(0);
        draft.setOwnerFirstName(primary.getFirstName());
        draft.setOwnerLastName(primary.getLastName());
        draft.setOwnerDob(primary.getDob());
        draft.setOwnerSsnLast4(primary.getSsnLast4());
        draft.setOwnerOwnershipPct(primary.getOwnershipPct());
        draft.setOwnerTitle(primary.getTitle());
    }

    private static List<FormationMemberDTO> toFormationMembers(TaalrLlcDraft draft) {
        List<FormationMemberDTO> list = new ArrayList<>();
        if (draft.getMembers() == null) {
            return list;
        }
        int idx = 0;
        for (TaalrLlcMemberDraft m : draft.getMembers()) {
            FormationMemberDTO dto = new FormationMemberDTO();
            dto.setFirstName(m.getFirstName());
            dto.setLastName(m.getLastName());
            dto.setDob(m.getDob());
            dto.setSsnLast4(m.getSsnLast4());
            dto.setOwnershipPct(m.getOwnershipPct());
            dto.setTitle(m.getTitle() != null ? m.getTitle() : "Member");
            dto.setPrimaryMember(idx == 0);
            list.add(dto);
            idx++;
        }
        return list;
    }

    private static String questionFor(String field) {
        if (field == null) {
            return "Please continue LLC formation details.";
        }
        return switch (field) {
            case "jurisdiction" -> "Which US state should we form the LLC in? (e.g. TX, CA, FL)";
            case "llcName" -> "What is the exact LLC legal name?";
            case "industry" -> "What industry is this business in?";
            case "businessPurpose" -> "What is the business purpose? (or reply skip)";
            case "ownershipType" -> "Single-member or multi-member LLC? Reply single or multi.";
            case "memberName" -> "Member first and last name? (e.g. Ali Ahmed)";
            case "memberDob" -> "Member date of birth? (YYYY-MM-DD)";
            case "memberSsn" -> "Member SSN last 4 digits?";
            case "memberPct" -> "Ownership percentage for this member? (e.g. 50). Single-member is auto 100%.";
            case "memberTitle" -> "Member title? (Member/Manager, or reply skip)";
            case "addAnotherMember" -> "Add another member? Reply YES or NO. (Ownership must total 100%)";
            case "memberPctFix" -> "Ownership is invalid for multi-member. Reply anything to restart member entry.";
            case "filingSpeed" -> "Filing speed: standard, expedited, or same day?";
            case "addonEin" -> "Add EIN filing service? Reply YES or NO.";
            case "addonScorp" -> "Add S-Corp election service? Reply YES or NO.";
            case "agentType" -> "Registered agent:\n1) Numbrics / Northwest\n2) My own agent\nReply 1 or 2.";
            case "ownAgentName" -> "Own registered agent full name?";
            case "ownAgentStreet" -> "Registered agent street address? (no P.O. Box)";
            case "ownAgentCity" -> "Registered agent city?";
            case "ownAgentZip" -> "Registered agent ZIP code?";
            default -> "Please continue LLC formation details.";
        };
    }

    private static String formatPreparePreview(TaalrLlcDraft d) {
        StringBuilder sb = new StringBuilder("LLC draft ready for prepare:\n\n");
        sb.append("• State: ").append(d.getJurisdiction()).append('\n');
        sb.append("• Name: ").append(d.getLlcName()).append('\n');
        sb.append("• Industry: ").append(d.getIndustry()).append('\n');
        sb.append("• Purpose: ").append(d.getBusinessPurpose()).append('\n');
        sb.append("• Ownership: ").append(d.getOwnershipType()).append('\n');
        sb.append("• Members:\n");
        if (d.getMembers() != null) {
            int i = 1;
            for (TaalrLlcMemberDraft m : d.getMembers()) {
                sb.append("  ").append(i++).append(") ").append(m.getFirstName()).append(' ').append(m.getLastName())
                        .append(" | DOB ").append(m.getDob())
                        .append(" | SSN on file")
                        .append(" | ").append(m.getOwnershipPct()).append('%')
                        .append(" | ").append(m.getTitle()).append('\n');
            }
        }
        sb.append("• Speed: ").append(d.getFilingSpeed()).append('\n');
        sb.append("• EIN add-on: ").append(Boolean.TRUE.equals(d.getAddonEin()) ? "Yes" : "No").append('\n');
        sb.append("• S-Corp add-on: ").append(Boolean.TRUE.equals(d.getAddonScorp()) ? "Yes" : "No").append('\n');
        sb.append("• Registered agent: ").append(d.getAgentType());
        return sb.toString();
    }

    private static String buildSuccessReply(LlcFormation formation, NorthwestPrepareResponseDTO prepared,
            NameCheckResponseDTO nameCheck) {
        StringBuilder sb = new StringBuilder();
        sb.append("LLC formation prepared successfully.\n");
        sb.append("Formation #").append(formation.getId());
        sb.append(" | status: ").append(formation.getStatus());
        sb.append(" | state: ").append(formation.getJurisdiction());
        if (formation.getTotalCents() != null) {
            sb.append(" | estimated total: $").append(String.format(Locale.US, "%.2f", formation.getTotalCents() / 100.0));
        }
        if (nameCheck != null && nameCheck.getResult() != null && nameCheck.getResult().getAvailable() != null) {
            sb.append(" | name check: ").append(Boolean.TRUE.equals(nameCheck.getResult().getAvailable()) ? "AVAILABLE" : "TAKEN");
        } else {
            sb.append(" | name check: pending/error");
        }
        if (prepared != null && prepared.getCompanyId() != null) {
            sb.append("\nNorthwest companyId: ").append(prepared.getCompanyId());
        }
        sb.append("\nNext: complete payment from the LLC Formation dashboard, then filing continues.");
        sb.append("\nSay \"llc status\" anytime for updates.");
        return sb.toString();
    }

    private static String defaultValue(String value, String fallback) {
        return notBlank(value) ? value : fallback;
    }

    private static String safe(String value) {
        return value != null ? value : "unknown error";
    }

    private static String blank(String value) {
        return notBlank(value) ? value : "—";
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
