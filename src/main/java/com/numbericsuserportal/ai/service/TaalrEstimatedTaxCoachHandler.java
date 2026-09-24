package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrEstimatedTaxDraft;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.ai.i18n.TaalrEstimatedTaxMessages;
import com.numbericsuserportal.ai.util.TaalrInputValidation;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phase 1 estimated-tax coach: collect inputs → federal + SE estimate with citations (EN/ES/HT).
 */
@Service
@Slf4j
public class TaalrEstimatedTaxCoachHandler {

    private static final Pattern AMOUNT = Pattern.compile(
            "[$]?\\s*([0-9]{1,3}(?:,[0-9]{3})+(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)");

    @Autowired
    private TaalrActionSessionService sessionService;

    public TaalrActionResult handleStartOrContinue(TaalrActionRequest request, User user,
            TaalrIntentParseResult parsed, TaalrActionSessionEntity existingSession, String rawMessage) {
        TaalrSessionContext ctx = existingSession != null
                ? sessionService.loadContext(existingSession)
                : new TaalrSessionContext();
        if (ctx.getEstimatedTaxDraft() == null) {
            ctx.setEstimatedTaxDraft(new TaalrEstimatedTaxDraft());
        }
        TaalrEstimatedTaxDraft draft = ctx.getEstimatedTaxDraft();
        if (draft.getTaxYear() == null) {
            draft.setTaxYear(java.time.LocalDate.now().getYear());
        }
        mergeFromParsed(draft, parsed != null ? parsed.getEstimatedTax() : null);
        // Only sniff language from the opening message — not from later field answers like "English"
        if (existingSession == null) {
            detectLanguageFromMessage(draft, rawMessage);
        }

        if (rawMessage != null && !rawMessage.isBlank()) {
            if (wantsEscalation(rawMessage)) {
                persist(request, existingSession, ctx);
                return TaalrActionResult.handled(escalationReply(draft.getLanguage())
                        + "\n\n" + questionFor(draft, missingField(draft)));
            }
            if (TaalrInputValidation.isConfusion(rawMessage)) {
                persist(request, existingSession, ctx);
                String missing = missingField(draft);
                return TaalrActionResult.handled(questionFor(draft, missing));
            }
            String missingBefore = missingField(draft);
            if (missingBefore != null) {
                String err = applyAnswer(draft, missingBefore, rawMessage.trim());
                if (err != null) {
                    persist(request, existingSession, ctx);
                    return TaalrActionResult.handled(err);
                }
            }
        }

        String missing = missingField(draft);
        if (missing != null) {
            persist(request, existingSession, ctx);
            String prefix = "";
            if (existingSession == null && "filingStatus".equals(missing) && draft.getLanguage() != null) {
                prefix = TaalrEstimatedTaxMessages.intro(draft.getLanguage());
            }
            return TaalrActionResult.handled(prefix + questionFor(draft, missing));
        }

        TaalrEstimatedTaxCalculator.EstimateResult estimate = TaalrEstimatedTaxCalculator.compute(draft);
        String reply = TaalrEstimatedTaxCalculator.formatReply(draft, estimate);
        sessionService.clearSession(request);
        return TaalrActionResult.handled(reply);
    }

    public TaalrActionResult promptContinue(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        if (ctx.getEstimatedTaxDraft() == null) {
            ctx.setEstimatedTaxDraft(new TaalrEstimatedTaxDraft());
        }
        String missing = missingField(ctx.getEstimatedTaxDraft());
        return TaalrActionResult.handled(questionFor(ctx.getEstimatedTaxDraft(), missing));
    }

    public TaalrActionResult cancel(TaalrActionRequest request) {
        TaalrActionSessionEntity session = sessionService.findActiveSession(request).orElse(null);
        String lang = "EN";
        if (session != null) {
            TaalrSessionContext ctx = sessionService.loadContext(session);
            if (ctx.getEstimatedTaxDraft() != null && ctx.getEstimatedTaxDraft().getLanguage() != null) {
                lang = ctx.getEstimatedTaxDraft().getLanguage();
            }
        }
        sessionService.clearSession(request);
        return TaalrActionResult.handled(TaalrEstimatedTaxMessages.cancelled(lang));
    }

    private void persist(TaalrActionRequest request, TaalrActionSessionEntity existing, TaalrSessionContext ctx) {
        ctx.setPendingAction(TaalrPendingAction.ESTIMATED_TAX_DRAFT);
        if (existing != null) {
            sessionService.updateSession(existing, TaalrPendingAction.ESTIMATED_TAX_DRAFT, ctx);
        } else {
            sessionService.saveSession(request, TaalrPendingAction.ESTIMATED_TAX_DRAFT, ctx);
        }
    }

