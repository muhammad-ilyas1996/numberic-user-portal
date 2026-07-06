package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrActionChannel;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrInvoiceDraft;
import com.numbericsuserportal.ai.action.dto.TaalrReceiptDraft;
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
            ".*(continue|resume|finish|complete)\\s+(my\\s+)?(invoice|receipt|draft|reminder).*",
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
        sb.append("say \"continue invoice\" to resume automation or \"cancel\" to discard.");
        return sb.toString();
    }

    private String reminderFooter(TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        return "Reminder: " + describePending(session.getPendingAction(), ctx)
                + " Reply \"continue invoice\" to resume, or \"cancel\" to discard.";
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
}
