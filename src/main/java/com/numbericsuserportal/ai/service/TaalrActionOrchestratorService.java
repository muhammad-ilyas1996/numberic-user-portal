package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrActionChannel;
import com.numbericsuserportal.ai.action.TaalrChatMode;
import com.numbericsuserportal.ai.action.TaalrIntent;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.config.TaalrActionProperties;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Locale;

/**
 * Routes app chat and WhatsApp messages to receipt OCR and invoice automation before generic AI replies.
 */
@Service
@Slf4j
public class TaalrActionOrchestratorService {

    @Autowired
    private TaalrActionProperties properties;

    @Autowired
    private TaalrActionSessionService sessionService;

    @Autowired
    private TaalrIntentParserService intentParser;

    @Autowired
    private TaalrReceiptActionHandler receiptHandler;

    @Autowired
    private TaalrInvoiceActionHandler invoiceHandler;

    @Autowired
    private TaalrInvoiceQueryHandler invoiceQueryHandler;

    @Autowired
    private TaalrLlcFormationActionHandler llcFormationHandler;

    @Autowired
    private TaalrPendingContextService pendingContextService;

    @Autowired
    private UserRepository userRepository;

    public TaalrActionResult handle(TaalrActionRequest request) {
        if (!properties.isEnabled()) {
            return TaalrActionResult.notHandled();
        }

        boolean hasMedia = hasMedia(request);
        if (hasMedia) {
            return handleMedia(request);
        }
        return handleText(request);
    }

    private TaalrActionResult handleMedia(TaalrActionRequest request) {
        User user = resolveUser(request);
        if (user == null) {
            return TaalrActionResult.handled(
                    "To scan receipts, please register on Numbrics and link this phone number to your account.");
        }
        return receiptHandler.processMediaUpload(request, user);
    }

    private TaalrActionResult handleText(TaalrActionRequest request) {
        Optional<TaalrActionSessionEntity> sessionOpt = sessionService.findActiveSession(request);
        sessionOpt.ifPresent(sessionService::touchSession);

        boolean awaitingConfirm = sessionOpt.map(s -> s.getPendingAction() == TaalrPendingAction.RECEIPT_SAVE_CONFIRM
                || s.getPendingAction() == TaalrPendingAction.INVOICE_SEND_CONFIRM
                || s.getPendingAction() == TaalrPendingAction.INVOICE_RESEND_CONFIRM).orElse(false);

        String message = request.getMessage() != null ? request.getMessage().trim() : "";
        TaalrIntentParseResult parsed = intentParser.parse(message, awaitingConfirm);
        boolean guideMode = resolveMode(request) == TaalrChatMode.GUIDE;

        if (sessionOpt.isPresent()) {
            TaalrActionResult pending = handlePendingSession(request, sessionOpt.get(), parsed);
            if (pending.isHandled()) {
                return pending;
            }
        }

        User user = resolveUser(request);

        if (sessionOpt.isPresent() && pendingContextService.isResumeMessage(message)) {
            TaalrActionResult resumed = resumePending(request, user, sessionOpt.get(), parsed, message);
            if (resumed.isHandled()) {
                return resumed;
            }
        }

        if (sessionOpt.isPresent() && sessionOpt.get().getPendingAction() == TaalrPendingAction.INVOICE_DRAFT) {
            if (parsed.getIntent() == TaalrIntent.CANCEL) {
                return invoiceHandler.cancel(request);
            }
            if (user != null) {
                return invoiceHandler.continueDraft(request, user, sessionOpt.get(), parsed, message);
            }
        }
        if (sessionOpt.isPresent() && sessionOpt.get().getPendingAction() == TaalrPendingAction.LLC_DRAFT) {
            if (parsed.getIntent() == TaalrIntent.CANCEL) {
                return llcFormationHandler.cancel(request);
            }
            if (user != null) {
                return llcFormationHandler.handleStartOrContinue(request, user, parsed, sessionOpt.get(), message);
            }
        }

        if (user == null) {
            if (!guideMode && isAutomationIntent(parsed.getIntent())) {
                return TaalrActionResult.handled(
                        "Please log in to Numbrics (or link your WhatsApp number to your account) to use invoices and receipts.");
            }
            return TaalrActionResult.notHandled();
        }

        if (sessionOpt.isEmpty() && TaalrCapabilitiesService.shouldShowWelcome(message)) {
            return TaalrActionResult.handled(TaalrCapabilitiesService.buildWelcomeMessage(request.getChannel()));
        }

        if (parsed.getIntent() == TaalrIntent.CONFIRM_NO || parsed.getIntent() == TaalrIntent.CANCEL) {
            if (sessionOpt.isPresent()) {
                return handleCancel(request, sessionOpt.get());
            }
        }

        if (guideMode) {
            return TaalrActionResult.notHandled();
        }

        if (parsed.getIntent() == TaalrIntent.RECEIPT) {
            return receiptHandler.promptForPhoto(request.getChannel());
        }
        if (parsed.getIntent() == TaalrIntent.INVOICE_LIST) {
            extractInvoiceNumToParsed(message, parsed);
            return invoiceQueryHandler.handleList(user, parsed);
        }
        if (parsed.getIntent() == TaalrIntent.INVOICE_RESEND) {
            return invoiceQueryHandler.handleResend(request, user, parsed);
        }
        if (parsed.getIntent() == TaalrIntent.INVOICE) {
            return invoiceHandler.handleNewIntent(request, user, parsed, sessionOpt.orElse(null));
        }
        if (parsed.getIntent() == TaalrIntent.LLC_FORMATION) {
            return llcFormationHandler.handleStartOrContinue(request, user, parsed, sessionOpt.orElse(null), message);
        }
        if (parsed.getIntent() == TaalrIntent.LLC_STATUS) {
            return llcFormationHandler.handleStatus(user);
        }

        return TaalrActionResult.notHandled();
    }

