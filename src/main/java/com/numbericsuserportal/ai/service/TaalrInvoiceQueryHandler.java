package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrInvoiceDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.invoice.dto.InvoiceSearch;
import com.numbericsuserportal.invoice.dto.SendInvoiceRequestDto;
import com.numbericsuserportal.invoice.dto.SendInvoiceResponseDto;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.entity.InvoiceSendLog;
import com.numbericsuserportal.invoice.impl.InvoiceSendServiceImpl;
import com.numbericsuserportal.invoice.repo.InvoiceSendLogRepo;
import com.numbericsuserportal.invoice.service.InvoiceAndTaxService;
import com.numbericsuserportal.invoice.service.InvoiceSendService;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TaalrInvoiceQueryHandler {

    private static final int LIST_LIMIT = 10;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final Pattern INV_NUM_PATTERN = Pattern.compile(
            "\\b(INV-[A-Za-z0-9\\-]+)\\b", Pattern.CASE_INSENSITIVE);

    @Autowired
    private InvoiceAndTaxService invoiceAndTaxService;

    @Autowired
    private InvoiceSendService invoiceSendService;

    @Autowired
    private InvoiceSendLogRepo invoiceSendLogRepo;

    @Autowired
    private TaalrActionSessionService sessionService;

    public TaalrActionResult handleList(User user, TaalrIntentParseResult parsed) {
        TaalrInvoiceDraft hint = parsed.getInvoice() != null ? parsed.getInvoice() : new TaalrInvoiceDraft();
        if (hasInvoiceIdentifier(hint)) {
            return handleSingleStatus(user, hint);
        }

        String filter = normalizeFilter(parsed.getStatusFilter());
        InvoiceSearch search = new InvoiceSearch();
        search.setPageNumber(1);
        search.setPageSize(50);

        Page<InvoiceAndTaxEntity> page = invoiceAndTaxService.searchInvoice(search, user);
        List<InvoiceAndTaxEntity> all = page.getContent();
        List<InvoiceAndTaxEntity> filtered = filterInvoices(all, filter);

        if (filtered.isEmpty()) {
            return TaalrActionResult.handled("You have no " + filterLabel(filter) + "invoices right now.");
        }

        int total = filtered.size();
        List<InvoiceAndTaxEntity> shown = filtered.subList(0, Math.min(LIST_LIMIT, total));
        StringBuilder sb = new StringBuilder();
        sb.append("Your invoices");
        if (!"ALL".equals(filter)) {
            sb.append(" (").append(filterLabel(filter).trim()).append(")");
        }
        sb.append(" — showing ").append(shown.size());
        if (total > shown.size()) {
            sb.append(" of ").append(total);
        }
        sb.append(":\n\n");

        for (InvoiceAndTaxEntity inv : shown) {
            sb.append(formatInvoiceLine(inv)).append('\n');
        }

        sb.append('\n').append(summarizeCounts(all));
        sb.append("\n\nAsk about a specific invoice (e.g. \"status of INV-001\") or say \"resend INV-001\".");
        return TaalrActionResult.handled(sb.toString());
    }

    public TaalrActionResult handleResend(TaalrActionRequest request, User user, TaalrIntentParseResult parsed) {
        TaalrInvoiceDraft hint = parsed.getInvoice() != null ? parsed.getInvoice() : new TaalrInvoiceDraft();
        extractInvoiceNumFromText(request.getMessage(), hint);

        if (!hasInvoiceIdentifier(hint)) {
            return TaalrActionResult.handled(
                    "Which invoice should I resend? Please provide the invoice number (e.g. INV-ABC123) or invoice ID.");
        }

        Optional<InvoiceAndTaxEntity> invoiceOpt = resolveInvoice(user, hint);
        if (invoiceOpt.isEmpty()) {
            return TaalrActionResult.handled("I couldn't find that invoice. Check the invoice number and try again.");
        }

        InvoiceAndTaxEntity invoice = invoiceOpt.get();
        if (isPaid(invoice)) {
            return TaalrActionResult.handled(
                    "Invoice " + safeNum(invoice) + " is already marked PAID — no reminder needed.");
        }

        TaalrInvoiceDraft draft = buildResendDraft(invoice, hint, parsed);

        TaalrSessionContext ctx = new TaalrSessionContext();
        ctx.setCreatedInvoiceId(invoice.getId());
        ctx.setInvoiceDraft(draft);
        sessionService.saveSession(request, TaalrPendingAction.INVOICE_RESEND_CONFIRM, ctx);

        if (draft.getRecipientPhoneOrEmail() == null || draft.getRecipientPhoneOrEmail().isBlank()) {
            return TaalrActionResult.handled(
                    "Where should I resend " + safeNum(invoice) + "? Please provide an email or phone number.");
        }

        return TaalrActionResult.handled(formatResendPreview(invoice, draft));
    }

    public TaalrActionResult continueResendDraft(TaalrActionRequest request, User user,
            TaalrActionSessionEntity session, String rawMessage) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        TaalrInvoiceDraft draft = ctx.getInvoiceDraft();
        if (draft == null) {
            draft = new TaalrInvoiceDraft();
            ctx.setInvoiceDraft(draft);
        }

        if (rawMessage != null && !rawMessage.isBlank()) {
            if (rawMessage.contains("@")) {
                draft.setRecipientPhoneOrEmail(rawMessage.trim());
                draft.setChannel(InvoiceSendServiceImpl.CHANNEL_EMAIL);
            } else {
                draft.setRecipientPhoneOrEmail(rawMessage.trim());
                draft.setChannel(InvoiceSendServiceImpl.CHANNEL_WHATSAPP);
            }
        }

        if (draft.getRecipientPhoneOrEmail() == null || draft.getRecipientPhoneOrEmail().isBlank()) {
            return TaalrActionResult.handled("Please provide the email or phone number to resend the invoice to.");
        }

        sessionService.updateSession(session, TaalrPendingAction.INVOICE_RESEND_CONFIRM, ctx);
        Long invoiceId = ctx.getCreatedInvoiceId();
        InvoiceAndTaxEntity invoice = invoiceId != null
                ? invoiceAndTaxService.getInvoiceDetail(invoiceId, user)
                : null;
        if (invoice == null || invoice.getId() == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please ask to resend the invoice again.");
        }
        return TaalrActionResult.handled(formatResendPreview(invoice, draft));
    }

    public TaalrActionResult confirmResend(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        Long invoiceId = ctx.getCreatedInvoiceId();
        TaalrInvoiceDraft draft = ctx.getInvoiceDraft();

        if (invoiceId == null || draft == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please ask to resend the invoice again.");
        }

        SendInvoiceRequestDto sendReq = new SendInvoiceRequestDto();
        sendReq.setInvoiceId(invoiceId);
        sendReq.setChannel(resolveChannel(draft));
        sendReq.setRecipientPhoneOrEmail(draft.getRecipientPhoneOrEmail());

        SendInvoiceResponseDto result = invoiceSendService.sendInvoice(sendReq, user);
        sessionService.clearSession(request);

        if (result.isSuccess()) {
            return TaalrActionResult.handled(
                    "Reminder sent for invoice via " + sendReq.getChannel()
                            + " to " + sendReq.getRecipientPhoneOrEmail() + ".");
        }
        return TaalrActionResult.handled(
                result.getMessage() != null ? result.getMessage() : "Failed to resend invoice. Try from the dashboard.");
    }

    public TaalrActionResult cancel(TaalrActionRequest request) {
        sessionService.clearSession(request);
        return TaalrActionResult.handled("Resend cancelled.");
    }

    private TaalrActionResult handleSingleStatus(User user, TaalrInvoiceDraft hint) {
        Optional<InvoiceAndTaxEntity> invoiceOpt = resolveInvoice(user, hint);
        if (invoiceOpt.isEmpty()) {
            return TaalrActionResult.handled("Invoice not found. Check the invoice number or ID.");
        }

        InvoiceAndTaxEntity inv = invoiceOpt.get();
        StringBuilder sb = new StringBuilder();
        sb.append("Invoice ").append(safeNum(inv)).append(":\n\n");
        sb.append("• Customer: ").append(blankDash(inv.getCustomerName())).append('\n');
        sb.append("• Amount: ").append(formatMoney(inv.getTaxableAmount())).append('\n');
        sb.append("• Status: ").append(blankDash(inv.getInvoiceStatus())).append('\n');
        sb.append("• Due: ").append(formatDate(inv.getInvoiceDueDate())).append('\n');

        Optional<InvoiceSendLog> lastSend = getLastSendLog(inv.getId());
        if (lastSend.isPresent()) {
            InvoiceSendLog log = lastSend.get();
            sb.append("• Last sent: ").append(log.getChannel()).append(" to ")
                    .append(log.getSentTo());
            if (log.getSentAt() != null) {
                sb.append(" on ").append(new java.text.SimpleDateFormat("MMM d, yyyy").format(log.getSentAt()));
            }
            sb.append('\n');
        } else {
            sb.append("• Last sent: never\n");
        }

        if (!isPaid(inv)) {
            sb.append("\nSay \"resend ").append(safeNum(inv)).append("\" to send a payment reminder.");
        }
        return TaalrActionResult.handled(sb.toString());
    }

    private TaalrInvoiceDraft buildResendDraft(InvoiceAndTaxEntity invoice, TaalrInvoiceDraft hint,
            TaalrIntentParseResult parsed) {
        TaalrInvoiceDraft draft = new TaalrInvoiceDraft();
        draft.setInvoiceId(invoice.getId());
        draft.setInvoiceNum(invoice.getInvoiceNum());
        draft.setCustomerName(invoice.getCustomerName());
        draft.setAmount(invoice.getTaxableAmount());

        if (hint.getRecipientPhoneOrEmail() != null && !hint.getRecipientPhoneOrEmail().isBlank()) {
            draft.setRecipientPhoneOrEmail(hint.getRecipientPhoneOrEmail().trim());
            draft.setChannel(hint.getChannel() != null ? hint.getChannel() : inferChannel(hint.getRecipientPhoneOrEmail()));
        } else if (parsed.getInvoice() != null && parsed.getInvoice().getRecipientPhoneOrEmail() != null) {
            draft.setRecipientPhoneOrEmail(parsed.getInvoice().getRecipientPhoneOrEmail().trim());
            draft.setChannel(parsed.getInvoice().getChannel());
        } else {
            Optional<InvoiceSendLog> lastSend = getLastSendLog(invoice.getId());
            if (lastSend.isPresent()) {
                draft.setRecipientPhoneOrEmail(lastSend.get().getSentTo());
                draft.setChannel(lastSend.get().getChannel());
            } else if (invoice.getCustomerEmail() != null && !invoice.getCustomerEmail().isBlank()) {
                draft.setRecipientPhoneOrEmail(invoice.getCustomerEmail().trim());
                draft.setChannel(InvoiceSendServiceImpl.CHANNEL_EMAIL);
            } else if (invoice.getCustomerEmail() == null && invoice.getCustomerName() != null) {
                // no phone field on entity beyond customer - skip
            }
        }
        return draft;
    }

    private String formatResendPreview(InvoiceAndTaxEntity invoice, TaalrInvoiceDraft draft) {
        StringBuilder sb = new StringBuilder("Resend payment reminder:\n\n");
        sb.append("• Invoice: ").append(safeNum(invoice)).append('\n');
        sb.append("• Customer: ").append(blankDash(invoice.getCustomerName())).append('\n');
        sb.append("• Amount: ").append(formatMoney(invoice.getTaxableAmount())).append('\n');
        sb.append("• Channel: ").append(resolveChannel(draft)).append('\n');
        sb.append("• Send to: ").append(draft.getRecipientPhoneOrEmail()).append("\n\n");
        sb.append("Reply YES to resend, or NO to cancel.");
        return sb.toString();
    }

    private Optional<InvoiceAndTaxEntity> resolveInvoice(User user, TaalrInvoiceDraft hint) {
        if (hint.getInvoiceId() != null) {
            InvoiceAndTaxEntity e = invoiceAndTaxService.getInvoiceDetail(hint.getInvoiceId(), user);
            if (e.getId() != null) {
                return Optional.of(e);
            }
        }
        if (hint.getInvoiceNum() != null && !hint.getInvoiceNum().isBlank()) {
            InvoiceAndTaxEntity e = invoiceAndTaxService.getInvoiceDetailByInvoiceNumber(
                    hint.getInvoiceNum().trim(), user);
            if (e.getId() != null) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    private Optional<InvoiceSendLog> getLastSendLog(Long invoiceId) {
        Page<InvoiceSendLog> logs = invoiceSendLogRepo.findByInvoiceIdOrderBySentAtDesc(
                invoiceId, PageRequest.of(0, 1));
        if (logs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(logs.getContent().get(0));
    }

    private static void extractInvoiceNumFromText(String message, TaalrInvoiceDraft draft) {
        if (message == null || draft.getInvoiceNum() != null) {
            return;
        }
        Matcher m = INV_NUM_PATTERN.matcher(message);
        if (m.find()) {
            draft.setInvoiceNum(m.group(1).toUpperCase(Locale.ROOT));
        }
    }

    private static boolean hasInvoiceIdentifier(TaalrInvoiceDraft draft) {
        return draft.getInvoiceId() != null
                || (draft.getInvoiceNum() != null && !draft.getInvoiceNum().isBlank());
    }

    private static List<InvoiceAndTaxEntity> filterInvoices(List<InvoiceAndTaxEntity> invoices, String filter) {
        List<InvoiceAndTaxEntity> out = new ArrayList<>();
        for (InvoiceAndTaxEntity inv : invoices) {
            if (matchesFilter(inv, filter)) {
                out.add(inv);
            }
        }
        return out;
    }

    private static boolean matchesFilter(InvoiceAndTaxEntity inv, String filter) {
        if ("ALL".equals(filter)) {
            return true;
        }
        boolean paid = isPaid(inv);
        if ("PAID".equals(filter)) {
            return paid;
        }
        if ("UNPAID".equals(filter)) {
            return !paid;
        }
        if ("DRAFT".equals(filter)) {
            return inv.getInvoiceStatus() != null
                    && "DRAFT".equalsIgnoreCase(inv.getInvoiceStatus().trim());
        }
        return true;
    }

    private static String normalizeFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return "ALL";
        }
        String f = raw.trim().toUpperCase(Locale.ROOT);
        if (f.contains("UNPAID") || f.contains("OUTSTANDING") || f.contains("OVERDUE")) {
            return "UNPAID";
        }
        if (f.contains("PAID")) {
            return "PAID";
        }
        if (f.contains("DRAFT")) {
            return "DRAFT";
        }
        return "ALL";
    }

    private static String filterLabel(String filter) {
        return switch (filter) {
            case "UNPAID" -> "unpaid ";
            case "PAID" -> "paid ";
            case "DRAFT" -> "draft ";
            default -> "";
        };
    }

    private static String summarizeCounts(List<InvoiceAndTaxEntity> invoices) {
        int unpaid = 0;
        int paid = 0;
        int draft = 0;
        for (InvoiceAndTaxEntity inv : invoices) {
            if (isPaid(inv)) {
                paid++;
            } else {
                unpaid++;
            }
            if (inv.getInvoiceStatus() != null && "DRAFT".equalsIgnoreCase(inv.getInvoiceStatus().trim())) {
                draft++;
            }
        }
        return "Summary: " + unpaid + " unpaid | " + paid + " paid | " + draft + " draft";
    }

    private static String formatInvoiceLine(InvoiceAndTaxEntity inv) {
        return "• " + safeNum(inv) + " — " + blankDash(inv.getCustomerName())
                + " — " + formatMoney(inv.getTaxableAmount())
                + " — " + blankDash(inv.getInvoiceStatus())
                + " — due " + formatDate(inv.getInvoiceDueDate());
    }

    private static boolean isPaid(InvoiceAndTaxEntity inv) {
        return inv.getInvoiceStatus() != null && "PAID".equalsIgnoreCase(inv.getInvoiceStatus().trim());
    }

    private static String safeNum(InvoiceAndTaxEntity inv) {
        if (inv.getInvoiceNum() != null && !inv.getInvoiceNum().isBlank()) {
            return inv.getInvoiceNum().trim();
        }
        return "ID " + inv.getId();
    }

    private static String blankDash(String s) {
        return s != null && !s.isBlank() ? s.trim() : "—";
    }

    private static String formatMoney(Double amount) {
        if (amount == null) {
            return "—";
        }
        return "$" + String.format(Locale.US, "%.2f", amount);
    }

    private static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "—";
    }

    private static String resolveChannel(TaalrInvoiceDraft draft) {
        if (draft.getChannel() != null && !draft.getChannel().isBlank()) {
            String ch = draft.getChannel().trim().toUpperCase(Locale.ROOT);
            if (InvoiceSendServiceImpl.CHANNEL_EMAIL.equals(ch)) {
                return InvoiceSendServiceImpl.CHANNEL_EMAIL;
            }
            return InvoiceSendServiceImpl.CHANNEL_WHATSAPP;
        }
        return inferChannel(draft.getRecipientPhoneOrEmail());
    }

    private static String inferChannel(String recipient) {
        if (recipient != null && recipient.contains("@")) {
            return InvoiceSendServiceImpl.CHANNEL_EMAIL;
        }
        return InvoiceSendServiceImpl.CHANNEL_WHATSAPP;
    }
}
