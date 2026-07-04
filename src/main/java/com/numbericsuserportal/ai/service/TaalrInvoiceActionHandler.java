package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrIntent;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrInvoiceDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
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

        mergeInvoiceDraft(ctx.getInvoiceDraft(), incoming);

        if (parsed.getMissingField() != null && !parsed.getMissingField().isBlank()
                && parsed.getQuestion() != null && !parsed.getQuestion().isBlank()) {
            if (existingSession != null) {
                sessionService.updateSession(existingSession, TaalrPendingAction.INVOICE_DRAFT, ctx);
            } else {
                sessionService.saveSession(request, TaalrPendingAction.INVOICE_DRAFT, ctx);
            }
            return TaalrActionResult.handled(parsed.getQuestion());
        }

        String missing = findMissingRequiredField(ctx.getInvoiceDraft());
        if (missing != null) {
            ctx.setInvoiceDraft(ctx.getInvoiceDraft());
            if (existingSession != null) {
                sessionService.updateSession(existingSession, TaalrPendingAction.INVOICE_DRAFT, ctx);
            } else {
                sessionService.saveSession(request, TaalrPendingAction.INVOICE_DRAFT, ctx);
            }
            return TaalrActionResult.handled(questionForMissing(missing, ctx.getInvoiceDraft()));
        }

        return prepareSendConfirmation(request, user, ctx, existingSession);
    }

    public TaalrActionResult continueDraft(TaalrActionRequest request, User user,
            TaalrActionSessionEntity session, TaalrIntentParseResult parsed, String rawMessage) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        if (parsed.getInvoice() != null) {
            mergeInvoiceDraft(ctx.getInvoiceDraft(), parsed.getInvoice());
        }
        String missingBefore = findMissingRequiredField(ctx.getInvoiceDraft());
        if (missingBefore != null && rawMessage != null && !rawMessage.isBlank()
                && parsed.getIntent() == TaalrIntent.CHAT) {
            applyDirectAnswer(ctx.getInvoiceDraft(), missingBefore, rawMessage.trim());
        }
        String missing = findMissingRequiredField(ctx.getInvoiceDraft());
        if (missing != null) {
            sessionService.updateSession(session, TaalrPendingAction.INVOICE_DRAFT, ctx);
            if (parsed.getQuestion() != null && !parsed.getQuestion().isBlank()) {
                return TaalrActionResult.handled(parsed.getQuestion());
            }
            return TaalrActionResult.handled(questionForMissing(missing, ctx.getInvoiceDraft()));
        }
        return prepareSendConfirmation(request, user, ctx, session);
    }

    public TaalrActionResult confirmSend(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        Long invoiceId = ctx.getCreatedInvoiceId();
        if (invoiceId == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please describe the invoice again.");
        }

        TaalrInvoiceDraft draft = ctx.getInvoiceDraft();
        SendInvoiceRequestDto sendReq = new SendInvoiceRequestDto();
        sendReq.setInvoiceId(invoiceId);
        sendReq.setChannel(resolveChannel(draft));
        sendReq.setRecipientPhoneOrEmail(resolveRecipient(draft));

        SendInvoiceResponseDto sendResult = invoiceSendService.sendInvoice(sendReq, user);
        sessionService.clearSession(request);

        if (sendResult.isSuccess()) {
            String channel = sendReq.getChannel();
            return TaalrActionResult.handled(
                    "Invoice sent successfully via " + channel + " to " + sendReq.getRecipientPhoneOrEmail() + ".");
        }
        return TaalrActionResult.handled(
                sendResult.getMessage() != null ? sendResult.getMessage() : "Failed to send invoice. Please try from the dashboard.");
    }

    public TaalrActionResult cancel(TaalrActionRequest request) {
        sessionService.clearSession(request);
        return TaalrActionResult.handled("Invoice cancelled. Let me know if you'd like to create another one.");
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
            return TaalrActionResult.handled("I couldn't create that invoice: " + safeMessage(e) + ". Please check the details and try again.");
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
        if (draft.getInvoiceNum() != null && !draft.getInvoiceNum().isBlank()) {
            InvoiceAndTaxEntity existing = invoiceAndTaxService.getInvoiceDetailByInvoiceNumber(
                    draft.getInvoiceNum().trim(), user);
            if (existing.getId() != null) {
                draft.setInvoiceId(existing.getId());
                if (draft.getAmount() == null && existing.getTaxableAmount() != null) {
                    draft.setAmount(existing.getTaxableAmount());
                }
                if (draft.getCustomerName() == null) {
                    draft.setCustomerName(existing.getCustomerName());
                }
                InvoiceAndTaxDTO dto = new InvoiceAndTaxDTO();
                dto.setId(existing.getId());
                dto.setInvoiceNum(existing.getInvoiceNum());
                return dto;
            }
        }

        double amount = draft.getAmount() != null ? draft.getAmount() : 0.0;
        InvoiceAndTaxDTO dto = new InvoiceAndTaxDTO();
        dto.setCustomerName(draft.getCustomerName());
        dto.setCustomerEmail(draft.getCustomerEmail());
        dto.setCurrency("USD");
        dto.setDescription(draft.getDescription() != null ? draft.getDescription() : "Services");
        dto.setInvoiceNum("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        dto.setInvoiceIssueDate(LocalDate.now());
        dto.setInvoiceDueDate(LocalDate.now().plusDays(30));
        dto.setInvoiceStatus("DRAFT");
        dto.setTaxableAmount(amount);
        dto.setTotalTaxAmountCalculated(amount);

        InvoiceProductDTO line = new InvoiceProductDTO();
        line.setProductName(draft.getDescription() != null ? draft.getDescription() : "Service");
        line.setQuantity(1.0);
        line.setAmount(amount);
        dto.setInvoiceProductList(new ArrayList<>());
        dto.getInvoiceProductList().add(line);

        return invoiceAndTaxService.createInvoiceAndTax(dto, user);
    }

    private String formatInvoicePreview(TaalrInvoiceDraft draft, Long invoiceId) {
        StringBuilder sb = new StringBuilder("Invoice ready to send:\n\n");
        sb.append("• Invoice #: ").append(draft.getInvoiceNum() != null ? draft.getInvoiceNum() : ("ID " + invoiceId)).append('\n');
        sb.append("• Customer: ").append(draft.getCustomerName()).append('\n');
        sb.append("• Amount: $").append(draft.getAmount()).append('\n');
        sb.append("• Channel: ").append(resolveChannel(draft)).append('\n');
        sb.append("• Send to: ").append(resolveRecipient(draft)).append('\n');
        sb.append("• Due: ").append(LocalDate.now().plusDays(30).format(DATE_FMT)).append("\n\n");
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
        if (source.getInvoiceNum() != null && !source.getInvoiceNum().isBlank()) {
            target.setInvoiceNum(source.getInvoiceNum().trim());
        }
        if (source.getCustomerName() != null && !source.getCustomerName().isBlank()) {
            target.setCustomerName(source.getCustomerName().trim());
        }
        if (source.getCustomerEmail() != null && !source.getCustomerEmail().isBlank()) {
            target.setCustomerEmail(source.getCustomerEmail().trim());
        }
        if (source.getCustomerPhone() != null && !source.getCustomerPhone().isBlank()) {
            target.setCustomerPhone(source.getCustomerPhone().trim());
        }
        if (source.getAmount() != null) {
            target.setAmount(source.getAmount());
        }
        if (source.getDescription() != null && !source.getDescription().isBlank()) {
            target.setDescription(source.getDescription().trim());
        }
        if (source.getChannel() != null && !source.getChannel().isBlank()) {
            target.setChannel(source.getChannel().trim().toUpperCase());
        }
        if (source.getRecipientPhoneOrEmail() != null && !source.getRecipientPhoneOrEmail().isBlank()) {
            target.setRecipientPhoneOrEmail(source.getRecipientPhoneOrEmail().trim());
        }
    }

    private String findMissingRequiredField(TaalrInvoiceDraft draft) {
        if (draft.getInvoiceId() != null || (draft.getInvoiceNum() != null && !draft.getInvoiceNum().isBlank())) {
            String recipient = resolveRecipient(draft);
            if (recipient == null || recipient.isBlank()) {
                return "recipient";
            }
            return null;
        }
        if (draft.getCustomerName() == null || draft.getCustomerName().isBlank()) {
            return "customerName";
        }
        if (draft.getAmount() == null || draft.getAmount() <= 0) {
            return "amount";
        }
        if (resolveRecipient(draft) == null || resolveRecipient(draft).isBlank()) {
            return "recipient";
        }
        return null;
    }

    private String resolveRecipient(TaalrInvoiceDraft draft) {
        if (draft.getRecipientPhoneOrEmail() != null && !draft.getRecipientPhoneOrEmail().isBlank()) {
            return draft.getRecipientPhoneOrEmail().trim();
        }
        if (InvoiceSendServiceImpl.CHANNEL_EMAIL.equals(resolveChannel(draft))
                && draft.getCustomerEmail() != null && !draft.getCustomerEmail().isBlank()) {
            return draft.getCustomerEmail().trim();
        }
        if (draft.getCustomerPhone() != null && !draft.getCustomerPhone().isBlank()) {
            return draft.getCustomerPhone().trim();
        }
        return null;
    }

    private String resolveChannel(TaalrInvoiceDraft draft) {
        if (draft.getChannel() != null && !draft.getChannel().isBlank()) {
            String ch = draft.getChannel().trim().toUpperCase();
            if (InvoiceSendServiceImpl.CHANNEL_EMAIL.equals(ch)) {
                return InvoiceSendServiceImpl.CHANNEL_EMAIL;
            }
            return InvoiceSendServiceImpl.CHANNEL_WHATSAPP;
        }
        if (draft.getCustomerEmail() != null && draft.getCustomerEmail().contains("@")
                && (draft.getCustomerPhone() == null || draft.getCustomerPhone().isBlank())) {
            return InvoiceSendServiceImpl.CHANNEL_EMAIL;
        }
        return InvoiceSendServiceImpl.CHANNEL_WHATSAPP;
    }

    private String questionForMissing(String field, TaalrInvoiceDraft draft) {
        return switch (field) {
            case "customerName" -> "Who is this invoice for? Please provide the customer name.";
            case "amount" -> "What is the invoice amount? (e.g. $500)";
            case "recipient" -> {
                String ch = resolveChannel(draft);
                if (InvoiceSendServiceImpl.CHANNEL_EMAIL.equals(ch)) {
                    yield "What email address should I send the invoice to?";
                }
                yield "What phone number should I send the invoice to? (include country code if outside US)";
            }
            default -> "Please provide the missing invoice details.";
        };
    }

    private void applyDirectAnswer(TaalrInvoiceDraft draft, String missingField, String answer) {
        switch (missingField) {
            case "customerName" -> draft.setCustomerName(answer);
            case "amount" -> {
                Double parsed = parseAmount(answer);
                if (parsed != null) {
                    draft.setAmount(parsed);
                }
            }
            case "recipient" -> {
                if (answer.contains("@")) {
                    draft.setRecipientPhoneOrEmail(answer);
                    draft.setCustomerEmail(answer);
                    draft.setChannel(InvoiceSendServiceImpl.CHANNEL_EMAIL);
                } else {
                    draft.setRecipientPhoneOrEmail(answer);
                    draft.setCustomerPhone(answer);
                    draft.setChannel(InvoiceSendServiceImpl.CHANNEL_WHATSAPP);
                }
            }
            default -> { /* ignore */ }
        }
    }

    private static Double parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.replaceAll("[^0-9.]", "");
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String safeMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "unknown error";
    }
}
