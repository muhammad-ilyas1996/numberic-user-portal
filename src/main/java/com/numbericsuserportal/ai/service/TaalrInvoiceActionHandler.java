package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrIntent;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrInvoiceDraft;
import com.numbericsuserportal.ai.action.dto.TaalrInvoiceLineItem;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.ai.util.TaalrInputValidation;
import com.numbericsuserportal.invoice.dto.InvoiceAndTaxDTO;
import com.numbericsuserportal.invoice.dto.SendInvoiceRequestDto;
import com.numbericsuserportal.invoice.dto.SendInvoiceResponseDto;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.impl.InvoiceSendServiceImpl;
import com.numbericsuserportal.invoice.service.InvoiceAndTaxService;
import com.numbericsuserportal.invoice.service.InvoiceSendService;
import com.numbericsuserportal.invoiceproduct.dto.InvoiceProductDTO;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class TaalrInvoiceActionHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @Autowired
    private InvoiceAndTaxService invoiceAndTaxService;

    @Autowired
    private InvoiceSendService invoiceSendService;

    @Autowired
    private TaalrActionSessionService sessionService;

    public TaalrActionResult handleNewIntent(TaalrActionRequest request, User user,
            TaalrIntentParseResult parsed, TaalrActionSessionEntity existingSession) {
        TaalrInvoiceDraft incoming = parsed.getInvoice() != null ? parsed.getInvoice() : new TaalrInvoiceDraft();
        TaalrSessionContext ctx = existingSession != null
                ? sessionService.loadContext(existingSession)
                : new TaalrSessionContext();
        if (ctx.getInvoiceDraft() == null) {
            ctx.setInvoiceDraft(new TaalrInvoiceDraft());
        }

        mergeInvoiceDraft(ctx.getInvoiceDraft(), incoming);
        return nextInvoiceStep(request, user, ctx, existingSession, null);
    }

    public TaalrActionResult continueDraft(TaalrActionRequest request, User user,
            TaalrActionSessionEntity session, TaalrIntentParseResult parsed, String rawMessage) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        if (ctx.getInvoiceDraft() == null) {
            ctx.setInvoiceDraft(new TaalrInvoiceDraft());
        }
        if (parsed.getInvoice() != null) {
            mergeInvoiceDraft(ctx.getInvoiceDraft(), parsed.getInvoice());
        }

        String missingBefore = findMissingRequiredField(ctx.getInvoiceDraft());
        if (rawMessage != null && !rawMessage.isBlank()) {
            if (TaalrInputValidation.isConfusion(rawMessage)) {
                sessionService.updateSession(session, TaalrPendingAction.INVOICE_DRAFT, ctx);
                return TaalrActionResult.handled(
                        "No problem — let's keep it simple.\n\n" + questionForMissing(missingBefore, ctx.getInvoiceDraft()));
            }
            if (missingBefore != null && (parsed.getIntent() == TaalrIntent.CHAT
                    || parsed.getIntent() == TaalrIntent.INVOICE
                    || parsed.getIntent() == TaalrIntent.CONFIRM_YES
                    || parsed.getIntent() == TaalrIntent.CONFIRM_NO)) {
                String err = applyDirectAnswer(ctx.getInvoiceDraft(), missingBefore, rawMessage.trim());
                if (err != null) {
                    sessionService.updateSession(session, TaalrPendingAction.INVOICE_DRAFT, ctx);
                    return TaalrActionResult.handled(err + "\n\n" + questionForMissing(missingBefore, ctx.getInvoiceDraft()));
                }
            }
        }
        return nextInvoiceStep(request, user, ctx, session, parsed != null ? parsed.getQuestion() : null);
    }

    public TaalrActionResult confirmSend(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        Long invoiceId = ctx.getCreatedInvoiceId();
        if (invoiceId == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please describe the invoice again.");
        }

        TaalrInvoiceDraft draft = ctx.getInvoiceDraft();
        String recipient = resolveRecipient(draft);
        if (!TaalrInputValidation.isValidRecipient(recipient)) {
            sessionService.updateSession(session, TaalrPendingAction.INVOICE_DRAFT, ctx);
            return TaalrActionResult.handled(
                    "Recipient is invalid. Please send a valid email or phone number (with country code if needed).");
        }

        SendInvoiceRequestDto sendReq = new SendInvoiceRequestDto();
        sendReq.setInvoiceId(invoiceId);
        sendReq.setChannel(resolveChannel(draft));
        sendReq.setRecipientPhoneOrEmail(
                TaalrInputValidation.isValidEmail(recipient) ? recipient : TaalrInputValidation.normalizePhone(recipient));

        SendInvoiceResponseDto sendResult = invoiceSendService.sendInvoice(sendReq, user);
        sessionService.clearSession(request);

        if (sendResult.isSuccess()) {
            return TaalrActionResult.handled(
                    "Invoice sent successfully via " + sendReq.getChannel() + " to " + sendReq.getRecipientPhoneOrEmail() + ".");
        }
        return TaalrActionResult.handled(
                sendResult.getMessage() != null ? sendResult.getMessage() : "Failed to send invoice. Please try from the dashboard.");
    }

    public TaalrActionResult cancel(TaalrActionRequest request) {
        sessionService.clearSession(request);
        return TaalrActionResult.handled("Invoice cancelled. Say \"create invoice\" anytime, or ask me to guide you manually.");
    }

    private TaalrActionResult nextInvoiceStep(TaalrActionRequest request, User user, TaalrSessionContext ctx,
            TaalrActionSessionEntity existingSession, String preferredQuestion) {
        String missing = findMissingRequiredField(ctx.getInvoiceDraft());
        if (missing != null) {
            if (existingSession != null) {
                sessionService.updateSession(existingSession, TaalrPendingAction.INVOICE_DRAFT, ctx);
            } else {
                sessionService.saveSession(request, TaalrPendingAction.INVOICE_DRAFT, ctx);
            }
            if (preferredQuestion != null && !preferredQuestion.isBlank()) {
                return TaalrActionResult.handled(preferredQuestion);
            }
            return TaalrActionResult.handled(questionForMissing(missing, ctx.getInvoiceDraft()));
        }
        return prepareSendConfirmation(request, user, ctx, existingSession);
    }

    private TaalrActionResult prepareSendConfirmation(TaalrActionRequest request, User user,
            TaalrSessionContext ctx, TaalrActionSessionEntity existingSession) {
        try {
            Long invoiceId = ctx.getCreatedInvoiceId();
            if (invoiceId == null) {
                InvoiceAndTaxDTO created = createInvoiceFromDraft(ctx.getInvoiceDraft(), user);
                invoiceId = created.getId();
                ctx.setCreatedInvoiceId(invoiceId);
                if (created.getInvoiceNum() != null) {
                    ctx.getInvoiceDraft().setInvoiceNum(created.getInvoiceNum());
                }
            }

            if (existingSession != null) {
                sessionService.updateSession(existingSession, TaalrPendingAction.INVOICE_SEND_CONFIRM, ctx);
            } else {
                sessionService.saveSession(request, TaalrPendingAction.INVOICE_SEND_CONFIRM, ctx);
            }

            return TaalrActionResult.handled(formatInvoicePreview(ctx.getInvoiceDraft(), invoiceId));
        } catch (Exception e) {
            log.error("Taalr invoice create failed for user {}", user.getUserId(), e);
            sessionService.clearSession(request);
            return TaalrActionResult.handled("I couldn't create that invoice: " + safeMessage(e)
                    + ". Please check the details and try again.");
        }
    }

    private InvoiceAndTaxDTO createInvoiceFromDraft(TaalrInvoiceDraft draft, User user) {
        if (draft.getInvoiceId() != null) {
            InvoiceAndTaxEntity existing = invoiceAndTaxService.getInvoiceDetail(draft.getInvoiceId(), user);
            if (existing.getId() != null) {
                InvoiceAndTaxDTO dto = new InvoiceAndTaxDTO();
                dto.setId(existing.getId());
                dto.setInvoiceNum(existing.getInvoiceNum());
                dto.setCustomerName(existing.getCustomerName());
                dto.setTaxableAmount(existing.getTaxableAmount());
                return dto;
            }
        }

        ensureLineItemsSeeded(draft);
        recalculateTotal(draft);
        double amount = draft.getAmount() != null ? draft.getAmount() : 0.0;
        int dueDays = draft.getDueDays() != null ? draft.getDueDays() : 30;
        String description = draft.getDescription() != null ? draft.getDescription().trim()
                : (draft.getLineItems().isEmpty() ? "Services" : draft.getLineItems().get(0).getName());

        InvoiceAndTaxDTO dto = new InvoiceAndTaxDTO();
        dto.setCustomerName(draft.getCustomerName());
        dto.setCustomerEmail(draft.getCustomerEmail());
        dto.setCurrency("USD");
        dto.setDescription(description);
        dto.setInvoiceNum("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        dto.setInvoiceIssueDate(LocalDate.now());
        dto.setInvoiceDueDate(LocalDate.now().plusDays(dueDays));
        dto.setInvoiceStatus("DRAFT");
        dto.setTaxableAmount(amount);
        dto.setTotalTaxAmountCalculated(0.0);
        dto.setInvoiceProductList(new ArrayList<>());

        for (TaalrInvoiceLineItem item : draft.getLineItems()) {
            InvoiceProductDTO line = new InvoiceProductDTO();
            line.setProductName(item.getName());
            line.setQuantity(item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1.0);
            line.setAmount(item.getAmount());
            line.setDescription(item.getName());
            dto.getInvoiceProductList().add(line);
        }

        return invoiceAndTaxService.createInvoiceAndTax(dto, user);
    }

    private String formatInvoicePreview(TaalrInvoiceDraft draft, Long invoiceId) {
        ensureLineItemsSeeded(draft);
        recalculateTotal(draft);
        int dueDays = draft.getDueDays() != null ? draft.getDueDays() : 30;
        StringBuilder sb = new StringBuilder("Invoice ready to send:\n\n");
        sb.append("• Invoice #: ").append(draft.getInvoiceNum() != null ? draft.getInvoiceNum() : ("ID " + invoiceId)).append('\n');
        sb.append("• Customer: ").append(draft.getCustomerName()).append('\n');
        sb.append("• Line items:\n");
        int i = 1;
        for (TaalrInvoiceLineItem item : draft.getLineItems()) {
            sb.append("  ").append(i++).append(") ").append(item.getName())
                    .append(" | qty ").append(item.getQuantity() != null ? item.getQuantity() : 1)
                    .append(" | $").append(String.format(Locale.US, "%.2f", item.getAmount())).append('\n');
        }
        sb.append("• Total: $").append(String.format(Locale.US, "%.2f", draft.getAmount())).append('\n');
        sb.append("• Channel: ").append(resolveChannel(draft)).append('\n');
        sb.append("• Send to: ").append(resolveRecipient(draft)).append('\n');
        sb.append("• Due: ").append(LocalDate.now().plusDays(dueDays).format(DATE_FMT)).append("\n\n");
        sb.append("Reply YES to send, or NO to cancel.");
        return sb.toString();
    }

    private void mergeInvoiceDraft(TaalrInvoiceDraft target, TaalrInvoiceDraft source) {
        if (source == null) {
            return;
        }
        if (source.getInvoiceId() != null) {
            target.setInvoiceId(source.getInvoiceId());
        }
        if (notBlank(source.getInvoiceNum())) {
            target.setInvoiceNum(source.getInvoiceNum().trim());
        }
        if (TaalrInputValidation.isValidPersonName(source.getCustomerName())
                || (notBlank(source.getCustomerName()) && !TaalrInputValidation.isConfusion(source.getCustomerName())
                && !TaalrInputValidation.isValidEmail(source.getCustomerName())
                && !TaalrInputValidation.isValidPhone(source.getCustomerName()))) {
            if (notBlank(source.getCustomerName()) && source.getCustomerName().trim().length() >= 2) {
                target.setCustomerName(source.getCustomerName().trim());
            }
        }
        if (TaalrInputValidation.isValidEmail(source.getCustomerEmail())) {
            target.setCustomerEmail(source.getCustomerEmail().trim());
        }
        if (TaalrInputValidation.isValidPhone(source.getCustomerPhone())) {
            target.setCustomerPhone(TaalrInputValidation.normalizePhone(source.getCustomerPhone()));
        }
        if (TaalrInputValidation.isValidDescription(source.getDescription())) {
            target.setDescription(source.getDescription().trim());
        }
        if (source.getQuantity() != null && source.getQuantity() > 0) {
            target.setQuantity(source.getQuantity());
        }
        if (source.getAmount() != null && source.getAmount() > 0
                && (target.getLineItems() == null || target.getLineItems().isEmpty())
                && notBlank(target.getDescription())) {
            target.setAmount(source.getAmount());
            ensureLineItemsSeeded(target);
            target.setAskingAddAnotherLine(true);
        } else if (source.getAmount() != null && source.getAmount() > 0
                && (target.getLineItems() == null || target.getLineItems().isEmpty())) {
            target.setAmount(source.getAmount());
        }
        if (notBlank(source.getChannel())) {
            String ch = source.getChannel().trim().toUpperCase(Locale.ROOT);
            if ("EMAIL".equals(ch) || "WHATSAPP".equals(ch)) {
                target.setChannel(ch);
            }
        }
        if (TaalrInputValidation.isValidRecipient(source.getRecipientPhoneOrEmail())) {
            String recipient = source.getRecipientPhoneOrEmail().trim();
            target.setRecipientPhoneOrEmail(
                    TaalrInputValidation.isValidEmail(recipient) ? recipient : TaalrInputValidation.normalizePhone(recipient));
            if (TaalrInputValidation.isValidEmail(recipient)) {
                target.setCustomerEmail(recipient);
                target.setChannel(InvoiceSendServiceImpl.CHANNEL_EMAIL);
            } else {
                target.setCustomerPhone(TaalrInputValidation.normalizePhone(recipient));
                target.setChannel(InvoiceSendServiceImpl.CHANNEL_WHATSAPP);
            }
        }
        if (source.getDueDays() != null && source.getDueDays() >= 1 && source.getDueDays() <= 365) {
            target.setDueDays(source.getDueDays());
        }
    }

    private String findMissingRequiredField(TaalrInvoiceDraft draft) {
        if (draft.getLineItems() == null) {
            draft.setLineItems(new ArrayList<>());
        }
        if (draft.getInvoiceId() != null || notBlank(draft.getInvoiceNum())) {
            if (!notBlank(resolveRecipient(draft))) {
                return "recipient";
            }
            return null;
        }
        if (!notBlank(draft.getCustomerName())) {
            return "customerName";
        }
        if (draft.getLineItems().isEmpty() || Boolean.TRUE.equals(draft.getCollectingNextLine())
                || draft.getPendingLineName() != null || draft.getPendingLineQty() != null) {
            if (Boolean.TRUE.equals(draft.getAskingAddAnotherLine())
                    && !Boolean.TRUE.equals(draft.getCollectingNextLine())
                    && draft.getPendingLineName() == null
                    && draft.getPendingLineQty() == null
                    && !draft.getLineItems().isEmpty()) {
                return "addAnotherLine";
            }
            if (!notBlank(draft.getPendingLineName())) {
                return "lineName";
            }
            if (draft.getPendingLineQty() == null) {
                return "lineQty";
            }
            return "lineAmount";
        }
        if (Boolean.TRUE.equals(draft.getAskingAddAnotherLine())) {
            return "addAnotherLine";
        }
        recalculateTotal(draft);
        if (draft.getAmount() == null || draft.getAmount() <= 0) {
            return "lineName";
        }
        if (!notBlank(draft.getChannel())) {
            return "channel";
        }
        if (!notBlank(resolveRecipient(draft))) {
            return "recipient";
        }
        if (draft.getDueDays() == null) {
            return "dueDays";
        }
        return null;
    }

    private String resolveRecipient(TaalrInvoiceDraft draft) {
        if (notBlank(draft.getRecipientPhoneOrEmail())) {
            return draft.getRecipientPhoneOrEmail().trim();
        }
        if (InvoiceSendServiceImpl.CHANNEL_EMAIL.equals(resolveChannel(draft))
                && notBlank(draft.getCustomerEmail())) {
            return draft.getCustomerEmail().trim();
        }
        if (notBlank(draft.getCustomerPhone())) {
            return draft.getCustomerPhone().trim();
        }
        return null;
    }

    private String resolveChannel(TaalrInvoiceDraft draft) {
        if (notBlank(draft.getChannel())) {
            String ch = draft.getChannel().trim().toUpperCase(Locale.ROOT);
            if (InvoiceSendServiceImpl.CHANNEL_EMAIL.equals(ch)) {
                return InvoiceSendServiceImpl.CHANNEL_EMAIL;
            }
            return InvoiceSendServiceImpl.CHANNEL_WHATSAPP;
        }
        if (notBlank(draft.getCustomerEmail()) && draft.getCustomerEmail().contains("@")
                && !notBlank(draft.getCustomerPhone())) {
            return InvoiceSendServiceImpl.CHANNEL_EMAIL;
        }
        return InvoiceSendServiceImpl.CHANNEL_WHATSAPP;
    }

    private String questionForMissing(String field, TaalrInvoiceDraft draft) {
        if (field == null) {
            return "Please provide the missing invoice details.";
        }
        return switch (field) {
            case "customerName" -> "Who is this invoice for? Please provide the customer name.";
            case "lineName" -> draft.getLineItems() != null && !draft.getLineItems().isEmpty()
                    ? "Next line item name? (e.g. Design work)"
                    : "First line item name? (e.g. Consulting services)";
            case "lineQty" -> "Quantity for this line item? (e.g. 1)";
            case "lineAmount" -> "Amount for this line item? (e.g. 300)";
            case "addAnotherLine" -> "Add another line item? Reply YES or NO.";
            case "channel" -> "How should I send it — Email or WhatsApp?";
            case "recipient" -> {
                String ch = resolveChannel(draft);
                if (InvoiceSendServiceImpl.CHANNEL_EMAIL.equals(ch)) {
                    yield "What email address should I send the invoice to?";
                }
                yield "What phone number should I send the invoice to? (include country code, e.g. +15551234567)";
            }
            case "dueDays" -> "How many days until due? (e.g. 15 or 30). Reply \"skip\" for default 30 days.";
            default -> "Please provide the missing invoice details.";
        };
    }

    /** @return error message if invalid, otherwise null */
    private String applyDirectAnswer(TaalrInvoiceDraft draft, String missingField, String answer) {
        if (TaalrInputValidation.isConfusion(answer)) {
            return "I didn't catch that as a valid answer.";
        }
        if (draft.getLineItems() == null) {
            draft.setLineItems(new ArrayList<>());
        }
        return switch (missingField) {
            case "customerName" -> {
                if (!TaalrInputValidation.isValidPersonName(answer)
                        && (answer.trim().length() < 2 || TaalrInputValidation.isValidEmail(answer)
                        || TaalrInputValidation.isValidPhone(answer))) {
                    yield "Please enter a valid customer name (letters only, e.g. Ali Ahmed).";
                }
                draft.setCustomerName(answer.trim());
                yield null;
            }
            case "lineName" -> {
                if (!TaalrInputValidation.isValidDescription(answer)) {
                    yield "Please enter a line item name (2–200 characters).";
                }
                draft.setPendingLineName(answer.trim());
                draft.setAskingAddAnotherLine(false);
                draft.setCollectingNextLine(true);
                yield null;
            }
            case "lineQty" -> {
                Double qty = TaalrInputValidation.parseQuantity(answer);
                if (qty == null) {
                    yield "Please enter a valid quantity greater than 0.";
                }
                draft.setPendingLineQty(qty);
                draft.setCollectingNextLine(true);
                yield null;
            }
            case "lineAmount" -> {
                Double amt = TaalrInputValidation.parseAmount(answer);
                if (amt == null) {
                    yield "Please enter a valid line amount greater than 0.";
                }
                TaalrInvoiceLineItem item = new TaalrInvoiceLineItem();
                item.setName(draft.getPendingLineName());
                item.setQuantity(draft.getPendingLineQty() != null ? draft.getPendingLineQty() : 1.0);
                item.setAmount(amt);
                draft.getLineItems().add(item);
                draft.setPendingLineName(null);
                draft.setPendingLineQty(null);
                draft.setCollectingNextLine(false);
                draft.setAskingAddAnotherLine(true);
                draft.setDescription(draft.getLineItems().get(0).getName());
                recalculateTotal(draft);
                yield null;
            }
            case "addAnotherLine" -> {
                Boolean yn = TaalrInputValidation.parseYesNo(answer);
                if (yn == null) {
                    yield "Please reply YES to add another line item, or NO to continue.";
                }
                draft.setAskingAddAnotherLine(false);
                if (Boolean.TRUE.equals(yn)) {
                    draft.setCollectingNextLine(true);
                    draft.setPendingLineName(null);
                    draft.setPendingLineQty(null);
                } else {
                    draft.setCollectingNextLine(false);
                }
                yield null;
            }
            case "channel" -> {
                String ch = TaalrInputValidation.parseChannel(answer);
                if (ch == null) {
                    yield "Please reply with Email or WhatsApp.";
                }
                draft.setChannel(ch);
                yield null;
            }
            case "recipient" -> {
                if (!TaalrInputValidation.isValidRecipient(answer)) {
                    yield "That doesn't look like a valid email or phone. Example: jane@example.com or +15551234567.";
                }
                if (TaalrInputValidation.isValidEmail(answer)) {
                    draft.setRecipientPhoneOrEmail(answer.trim());
                    draft.setCustomerEmail(answer.trim());
                    draft.setChannel(InvoiceSendServiceImpl.CHANNEL_EMAIL);
                } else {
                    String phone = TaalrInputValidation.normalizePhone(answer);
                    draft.setRecipientPhoneOrEmail(phone);
                    draft.setCustomerPhone(phone);
                    draft.setChannel(InvoiceSendServiceImpl.CHANNEL_WHATSAPP);
                }
                yield null;
            }
            case "dueDays" -> {
                Integer days = TaalrInputValidation.parseDueDays(answer);
                if (days == null) {
                    yield "Please enter due days between 1 and 365, or reply \"skip\" for 30 days.";
                }
                draft.setDueDays(days);
                yield null;
            }
            default -> null;
        };
    }

    private static void ensureLineItemsSeeded(TaalrInvoiceDraft draft) {
        if (draft.getLineItems() == null) {
            draft.setLineItems(new ArrayList<>());
        }
        if (draft.getLineItems().isEmpty()
                && notBlank(draft.getDescription())
                && draft.getAmount() != null
                && draft.getAmount() > 0) {
            TaalrInvoiceLineItem item = new TaalrInvoiceLineItem();
            item.setName(draft.getDescription().trim());
            item.setQuantity(draft.getQuantity() != null && draft.getQuantity() > 0 ? draft.getQuantity() : 1.0);
            item.setAmount(draft.getAmount());
            draft.getLineItems().add(item);
            draft.setAskingAddAnotherLine(false);
        }
    }

    private static void recalculateTotal(TaalrInvoiceDraft draft) {
        if (draft.getLineItems() == null || draft.getLineItems().isEmpty()) {
            return;
        }
        double total = 0.0;
        for (TaalrInvoiceLineItem item : draft.getLineItems()) {
            if (item.getAmount() != null) {
                total += item.getAmount();
            }
        }
        draft.setAmount(total);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String safeMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "unknown error";
    }
}
