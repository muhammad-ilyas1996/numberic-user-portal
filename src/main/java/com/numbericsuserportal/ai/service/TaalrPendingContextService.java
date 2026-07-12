package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrActionChannel;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrInvoiceDraft;
import com.numbericsuserportal.ai.action.dto.TaalrLlcDraft;
import com.numbericsuserportal.ai.action.dto.TaalrReceiptDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSalesTaxDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Surfaces in-progress automation (stored in DB {@code taalr_action_sessions}) to Claude guidance
 * and end-of-reply reminders.
 */
@Service
public class TaalrPendingContextService {

    private static final Pattern RESUME_PATTERN = Pattern.compile(
            ".*(continue|resume|finish|complete)\\s+(my\\s+)?(invoice|receipt|draft|reminder|llc|formation|sales\\s*tax|salestax).*",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private TaalrActionSessionService sessionService;

    public boolean isResumeMessage(String message) {
        return message != null && RESUME_PATTERN.matcher(message.trim()).matches();
    }

    public Optional<TaalrActionSessionEntity> findActiveSession(User user) {
        if (user == null || user.getUserId() == null) {
            return Optional.empty();
        }
        TaalrActionRequest req = TaalrActionRequest.builder()
                .channel(TaalrActionChannel.APP_CHAT)
                .userId(user.getUserId())
                .build();
        return sessionService.findActiveSession(req);
    }

    /** Injected into Claude system prompt so guidance stays aware of pending work. */
    public Optional<String> buildSystemContext(User user) {
        return findActiveSession(user).map(this::describeForClaude);
    }

    /** Appended after Claude guidance replies when automation is still in progress. */
    public Optional<String> buildGuidanceReminder(User user) {
        return findActiveSession(user).map(this::reminderFooter);
    }

    private String describeForClaude(TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        StringBuilder sb = new StringBuilder();
        sb.append("--- PENDING NUMBRICS ACTION (stored server-side, not yet completed) ---\n");
        sb.append(describePending(session.getPendingAction(), ctx));
        sb.append("\nIf the user asks for guidance, answer normally but you may briefly mention they can ");
        sb.append("say \"").append(resumePhrase(session.getPendingAction()))
                .append("\" to resume automation or \"cancel\" to discard.");
        return sb.toString();
    }

    private String reminderFooter(TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        return "Reminder: " + describePending(session.getPendingAction(), ctx)
                + " Reply \"" + resumePhrase(session.getPendingAction()) + "\" to resume, or \"cancel\" to discard.";
    }

    private static String resumePhrase(TaalrPendingAction action) {
        if (action == TaalrPendingAction.LLC_DRAFT || action == TaalrPendingAction.LLC_PREPARE_CONFIRM) {
            return "continue llc";
        }
        if (action == TaalrPendingAction.RECEIPT_SAVE_CONFIRM) {
            return "continue receipt";
        }
        if (action == TaalrPendingAction.SALES_TAX_DRAFT || action == TaalrPendingAction.SALES_TAX_REVIEW_CONFIRM) {
            return "continue sales tax";
        }
        return "continue invoice";
    }

    private String describePending(TaalrPendingAction action, TaalrSessionContext ctx) {
        if (action == null) {
            return "You have an unfinished task.";
        }
        return switch (action) {
            case INVOICE_DRAFT -> describeInvoiceDraft(ctx);
            case INVOICE_SEND_CONFIRM -> describeInvoiceSendConfirm(ctx);
            case INVOICE_RESEND_CONFIRM -> describeInvoiceResendConfirm(ctx);
            case RECEIPT_SAVE_CONFIRM -> describeReceiptConfirm(ctx);
            case LLC_DRAFT -> describeLlcDraft(ctx);
            case LLC_PREPARE_CONFIRM -> describeLlcPrepareConfirm(ctx);
            case SALES_TAX_DRAFT -> describeSalesTaxDraft(ctx);
            case SALES_TAX_REVIEW_CONFIRM -> describeSalesTaxReview(ctx);
        };
    }

    private static String describeInvoiceDraft(TaalrSessionContext ctx) {
        TaalrInvoiceDraft d = ctx.getInvoiceDraft();
        if (d == null) {
            return "You were creating an invoice (details incomplete).";
        }
        StringBuilder sb = new StringBuilder("You were creating an invoice");
        if (d.getCustomerName() != null && !d.getCustomerName().isBlank()) {
            sb.append(" for ").append(d.getCustomerName().trim());
        }
        if (d.getAmount() != null) {
            sb.append(" ($").append(String.format(Locale.US, "%.2f", d.getAmount())).append(')');
        }
        sb.append(" — not sent yet.");
        return sb.toString();
    }

    private static String describeInvoiceSendConfirm(TaalrSessionContext ctx) {
        TaalrInvoiceDraft d = ctx.getInvoiceDraft();
        if (d != null && d.getInvoiceNum() != null) {
            return "Invoice " + d.getInvoiceNum() + " is ready to send — waiting for your YES/NO.";
        }
        if (ctx.getCreatedInvoiceId() != null) {
            return "An invoice is ready to send (ID " + ctx.getCreatedInvoiceId() + ") — waiting for YES/NO.";
        }
        return "An invoice is ready to send — waiting for your YES/NO.";
    }

    private static String describeInvoiceResendConfirm(TaalrSessionContext ctx) {
        TaalrInvoiceDraft d = ctx.getInvoiceDraft();
        if (d != null && d.getInvoiceNum() != null) {
            return "Payment reminder for " + d.getInvoiceNum() + " is ready — waiting for YES/NO.";
        }
        return "A payment reminder is ready to send — waiting for YES/NO.";
    }

    private static String describeReceiptConfirm(TaalrSessionContext ctx) {
        TaalrReceiptDraft d = ctx.getReceiptDraft();
        if (d != null && d.getMerchantName() != null) {
            return "Receipt from " + d.getMerchantName() + " was scanned — waiting for YES to save.";
        }
        return "A receipt was scanned — waiting for YES to save.";
    }

    private static String describeLlcDraft(TaalrSessionContext ctx) {
        TaalrLlcDraft d = ctx.getLlcDraft();
        if (d == null) {
            return "You were setting up LLC formation details.";
        }
        StringBuilder sb = new StringBuilder("You were preparing LLC formation");
        if (d.getLlcName() != null && !d.getLlcName().isBlank()) {
            sb.append(" for ").append(d.getLlcName().trim());
        }
        if (d.getJurisdiction() != null && !d.getJurisdiction().isBlank()) {
            sb.append(" in ").append(d.getJurisdiction().trim().toUpperCase(Locale.ROOT));
        }
        sb.append(" — draft is incomplete.");
        return sb.toString();
    }

    private static String describeLlcPrepareConfirm(TaalrSessionContext ctx) {
        TaalrLlcDraft d = ctx.getLlcDraft();
        if (d != null && d.getLlcName() != null) {
            return "LLC \"" + d.getLlcName() + "\" is ready — waiting for YES to run name check + prepare.";
        }
        return "LLC draft is ready — waiting for YES to prepare filing.";
    }

    private static String describeSalesTaxDraft(TaalrSessionContext ctx) {
        TaalrSalesTaxDraft d = ctx.getSalesTaxDraft();
        if (d == null) {
            return "You were preparing a sales tax filing draft.";
        }
        StringBuilder sb = new StringBuilder("You were preparing a sales tax filing");
        if (d.getStateCode() != null && !d.getStateCode().isBlank()) {
            sb.append(" for ").append(d.getStateCode().trim().toUpperCase(Locale.ROOT));
        }
        sb.append(" — draft is incomplete.");
        return sb.toString();
    }

    private static String describeSalesTaxReview(TaalrSessionContext ctx) {
        TaalrSalesTaxDraft d = ctx.getSalesTaxDraft();
        if (d != null && d.getStateCode() != null) {
            return "Sales tax draft for " + d.getStateCode() + " is ready — waiting for YES to save.";
        }
        return "Sales tax draft is ready — waiting for YES to save.";
    }
}
