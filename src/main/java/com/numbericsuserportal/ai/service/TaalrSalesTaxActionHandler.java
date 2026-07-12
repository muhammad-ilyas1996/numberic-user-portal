package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrIntent;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrSalesTaxDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.ai.util.TaalrInputValidation;
import com.numbericsuserportal.kintsugi.catalog.BusinessTypeCatalog;
import com.numbericsuserportal.kintsugi.domain.SalesTaxBusinessType;
import com.numbericsuserportal.kintsugi.dto.EnterSalesEstimateRequestDTO;
import com.numbericsuserportal.kintsugi.dto.FilingDraftRequestDTO;
import com.numbericsuserportal.kintsugi.dto.FilingFlowSelectionDTO;
import com.numbericsuserportal.kintsugi.dto.ProductCategoryDTO;
import com.numbericsuserportal.kintsugi.dto.ProductSubCategoryDTO;
import com.numbericsuserportal.kintsugi.service.ExemptionService;
import com.numbericsuserportal.kintsugi.service.KintsugiFilingSubmissionService;
import com.numbericsuserportal.kintsugi.service.KintsugiProductService;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Taalr sales-tax filing automation (Kintsugi filing flow).
 * Collects draft → review confirm → saves DRAFT; payment + submit stay on dashboard.
 */
@Service
@Slf4j
public class TaalrSalesTaxActionHandler {

    private static final int MAX_LIST = 12;

    @Autowired
    private ExemptionService exemptionService;

    @Autowired
    private KintsugiFilingSubmissionService filingSubmissionService;

    @Autowired
    private KintsugiProductService productService;

    @Autowired
    private TaalrActionSessionService sessionService;

    public TaalrActionResult handleStartOrContinue(TaalrActionRequest request, User user,
            TaalrIntentParseResult parsed, TaalrActionSessionEntity existingSession, String rawMessage) {
        TaalrSessionContext ctx = existingSession != null
                ? sessionService.loadContext(existingSession)
                : new TaalrSessionContext();
        if (ctx.getSalesTaxDraft() == null) {
            ctx.setSalesTaxDraft(new TaalrSalesTaxDraft());
        }
        seedFromUser(user, ctx.getSalesTaxDraft());
        mergeDraft(ctx.getSalesTaxDraft(), parsed != null ? parsed.getSalesTax() : null);

        // Ensure nexus/registration flags when state was pre-filled (heuristic/Claude).
        if (notBlank(ctx.getSalesTaxDraft().getStateCode())
                && ctx.getSalesTaxDraft().getRegistrationRequired() == null) {
            try {
                ensureRegistrationFlags(user, ctx.getSalesTaxDraft());
            } catch (Exception e) {
                log.debug("Nexus context optional: {}", e.getMessage());
            }
        }

        if (ctx.getSalesTaxDraft().isAwaitingReviewConfirm()) {
            return handleReviewConfirm(request, user, existingSession, ctx, parsed, rawMessage);
        }

        if (rawMessage != null && !rawMessage.isBlank()) {
            if (TaalrInputValidation.isConfusion(rawMessage)) {
                persistDraft(request, existingSession, ctx);
                return TaalrActionResult.handled("No problem — let's continue step by step.\n\n"
                        + questionFor(missingField(user, ctx.getSalesTaxDraft()), user, ctx.getSalesTaxDraft()));
            }
            String missingBefore = missingField(user, ctx.getSalesTaxDraft());
            if (missingBefore != null) {
                String err = applyDirectAnswer(user, ctx.getSalesTaxDraft(), missingBefore, rawMessage.trim());
                if (err != null) {
                    persistDraft(request, existingSession, ctx);
                    return TaalrActionResult.handled(err + "\n\n"
                            + questionFor(missingBefore, user, ctx.getSalesTaxDraft()));
                }
            }
        }

        String missing = missingField(user, ctx.getSalesTaxDraft());
        if (missing != null) {
            persistDraft(request, existingSession, ctx);
            return TaalrActionResult.handled(questionFor(missing, user, ctx.getSalesTaxDraft()));
        }

        ctx.getSalesTaxDraft().setAwaitingReviewConfirm(true);
        if (existingSession != null) {
            sessionService.updateSession(existingSession, TaalrPendingAction.SALES_TAX_REVIEW_CONFIRM, ctx);
        } else {
            sessionService.saveSession(request, TaalrPendingAction.SALES_TAX_REVIEW_CONFIRM, ctx);
        }
        return TaalrActionResult.handled(formatReviewPreview(ctx.getSalesTaxDraft())
                + "\n\nReply YES to save this sales-tax filing draft, or NO to cancel.");
    }