    private TaalrActionResult resumePending(TaalrActionRequest request, User user,
            TaalrActionSessionEntity session, TaalrIntentParseResult parsed, String message) {
        if (user == null) {
            return TaalrActionResult.handled("Please log in to resume your pending task.");
        }
        TaalrPendingAction action = session.getPendingAction();
        if (action == TaalrPendingAction.INVOICE_DRAFT) {
            return invoiceHandler.continueDraft(request, user, session, parsed, message);
        }
        if (action == TaalrPendingAction.INVOICE_SEND_CONFIRM) {
            return TaalrActionResult.handled("Please reply YES to send the invoice, or NO to cancel.");
        }
        if (action == TaalrPendingAction.INVOICE_RESEND_CONFIRM) {
            return TaalrActionResult.handled("Please reply YES to resend the reminder, or NO to cancel.");
        }
        if (action == TaalrPendingAction.RECEIPT_SAVE_CONFIRM) {
            return TaalrActionResult.handled("Please reply YES to save the receipt, or NO to discard.");
        }
        if (action == TaalrPendingAction.LLC_DRAFT) {
            return llcFormationHandler.handleStartOrContinue(request, user, parsed, session, message);
        }
        return TaalrActionResult.notHandled();
    }

    private static boolean isAutomationIntent(TaalrIntent intent) {
        return intent == TaalrIntent.INVOICE || intent == TaalrIntent.RECEIPT
                || intent == TaalrIntent.INVOICE_LIST || intent == TaalrIntent.INVOICE_RESEND
                || intent == TaalrIntent.LLC_FORMATION || intent == TaalrIntent.LLC_STATUS;
    }

    private static TaalrChatMode resolveMode(TaalrActionRequest request) {
        return request.getMode() != null ? request.getMode() : TaalrChatMode.AUTO;
    }