    static String missingField(TaalrEstimatedTaxDraft draft) {
        if (draft.getLanguage() == null || draft.getLanguage().isBlank()) {
            return "language";
        }
        if (draft.getFilingStatus() == null || draft.getFilingStatus().isBlank()) {
            return "filingStatus";
        }
        if (draft.getStateCode() == null || draft.getStateCode().isBlank()) {
            return "stateCode";
        }
        if (draft.getTaxYear() == null) {
            return "taxYear";
        }
        if (draft.getYtdIncome() == null) {
            return "ytdIncome";
        }
        if (draft.getDeductibleExpenses() == null) {
            return "deductibleExpenses";
        }
        if (draft.getPriorPayments() == null) {
            return "priorPayments";
        }
        if (draft.getWithholding() == null) {
            return "withholding";
        }
        return null;
    }

    private static String questionFor(TaalrEstimatedTaxDraft draft, String field) {
        if (field == null) {
            return "";
        }
        if ("language".equals(field)) {
            return TaalrEstimatedTaxMessages.askLanguage();
        }
        return TaalrEstimatedTaxMessages.ask(draft.getLanguage(), field);
    }

    private static String applyAnswer(TaalrEstimatedTaxDraft draft, String field, String raw) {
        return switch (field) {
            case "language" -> {
                String lang = detectLangToken(raw);
                if (lang == null) {
                    yield TaalrEstimatedTaxMessages.askLanguage();
                }
                draft.setLanguage(lang);
                yield null;
            }
            case "filingStatus" -> {
                String status = parseFilingStatus(raw);
                if (status == null) {
                    yield TaalrEstimatedTaxMessages.invalid(draft.getLanguage(), field);
                }
                draft.setFilingStatus(status);
                yield null;
            }
            case "stateCode" -> {
                String state = TaalrInputValidation.parseUsState(raw);
                if (state == null) {
                    // accept bare 2-letter
                    Matcher m = Pattern.compile("\\b([A-Za-z]{2})\\b").matcher(raw.trim());
                    if (m.find()) {
                        state = m.group(1).toUpperCase(Locale.ROOT);
                    }
                }
                if (state == null || state.length() != 2) {
                    yield TaalrEstimatedTaxMessages.invalid(draft.getLanguage(), field);
                }
                draft.setStateCode(state);
                yield null;
            }
            case "taxYear" -> {
                Integer y = parseYear(raw);
                if (y == null) {
                    yield TaalrEstimatedTaxMessages.invalid(draft.getLanguage(), field);
                }
                draft.setTaxYear(y);
                yield null;
            }
            case "ytdIncome" -> {
                Double amt = parseAmount(raw);
                if (amt == null || amt < 0) {
                    yield TaalrEstimatedTaxMessages.invalid(draft.getLanguage(), field);
                }
                draft.setYtdIncome(amt);
                yield null;
            }
            case "deductibleExpenses" -> {
                Double amt = parseAmount(raw);
                if (amt == null || amt < 0) {
                    yield TaalrEstimatedTaxMessages.invalid(draft.getLanguage(), field);
                }
                draft.setDeductibleExpenses(amt);
                yield null;
            }
            case "priorPayments" -> {
                Double amt = parseAmount(raw);
                if (amt == null || amt < 0) {
                    yield TaalrEstimatedTaxMessages.invalid(draft.getLanguage(), field);
                }
                draft.setPriorPayments(amt);
                yield null;
            }
            case "withholding" -> {
                Double amt = parseAmount(raw);
                if (amt == null || amt < 0) {
                    yield TaalrEstimatedTaxMessages.invalid(draft.getLanguage(), field);
                }
                draft.setWithholding(amt);
                yield null;
            }
            default -> null;
        };
    }