    public TaalrActionResult promptContinue(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        if (ctx.getSalesTaxDraft() == null) {
            ctx.setSalesTaxDraft(new TaalrSalesTaxDraft());
        }
        seedFromUser(user, ctx.getSalesTaxDraft());
        if (ctx.getSalesTaxDraft().isAwaitingReviewConfirm()) {
            return TaalrActionResult.handled("Please reply YES to save the sales-tax draft, or NO to cancel.");
        }
        String missing = missingField(user, ctx.getSalesTaxDraft());
        persistDraft(request, session, ctx);
        if (missing != null) {
            return TaalrActionResult.handled(questionFor(missing, user, ctx.getSalesTaxDraft()));
        }
        ctx.getSalesTaxDraft().setAwaitingReviewConfirm(true);
        sessionService.updateSession(session, TaalrPendingAction.SALES_TAX_REVIEW_CONFIRM, ctx);
        return TaalrActionResult.handled(formatReviewPreview(ctx.getSalesTaxDraft())
                + "\n\nReply YES to save this sales-tax filing draft, or NO to cancel.");
    }

    public TaalrActionResult confirmReview(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        if (ctx.getSalesTaxDraft() == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Say \"file sales tax\" to begin again.");
        }
        return runSaveDraft(request, user, ctx);
    }

    public TaalrActionResult handleStatus(User user) {
        try {
            Map<String, Object> summary = filingSubmissionService.dashboardSummary(user);
            Map<String, Object> schedule = filingSubmissionService.filingSchedule(user);

            StringBuilder sb = new StringBuilder("Sales tax status:\n\n");
            sb.append("• YTD tax collected: $")
                    .append(fmtMoney(summary.get("ytdTaxCollected"))).append('\n');
            sb.append("• Filed count: ").append(summary.getOrDefault("filedCount", 0)).append('\n');
            sb.append("• Pending drafts: ").append(summary.getOrDefault("pendingCount", 0)).append('\n');
            if (Boolean.TRUE.equals(summary.get("businessTypeSetupRequired"))) {
                sb.append("• Business type: not set yet\n");
            } else if (user.getBusinessType() != null) {
                sb.append("• Business type: ")
                        .append(BusinessTypeCatalog.displayName(user.getBusinessType())).append('\n');
            }

            Object next = summary.get("nextFiling");
            if (next instanceof Map<?, ?> nextMap) {
                sb.append("\nNext / open filing:\n");
                sb.append("• State: ").append(blank(nextMap.get("stateCode"))).append('\n');
                sb.append("• Status: ").append(blank(nextMap.get("status"))).append('\n');
                sb.append("• Due: ").append(blank(nextMap.get("dueDate"))).append('\n');
                if (nextMap.get("filingId") != null) {
                    sb.append("• Filing ID: ").append(nextMap.get("filingId")).append('\n');
                }
            }

            Object filings = schedule.get("filings");
            if (filings instanceof List<?> list && !list.isEmpty()) {
                sb.append("\nRecent filings:\n");
                int i = 0;
                for (Object row : list) {
                    if (i++ >= 5) {
                        break;
                    }
                    if (row instanceof Map<?, ?> m) {
                        sb.append("• ").append(blank(m.get("stateCode")))
                                .append(" | ").append(blank(m.get("status")))
                                .append(" | due ").append(blank(m.get("dueDate"))).append('\n');
                    }
                }
            }

            sb.append("\nSay \"file sales tax\" to prepare a new quarterly draft, or open Sales Tax on the dashboard to pay & file.");
            return TaalrActionResult.handled(sb.toString());
        } catch (Exception e) {
            log.warn("Sales tax status failed: {}", e.getMessage());
            return TaalrActionResult.handled(
                    "I couldn't load sales tax status right now. Please try the Sales Tax dashboard, or ask me to guide you.");
        }
    }

    public TaalrActionResult cancel(TaalrActionRequest request) {
        sessionService.clearSession(request);
        return TaalrActionResult.handled(
                "Sales tax draft cancelled. Say \"file sales tax\" anytime, or ask for guidance.");
    }

    private TaalrActionResult handleReviewConfirm(TaalrActionRequest request, User user,
            TaalrActionSessionEntity session, TaalrSessionContext ctx,
            TaalrIntentParseResult parsed, String rawMessage) {
        if (parsed != null && parsed.getIntent() == TaalrIntent.CONFIRM_YES) {
            return runSaveDraft(request, user, ctx);
        }
        if (parsed != null && (parsed.getIntent() == TaalrIntent.CONFIRM_NO || parsed.getIntent() == TaalrIntent.CANCEL)) {
            return cancel(request);
        }
        if (TaalrInputValidation.isConfusion(rawMessage)) {
            return TaalrActionResult.handled("Reply YES to save the draft, or NO to cancel.");
        }
        return TaalrActionResult.handled("Please reply YES to save the sales-tax draft, or NO to cancel.");
    }

    private TaalrActionResult runSaveDraft(TaalrActionRequest request, User user, TaalrSessionContext ctx) {
        TaalrSalesTaxDraft draft = ctx.getSalesTaxDraft();
        try {
            if (user.getBusinessType() == null && notBlank(draft.getBusinessType())) {
                SalesTaxBusinessType type = SalesTaxBusinessType.valueOf(draft.getBusinessType());
                exemptionService.saveBusinessType(user, type, draft.getBusinessTypeDescription());
            }

            if (!Boolean.TRUE.equals(draft.getExemptionsConfirmed())) {
                exemptionService.confirmExemptionProfile(user, draft.getStateCode());
                draft.setExemptionsConfirmed(true);
            }

            ensureRegistrationFlags(user, draft);

            FilingFlowSelectionDTO selection = new FilingFlowSelectionDTO();
            selection.setFilingId(draft.getFilingId());
            selection.setStateCode(draft.getStateCode());
            selection.setCategory(draft.getCategory());
            selection.setSubcategory(draft.getSubcategory());
            selection.setRegistrationRequired(draft.getRegistrationRequired());
            selection.setStateTaxAccountId(draft.getStateTaxAccountId());
            Map<String, Object> savedSelection = filingSubmissionService.saveFlowSelection(user, selection);
            String filingId = String.valueOf(savedSelection.get("filingId"));
            draft.setFilingId(filingId);

            EnterSalesEstimateRequestDTO estimateReq = new EnterSalesEstimateRequestDTO();
            estimateReq.setFilingId(filingId);
            estimateReq.setStateCode(draft.getStateCode());
            estimateReq.setCategory(draft.getCategory());
            estimateReq.setSubcategory(draft.getSubcategory());
            estimateReq.setTaxableAmount(draft.getTaxableAmount());
            estimateReq.setExemptAmount(draft.getExemptAmount() != null ? draft.getExemptAmount() : 0.0);
            if (notBlank(draft.getCity())) {
                estimateReq.setCity(draft.getCity());
            }
            if (notBlank(draft.getPostalCode())) {
                estimateReq.setPostalCode(draft.getPostalCode());
            }
            Map<String, Object> estimate = filingSubmissionService.estimateAndSaveSales(user, estimateReq);

            FilingDraftRequestDTO reviewReq = new FilingDraftRequestDTO();
            reviewReq.setFilingId(filingId);
            reviewReq.setStateCode(draft.getStateCode());
            reviewReq.setCategory(draft.getCategory());
            reviewReq.setSubcategory(draft.getSubcategory());
            reviewReq.setTaxableAmount(draft.getTaxableAmount());
            reviewReq.setExemptAmount(draft.getExemptAmount() != null ? draft.getExemptAmount() : 0.0);
            reviewReq.setRegistrationRequired(draft.getRegistrationRequired());
            reviewReq.setStateTaxAccountId(draft.getStateTaxAccountId());
            Map<String, Object> review = filingSubmissionService.buildReview(user, reviewReq);

            sessionService.clearSession(request);
            return TaalrActionResult.handled(formatSuccess(draft, estimate, review));
        } catch (Exception e) {
            log.warn("Sales tax draft save failed: {}", e.getMessage(), e);
            draft.setAwaitingReviewConfirm(true);
            sessionService.saveSession(request, TaalrPendingAction.SALES_TAX_REVIEW_CONFIRM, ctx);
            return TaalrActionResult.handled(
                    "I couldn't save the sales-tax draft: " + safe(e.getMessage())
                            + ". Reply YES to retry, or cancel and use the Sales Tax dashboard.");
        }
    }

    private static void seedFromUser(User user, TaalrSalesTaxDraft draft) {
        if (user.getBusinessType() != null && !notBlank(draft.getBusinessType())) {
            draft.setBusinessType(user.getBusinessType().name());
        }
    }

    private void mergeDraft(TaalrSalesTaxDraft target, TaalrSalesTaxDraft source) {
        if (source == null) {
            return;
        }
        if (notBlank(source.getBusinessType())) {
            target.setBusinessType(source.getBusinessType().trim());
        }
        if (notBlank(source.getBusinessTypeDescription())) {
            target.setBusinessTypeDescription(source.getBusinessTypeDescription().trim());
        }
        if (notBlank(source.getStateCode())) {
            String state = TaalrInputValidation.parseUsState(source.getStateCode());
            if (state != null) {
                target.setStateCode(state);
            }
        }
        if (notBlank(source.getCategory())) {
            target.setCategory(source.getCategory().trim());
        }
        if (notBlank(source.getSubcategory())) {
            target.setSubcategory(source.getSubcategory().trim());
        }
        if (source.getTaxableAmount() != null && source.getTaxableAmount() >= 0) {
            target.setTaxableAmount(source.getTaxableAmount());
        }
        if (source.getExemptAmount() != null && source.getExemptAmount() >= 0) {
            target.setExemptAmount(source.getExemptAmount());
        }
        if (notBlank(source.getCity())) {
            target.setCity(source.getCity().trim());
            target.setCityCollected(true);
        }
        if (notBlank(source.getPostalCode())) {
            target.setPostalCode(source.getPostalCode().trim());
            target.setPostalCollected(true);
        }
        if (notBlank(source.getFilingId())) {
            target.setFilingId(source.getFilingId().trim());
        }
    }

    private String missingField(User user, TaalrSalesTaxDraft draft) {
        if (user.getBusinessType() == null && !notBlank(draft.getBusinessType())) {
            return "businessType";
        }
        if ((user.getBusinessType() == null || user.getBusinessType() == SalesTaxBusinessType.other)
                && "other".equalsIgnoreCase(draft.getBusinessType() != null ? draft.getBusinessType()
                : (user.getBusinessType() != null ? user.getBusinessType().name() : null))
                && !notBlank(draft.getBusinessTypeDescription())
                && !notBlank(user.getBusinessTypeDescription())) {
            return "businessTypeDescription";
        }
        if (!notBlank(draft.getStateCode())) {
            return "stateCode";
        }
        if (!Boolean.TRUE.equals(draft.getExemptionsConfirmed())) {
            return "exemptionsConfirm";
        }
        if (!notBlank(draft.getCategory())) {
            return "category";
        }
        if (!notBlank(draft.getSubcategory())) {
            return "subcategory";
        }
        if (draft.getTaxableAmount() == null) {
            return "taxableAmount";
        }
        if (draft.getExemptAmount() == null) {
            return "exemptAmount";
        }
        if (!Boolean.TRUE.equals(draft.getCityCollected())) {
            return "city";
        }
        if (!Boolean.TRUE.equals(draft.getPostalCollected())) {
            return "postalCode";
        }
        if (Boolean.TRUE.equals(draft.getRegistrationRequired())
                && !Boolean.TRUE.equals(draft.getRegistrationAcknowledged())) {
            return "registrationAck";
        }
        return null;
    }

    private String applyDirectAnswer(User user, TaalrSalesTaxDraft draft, String field, String answer) {
        if (TaalrInputValidation.isConfusion(answer)) {
            return "Please provide a clear answer.";
        }
        return switch (field) {
            case "businessType" -> {
                SalesTaxBusinessType type = parseBusinessType(answer);
                if (type == null) {
                    yield "Please choose a business type (reply with number or name):\n" + formatBusinessTypes();
                }
                draft.setBusinessType(type.name());
                try {
                    exemptionService.saveBusinessType(user, type,
                            type == SalesTaxBusinessType.other ? draft.getBusinessTypeDescription() : null);
                } catch (Exception e) {
                    yield "Couldn't save business type: " + safe(e.getMessage());
                }
                yield null;
            }
            case "businessTypeDescription" -> {
                if (answer.trim().length() < 3) {
                    yield "Please briefly describe your business (at least 3 characters).";
                }
                draft.setBusinessTypeDescription(answer.trim());
                try {
                    exemptionService.saveBusinessType(user, SalesTaxBusinessType.other, answer.trim());
                } catch (Exception e) {
                    yield "Couldn't save description: " + safe(e.getMessage());
                }
                yield null;
            }
            case "stateCode" -> {
                String state = TaalrInputValidation.parseUsState(answer);
                if (state == null) {
                    yield "Please enter a valid US state code (e.g. TX, CA, NY, FL).";
                }
                draft.setStateCode(state);
                draft.setExemptionsConfirmed(false);
                draft.setCategory(null);
                draft.setSubcategory(null);
                draft.setRegistrationAcknowledged(false);
                draft.setCityCollected(false);
                draft.setPostalCollected(false);
                draft.setCity(null);
                draft.setPostalCode(null);
                try {
                    ensureRegistrationFlags(user, draft);
                } catch (Exception e) {
                    log.debug("Nexus context optional: {}", e.getMessage());
                }
                yield null;
            }
            case "exemptionsConfirm" -> {
                Boolean yn = TaalrInputValidation.parseYesNo(answer);
                if (yn == null) {
                    yield "Reply YES to confirm the suggested exemptions, or NO to edit them on the Sales Tax dashboard.";
                }
                if (Boolean.FALSE.equals(yn)) {
                    yield "No problem — open Sales Tax → Exemptions on the dashboard to customize, "
                            + "then say \"file sales tax\" again. Or reply YES to use suggested defaults.";
                }
                try {
                    exemptionService.confirmExemptionProfile(user, draft.getStateCode());
                    draft.setExemptionsConfirmed(true);
                } catch (Exception e) {
                    yield "Couldn't confirm exemptions: " + safe(e.getMessage());
                }
                yield null;
            }
            case "category" -> {
                String cat = resolveCategory(answer);
                if (cat == null) {
                    yield "Please choose a product category by number or name:\n" + formatCategories();
                }
                draft.setCategory(cat);
                draft.setSubcategory(null);
                yield null;
            }
            case "subcategory" -> {
                String sub = resolveSubcategory(draft.getCategory(), answer);
                if (sub == null) {
                    yield "Please choose a subcategory by number or name:\n"
                            + formatSubcategories(draft.getCategory());
                }
                draft.setSubcategory(sub);
                yield null;
            }
            case "taxableAmount" -> {
                Double amt = TaalrInputValidation.parseAmount(answer);
                if (amt == null) {
                    yield "Please enter taxable sales amount for this quarter (e.g. 12000).";
                }
                draft.setTaxableAmount(amt);
                yield null;
            }
            case "exemptAmount" -> {
                if (TaalrInputValidation.isSkip(answer)) {
                    draft.setExemptAmount(0.0);
                    yield null;
                }
                Double amt = TaalrInputValidation.parseAmount(answer);
                if (amt == null && "0".equals(answer.trim())) {
                    draft.setExemptAmount(0.0);
                    yield null;
                }
                if (amt == null) {
                    yield "Please enter exempt sales amount (e.g. 500), or reply skip for $0.";
                }
                draft.setExemptAmount(amt);
                yield null;
            }
            case "city" -> {
                if (TaalrInputValidation.isSkip(answer)) {
                    draft.setCity(null);
                    draft.setCityCollected(true);
                    yield null;
                }
                if (answer.trim().length() < 2 || answer.trim().length() > 80) {
                    yield "Please enter a city name, or reply skip to use the state default.";
                }
                draft.setCity(answer.trim());
                draft.setCityCollected(true);
                yield null;
            }
            case "postalCode" -> {
                if (TaalrInputValidation.isSkip(answer)) {
                    draft.setPostalCode(null);
                    draft.setPostalCollected(true);
                    yield null;
                }
                String zip = answer.trim().replaceAll("[^0-9A-Za-z\\-]", "");
                if (zip.length() < 5 || zip.length() > 10) {
                    yield "Please enter a valid ZIP / postal code (e.g. 78701), or reply skip for default.";
                }
                draft.setPostalCode(zip);
                draft.setPostalCollected(true);
                yield null;
            }
            case "registrationAck" -> {
                Boolean yn = TaalrInputValidation.parseYesNo(answer);
                if (yn == null) {
                    yield "This state may need registration (extra fee on dashboard). Reply YES to continue, or NO to stop.";
                }
                if (Boolean.FALSE.equals(yn)) {
                    yield "Okay — reply \"cancel\" to discard, or YES if you still want to save the draft.";
                }
                draft.setRegistrationAcknowledged(true);
                yield null;
            }
            default -> "Please continue with the sales tax details.";
        };
    }

    private String questionFor(String field, User user, TaalrSalesTaxDraft draft) {
        if (field == null) {
            return "Please continue sales tax filing.";
        }
        return switch (field) {
            case "businessType" -> "What type of business are you filing for?\n" + formatBusinessTypes();
            case "businessTypeDescription" -> "Briefly describe your business (since you chose Other).";
            case "stateCode" -> "Which US state are you filing sales tax for? (e.g. TX)";
            case "exemptionsConfirm" -> formatExemptionPreview(user, draft.getStateCode())
                    + "\n\nReply YES to confirm these suggested taxable/exempt categories, "
                    + "or NO to customize on the dashboard.";
            case "category" -> "What product category best matches your sales?\n" + formatCategories();
            case "subcategory" -> "Which subcategory?\n" + formatSubcategories(draft.getCategory());
            case "taxableAmount" -> "Taxable sales amount for the current quarter? (e.g. 15000)";
            case "exemptAmount" -> "Exempt sales amount this quarter? (e.g. 500, or skip for $0)";
            case "city" -> "City for tax rate estimate? (e.g. Austin — or reply skip for state default)";
            case "postalCode" -> "ZIP / postal code? (e.g. 78701 — or reply skip for state default)";
            case "registrationAck" -> "Note: " + draft.getStateCode()
                    + " may require state registration (extra fee on dashboard when you pay).\n"
                    + "Reply YES to continue saving the draft, or NO to stop.";
            default -> "Please continue sales tax filing.";
        };
    }

    private void ensureRegistrationFlags(User user, TaalrSalesTaxDraft draft) {
        if (!notBlank(draft.getStateCode())) {
            return;
        }
        Map<String, Object> ctx = filingSubmissionService.stateFilingContext(user, draft.getStateCode());
        draft.setRegistrationRequired(Boolean.TRUE.equals(ctx.get("registrationRequired")));
        Object accountId = ctx.get("stateTaxAccountId");
        if (accountId != null && !String.valueOf(accountId).isBlank()) {
            draft.setStateTaxAccountId(String.valueOf(accountId));
        }
    }

    private String formatExemptionPreview(User user, String stateCode) {
        try {
            if (user.getBusinessType() == null) {
                return "I'll suggest exemptions after business type is set for " + stateCode + ".";
            }
            Map<String, Object> profile = exemptionService.getExemptionProfile(user, stateCode);
            StringBuilder sb = new StringBuilder("Suggested exemptions for ")
                    .append(profile.getOrDefault("stateName", stateCode))
                    .append(" (").append(profile.getOrDefault("businessTypeLabel", "")).append("):\n");
            Object taxable = profile.get("taxableCategories");
            Object exempt = profile.get("exemptCategories");
            sb.append("• Taxable: ").append(shortList(taxable)).append('\n');
            sb.append("• Exempt: ").append(shortList(exempt));
            return sb.toString();
        } catch (Exception e) {
            return "I can use suggested exemptions for " + stateCode + ".";
        }
    }

    private static String shortList(Object value) {
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return "—";
        }
        return list.stream().limit(6).map(String::valueOf).collect(Collectors.joining(", "))
                + (list.size() > 6 ? "…" : "");
    }

    private static String formatBusinessTypes() {
        StringBuilder sb = new StringBuilder();
        List<Map<String, Object>> types = BusinessTypeCatalog.allTypes();
        for (int i = 0; i < types.size(); i++) {
            Map<String, Object> t = types.get(i);
            sb.append(i + 1).append(") ").append(t.get("name")).append('\n');
        }
        sb.append("Reply with a number (1–").append(types.size()).append(") or the type name.");
        return sb.toString().trim();
    }

    private String formatCategories() {
        try {
            List<ProductCategoryDTO> cats = productService.getCategories();
            if (cats == null || cats.isEmpty()) {
                return "Reply with your product category name (from the Sales Tax catalog).";
            }
            StringBuilder sb = new StringBuilder();
            int n = Math.min(MAX_LIST, cats.size());
            for (int i = 0; i < n; i++) {
                sb.append(i + 1).append(") ").append(cats.get(i).getName()).append('\n');
            }
            if (cats.size() > n) {
                sb.append("… or type the exact category name.\n");
            }
            sb.append("Reply with a number or category name.");
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("Category list failed: {}", e.getMessage());
            return "Reply with your product category name (e.g. Physical, Digital).";
        }
    }

    private String formatSubcategories(String category) {
        try {
            List<ProductSubCategoryDTO> subs = productService.getSubcategories(category);
            if (subs == null || subs.isEmpty()) {
                return "Reply with the subcategory name.";
            }
            StringBuilder sb = new StringBuilder();
            int n = Math.min(MAX_LIST, subs.size());
            for (int i = 0; i < n; i++) {
                ProductSubCategoryDTO s = subs.get(i);
                sb.append(i + 1).append(") ").append(s.getName());
                if (s.getExample() != null && !s.getExample().isBlank()) {
                    sb.append(" — ").append(s.getExample());
                }
                sb.append('\n');
            }
            sb.append("Reply with a number or subcategory name.");
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("Subcategory list failed: {}", e.getMessage());
            return "Reply with the subcategory name for \"" + category + "\".";
        }
    }

    private String resolveCategory(String answer) {
        try {
            List<ProductCategoryDTO> cats = productService.getCategories();
            Integer idx = parseListIndex(answer, cats != null ? cats.size() : 0);
            if (idx != null && cats != null) {
                return cats.get(idx).getName();
            }
            if (cats != null) {
                for (ProductCategoryDTO c : cats) {
                    if (c.getName() != null && c.getName().equalsIgnoreCase(answer.trim())) {
                        return c.getName();
                    }
                }
                for (ProductCategoryDTO c : cats) {
                    if (c.getName() != null
                            && c.getName().toLowerCase(Locale.ROOT).contains(answer.trim().toLowerCase(Locale.ROOT))) {
                        return c.getName();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("resolveCategory fallback: {}", e.getMessage());
        }
        if (answer != null && answer.trim().length() >= 2 && answer.trim().length() <= 80
                && TaalrInputValidation.parseYesNo(answer) == null) {
            return answer.trim();
        }
        return null;
    }

    private String resolveSubcategory(String category, String answer) {
        try {
            List<ProductSubCategoryDTO> subs = productService.getSubcategories(category);
            Integer idx = parseListIndex(answer, subs != null ? subs.size() : 0);
            if (idx != null && subs != null) {
                return subs.get(idx).getName();
            }
            if (subs != null) {
                for (ProductSubCategoryDTO s : subs) {
                    if (s.getName() != null && s.getName().equalsIgnoreCase(answer.trim())) {
                        return s.getName();
                    }
                }
                for (ProductSubCategoryDTO s : subs) {
                    if (s.getName() != null
                            && s.getName().toLowerCase(Locale.ROOT).contains(answer.trim().toLowerCase(Locale.ROOT))) {
                        return s.getName();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("resolveSubcategory fallback: {}", e.getMessage());
        }
        if (answer != null && answer.trim().length() >= 2 && answer.trim().length() <= 100
                && TaalrInputValidation.parseYesNo(answer) == null) {
            return answer.trim();
        }
        return null;
    }

    private static Integer parseListIndex(String answer, int size) {
        if (answer == null || size <= 0) {
            return null;
        }
        String digits = answer.trim().replaceAll("[^0-9]", "");
        if (digits.isBlank() || digits.length() > 3) {
            return null;
        }
        try {
            int n = Integer.parseInt(digits);
            if (n >= 1 && n <= size) {
                return n - 1;
            }
        } catch (NumberFormatException ignored) {
            // ignore
        }
        return null;
    }

    private static SalesTaxBusinessType parseBusinessType(String answer) {
        if (answer == null || answer.isBlank()) {
            return null;
        }
        String t = answer.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        Integer idx = parseListIndex(answer, BusinessTypeCatalog.allTypes().size());
        if (idx != null) {
            String id = String.valueOf(BusinessTypeCatalog.allTypes().get(idx).get("id"));
            try {
                return SalesTaxBusinessType.valueOf(id);
            } catch (Exception ignored) {
                // fall through
            }
        }
        try {
            return SalesTaxBusinessType.valueOf(t);
        } catch (Exception ignored) {
            // match display names / keywords
        }
        if (t.contains("pet")) return SalesTaxBusinessType.pet_store;
        if (t.contains("salon") || t.contains("spa")) return SalesTaxBusinessType.salon;
        if (t.contains("restaurant") || t.contains("food")) return SalesTaxBusinessType.restaurant;
        if (t.contains("contract") || t.contains("trade")) return SalesTaxBusinessType.contractor;
        if (t.contains("retail") || t.contains("store")) return SalesTaxBusinessType.retail;
        if (t.contains("software") || t.contains("saas") || t.contains("digital")) return SalesTaxBusinessType.software;
        if (t.contains("health") || t.contains("medical")) return SalesTaxBusinessType.healthcare;
        if (t.contains("educat") || t.contains("train")) return SalesTaxBusinessType.education;
        if (t.contains("agri") || t.contains("farm")) return SalesTaxBusinessType.agriculture;
        if (t.contains("other") || t.contains("mixed")) return SalesTaxBusinessType.other;
        return null;
    }

    private static String formatReviewPreview(TaalrSalesTaxDraft d) {
        StringBuilder sb = new StringBuilder("Sales tax filing draft ready:\n\n");
        sb.append("• State: ").append(d.getStateCode()).append('\n');
        sb.append("• Category: ").append(d.getCategory()).append(" / ").append(d.getSubcategory()).append('\n');
        sb.append("• Taxable sales: $").append(String.format(Locale.US, "%.2f",
                d.getTaxableAmount() != null ? d.getTaxableAmount() : 0)).append('\n');
        sb.append("• Exempt sales: $").append(String.format(Locale.US, "%.2f",
                d.getExemptAmount() != null ? d.getExemptAmount() : 0)).append('\n');
        if (notBlank(d.getCity()) || notBlank(d.getPostalCode())) {
            sb.append("• Location: ")
                    .append(notBlank(d.getCity()) ? d.getCity() : "default city")
                    .append(notBlank(d.getPostalCode()) ? (", " + d.getPostalCode()) : "")
                    .append('\n');
        } else {
            sb.append("• Location: state default for rate estimate\n");
        }
        if (Boolean.TRUE.equals(d.getRegistrationRequired())) {
            sb.append("• Note: state registration may be required (extra fee on dashboard)\n");
        }
        sb.append("\nI'll save a DRAFT for the current quarter. Payment & final submit stay on the Sales Tax dashboard.");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String formatSuccess(TaalrSalesTaxDraft draft, Map<String, Object> estimate,
            Map<String, Object> review) {
        StringBuilder sb = new StringBuilder("Sales tax draft saved.\n\n");
        sb.append("• Filing ID: ").append(draft.getFilingId()).append('\n');
        sb.append("• State: ").append(draft.getStateCode()).append('\n');
        sb.append("• Category: ").append(draft.getCategory()).append(" / ").append(draft.getSubcategory()).append('\n');

        Object summaryObj = review.get("summary");
        if (summaryObj instanceof Map<?, ?> summary) {
            sb.append("• Period: ").append(blank(summary.get("filingPeriodLabel"))).append('\n');
            sb.append("• Due: ").append(blank(summary.get("dueDateLabel"))).append('\n');
            sb.append("• Taxable: $").append(fmtMoney(summary.get("taxableAmount"))).append('\n');
            sb.append("• Exempt: $").append(fmtMoney(summary.get("exemptAmount"))).append('\n');
            sb.append("• Est. tax owed: $").append(fmtMoney(summary.get("taxOwed"))).append('\n');
        } else {
            sb.append("• Est. tax owed: $").append(fmtMoney(estimate.get("taxOwed"))).append('\n');
        }
        sb.append("• Total to charge (fees + tax): $").append(fmtMoney(review.get("totalCharged"))).append('\n');
        if (estimate.get("savingsAmount") != null) {
            sb.append("• Est. exemption savings: $").append(fmtMoney(estimate.get("savingsAmount"))).append('\n');
        }

        sb.append("\nNext: open Numbrics → Sales Tax to complete payment and submit filing #")
                .append(draft.getFilingId()).append('.');
        sb.append("\n(Chat won't charge your card — that stays on the dashboard.)");
        sb.append("\nSay \"sales tax status\" anytime to check progress.");
        return sb.toString();
    }

    private void persistDraft(TaalrActionRequest request, TaalrActionSessionEntity existing, TaalrSessionContext ctx) {
        if (existing != null) {
            sessionService.updateSession(existing, TaalrPendingAction.SALES_TAX_DRAFT, ctx);
        } else {
            sessionService.saveSession(request, TaalrPendingAction.SALES_TAX_DRAFT, ctx);
        }
    }

    private static boolean notBlank(String v) {
        return v != null && !v.isBlank();
    }

    private static String blank(Object v) {
        return v == null || String.valueOf(v).isBlank() ? "—" : String.valueOf(v);
    }

    private static String fmtMoney(Object v) {
        if (v == null) {
            return "0.00";
        }
        if (v instanceof Number n) {
            return String.format(Locale.US, "%.2f", n.doubleValue());
        }
        try {
            return String.format(Locale.US, "%.2f", Double.parseDouble(String.valueOf(v)));
        } catch (Exception e) {
            return String.valueOf(v);
        }
    }

    private static String safe(String msg) {
        return msg != null ? msg : "unknown error";
    }
}