    private TaalrActionResult handlePendingSession(TaalrActionRequest request, TaalrActionSessionEntity session,
            TaalrIntentParseResult parsed) {
        User user = resolveUser(request);
        if (user == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please log in and try again.");
        }

        TaalrPendingAction action = session.getPendingAction();
        if (action == TaalrPendingAction.RECEIPT_SAVE_CONFIRM) {
            if (parsed.getIntent() == TaalrIntent.CONFIRM_YES) {
                return receiptHandler.confirmSave(request, user, session);
            }
            if (parsed.getIntent() == TaalrIntent.CONFIRM_NO || parsed.getIntent() == TaalrIntent.CANCEL) {
                return receiptHandler.discard(request);
            }
            return TaalrActionResult.handled("Please reply YES to save the receipt, or NO to discard.");
        }

        if (action == TaalrPendingAction.INVOICE_SEND_CONFIRM) {
            if (parsed.getIntent() == TaalrIntent.CONFIRM_YES) {
                return invoiceHandler.confirmSend(request, user, session);
            }
            if (parsed.getIntent() == TaalrIntent.CONFIRM_NO || parsed.getIntent() == TaalrIntent.CANCEL) {
                return invoiceHandler.cancel(request);
            }
            return TaalrActionResult.handled("Please reply YES to send the invoice, or NO to cancel.");
        }

        if (action == TaalrPendingAction.INVOICE_RESEND_CONFIRM) {
            TaalrSessionContext ctx = sessionService.loadContext(session);
            boolean needsRecipient = ctx.getInvoiceDraft() == null
                    || ctx.getInvoiceDraft().getRecipientPhoneOrEmail() == null
                    || ctx.getInvoiceDraft().getRecipientPhoneOrEmail().isBlank();

            if (needsRecipient
                    && parsed.getIntent() != TaalrIntent.CONFIRM_YES
                    && parsed.getIntent() != TaalrIntent.CONFIRM_NO) {
                return invoiceQueryHandler.continueResendDraft(request, user, session, request.getMessage());
            }
            if (parsed.getIntent() == TaalrIntent.CONFIRM_YES) {
                return invoiceQueryHandler.confirmResend(request, user, session);
            }
            if (parsed.getIntent() == TaalrIntent.CONFIRM_NO || parsed.getIntent() == TaalrIntent.CANCEL) {
                return invoiceQueryHandler.cancel(request);
            }
            return TaalrActionResult.handled("Please reply YES to resend the reminder, or NO to cancel.");
        }

        if (action == TaalrPendingAction.INVOICE_DRAFT) {
            return TaalrActionResult.notHandled();
        }
        if (action == TaalrPendingAction.LLC_DRAFT) {
            return TaalrActionResult.notHandled();
        }

        return TaalrActionResult.notHandled();
    }

    private TaalrActionResult handleCancel(TaalrActionRequest request, TaalrActionSessionEntity session) {
        if (session.getPendingAction() == TaalrPendingAction.RECEIPT_SAVE_CONFIRM) {
            return receiptHandler.discard(request);
        }
        if (session.getPendingAction() == TaalrPendingAction.INVOICE_RESEND_CONFIRM) {
            return invoiceQueryHandler.cancel(request);
        }
        if (session.getPendingAction() == TaalrPendingAction.LLC_DRAFT) {
            return llcFormationHandler.cancel(request);
        }
        return invoiceHandler.cancel(request);
    }

    private User resolveUser(TaalrActionRequest request) {
        if (request.getUserId() != null) {
            return userRepository.findById(request.getUserId()).orElse(null);
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            return userRepository.findByPhone(request.getPhoneNumber().trim()).orElse(null);
        }
        return null;
    }

    private static boolean hasMedia(TaalrActionRequest request) {
        return (request.getMediaUrl() != null && !request.getMediaUrl().isBlank())
                || (request.getMediaBase64() != null && !request.getMediaBase64().isBlank());
    }

    public TaalrActionRequest buildAppChatRequest(User user, String message, String mediaBase64,
            String mediaContentType, String mediaFileName, TaalrChatMode mode) {
        return TaalrActionRequest.builder()
                .mode(mode != null ? mode : TaalrChatMode.AUTO)
                .channel(TaalrActionChannel.APP_CHAT)
                .userId(user.getUserId())
                .message(message)
                .mediaBase64(mediaBase64)
                .mediaContentType(mediaContentType)
                .mediaFileName(mediaFileName)
                .build();
    }

    public static TaalrChatMode parseChatMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return TaalrChatMode.AUTO;
        }
        try {
            return TaalrChatMode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return TaalrChatMode.AUTO;
        }
    }

    public TaalrActionRequest buildAppChatRequest(User user, String message, String mediaBase64,
            String mediaContentType, String mediaFileName) {
        return buildAppChatRequest(user, message, mediaBase64, mediaContentType, mediaFileName, TaalrChatMode.AUTO);
    }

    public TaalrActionRequest buildWhatsAppRequest(String phone, Long userId, String message,
            String mediaUrl, String mediaContentType) {
        return TaalrActionRequest.builder()
                .channel(TaalrActionChannel.WHATSAPP)
                .phoneNumber(phone)
                .userId(userId)
                .message(message)
                .mediaUrl(mediaUrl)
                .mediaContentType(mediaContentType)
                .build();
    }

    private static void extractInvoiceNumToParsed(String message, TaalrIntentParseResult parsed) {
        if (message == null || parsed.getInvoice() == null) {
            return;
        }
        if (parsed.getInvoice().getInvoiceNum() != null && !parsed.getInvoice().getInvoiceNum().isBlank()) {
            return;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "\\b(INV-[A-Za-z0-9\\-]+)\\b", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(message);
        if (m.find()) {
            parsed.getInvoice().setInvoiceNum(m.group(1).toUpperCase(java.util.Locale.ROOT));
        }
    }
}