    /** Soft escalation when user asks for a human / CPA (identical safety path in all languages). */
    public static boolean wantsEscalation(String raw) {
        if (raw == null) {
            return false;
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        return lower.contains("human") || lower.contains("agent") || lower.contains("cpa")
                || lower.contains("accountant") || lower.contains("tax pro")
                || lower.contains("hablar con") || lower.contains("asesor")
                || lower.contains("moun") || lower.contains("kontab");
    }

    public static String escalationReply(String language) {
        return switch (TaalrEstimatedTaxMessages.normalizeLang(language)) {
            case "ES" -> "Puedo dar una estimación educativa, pero no asesoría fiscal personalizada. "
                    + "Para revisión profesional, use el dashboard de Numbrics o consulte a un CPA autorizado. "
                    + "Responda \"cancel\" para salir, o continúe con los datos.";
            case "HT" -> "Mwen ka bay yon estime edikatif, men pa konsèy taks pèsonalize. "
                    + "Pou revizyon pwofesyonèl, itilize tablodbò Numbrics oswa pale ak yon CPA. "
                    + "Reponn \"cancel\" pou sòti, oswa kontinye ak done yo.";
            default -> "I can provide an educational estimate, but not personalized tax advice. "
                    + "For a professional review, use the Numbrics dashboard or consult a licensed CPA. "
                    + "Reply \"cancel\" to exit, or continue with your numbers.";
        };
    }

    private static void mergeFromParsed(TaalrEstimatedTaxDraft draft, TaalrEstimatedTaxDraft from) {
        if (from == null) {
            return;
        }
        if (blank(draft.getLanguage()) && notBlank(from.getLanguage())) {
            draft.setLanguage(TaalrEstimatedTaxMessages.normalizeLang(from.getLanguage()));
        }
        if (blank(draft.getFilingStatus()) && notBlank(from.getFilingStatus())) {
            String s = parseFilingStatus(from.getFilingStatus());
            if (s != null) {
                draft.setFilingStatus(s);
            }
        }
        if (blank(draft.getStateCode()) && notBlank(from.getStateCode())) {
            draft.setStateCode(from.getStateCode().trim().toUpperCase(Locale.ROOT));
        }
        if (draft.getTaxYear() == null && from.getTaxYear() != null) {
            draft.setTaxYear(from.getTaxYear());
        }
        if (draft.getYtdIncome() == null && from.getYtdIncome() != null) {
            draft.setYtdIncome(from.getYtdIncome());
        }
        if (draft.getDeductibleExpenses() == null && from.getDeductibleExpenses() != null) {
            draft.setDeductibleExpenses(from.getDeductibleExpenses());
        }
        if (draft.getPriorPayments() == null && from.getPriorPayments() != null) {
            draft.setPriorPayments(from.getPriorPayments());
        }
        if (draft.getWithholding() == null && from.getWithholding() != null) {
            draft.setWithholding(from.getWithholding());
        }
    }

    private static void detectLanguageFromMessage(TaalrEstimatedTaxDraft draft, String raw) {
        if (draft.getLanguage() != null || raw == null) {
            return;
        }
        String lang = detectLangToken(raw);
        if (lang != null) {
            draft.setLanguage(lang);
        }
    }

    private static String detectLangToken(String raw) {
        if (raw == null) {
            return null;
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("español") || lower.contains("espanol") || lower.contains("spanish")
                || lower.trim().equals("es")) {
            return "ES";
        }
        if (lower.contains("kreyòl") || lower.contains("kreyol") || lower.contains("creole")
                || lower.contains("haitian") || lower.equals("ht")) {
            return "HT";
        }
        if (lower.contains("english") || lower.equals("en") || lower.equals("eng")) {
            return "EN";
        }
        // Spanish / Creole cues in full sentences
        if (lower.contains("impuestos estimados") || lower.contains("estado civil")
                || lower.contains("cuánto") || lower.contains("cuanto")) {
            return "ES";
        }
        if (lower.contains("enpo estime") || lower.contains("konbyen") || lower.contains("mwen bezwen")) {
            return "HT";
        }
        return null;
    }

    private static String parseFilingStatus(String raw) {
        if (raw == null) {
            return null;
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("joint") || lower.contains("mfj") || lower.contains("conjunto")
                || lower.contains("ansanm") || lower.contains("married filing jointly")) {
            return "MFJ";
        }
        if (lower.contains("separat") || lower.contains("mfs") || lower.contains("por separado")) {
            return "MFS";
        }
        if (lower.contains("head") || lower.contains("hoh") || lower.contains("cabeza")
                || lower.contains("chèf") || lower.contains("chef fanmi")) {
            return "HOH";
        }
        if (lower.contains("widow") || lower.contains("qw") || lower.contains("qualifying")) {
            return "QW";
        }
        if (lower.contains("single") || lower.contains("soltero") || lower.contains("selibat")
                || lower.equals("s")) {
            return "SINGLE";
        }
        return null;
    }

    private static Integer parseYear(String raw) {
        Matcher m = Pattern.compile("(20[2-3][0-9])").matcher(raw);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        // bare year like 25 → 2025
        Matcher m2 = Pattern.compile("\\b([2-3][0-9])\\b").matcher(raw.trim());
        if (m2.matches()) {
            return 2000 + Integer.parseInt(m2.group(1));
        }
        // default if user says "this year"
        if (raw.toLowerCase(Locale.ROOT).contains("this year")
                || raw.toLowerCase(Locale.ROOT).contains("este año")
                || raw.toLowerCase(Locale.ROOT).contains("ane sa")) {
            return LocalDate.now().getYear();
        }
        return null;
    }

    private static Double parseAmount(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim().toLowerCase(Locale.ROOT);
        if (t.equals("0") || t.equals("none") || t.equals("n/a") || t.equals("na")
                || t.equals("ninguno") || t.equals("ninguna") || t.equals("cero")
                || t.equals("anyen") || t.equals("zero")) {
            return 0.0;
        }
        Matcher m = AMOUNT.matcher(raw.replace(",", ""));
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(",", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean notBlank(String s) {
        return !blank(s);
    }
}
