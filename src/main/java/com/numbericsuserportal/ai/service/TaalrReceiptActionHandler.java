package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrActionChannel;
import com.numbericsuserportal.ai.action.TaalrIntent;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrIntentParseResult;
import com.numbericsuserportal.ai.action.dto.TaalrReceiptDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.ai.util.InMemoryMultipartFile;
import com.numbericsuserportal.ai.util.TaalrInputValidation;
import com.numbericsuserportal.recieptupload.dto.ReceiptDataResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveRequestDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSearch;
import com.numbericsuserportal.recieptupload.dto.ReceiptUploadResponseDTO;
import com.numbericsuserportal.recieptupload.respository.ReceiptRepository;
import com.numbericsuserportal.recieptupload.service.ReceiptService;
import com.numbericsuserportal.twilio.service.FileHandlerService;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

@Service
@Slf4j
public class TaalrReceiptActionHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final double LOW_CONFIDENCE = 0.55;

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private FileHandlerService fileHandlerService;

    @Autowired
    private TaalrActionSessionService sessionService;

    public TaalrActionResult processMediaUpload(TaalrActionRequest request, User user) {
        if (user == null || user.getUserId() == null) {
            return TaalrActionResult.handled(
                    "To save receipts via WhatsApp, please register on Numbrics and use the same phone number on your account.");
        }
        try {
            MultipartFile file = resolveMediaFile(request);
            if (file == null || file.isEmpty()) {
                return TaalrActionResult.handled("I couldn't read that image. Please send a clear photo of your receipt (JPEG or PNG).");
            }

            ReceiptUploadResponseDTO upload = receiptService.uploadReceipt(file, user.getUserId());
            if (upload.getReceiptId() == null) {
                return TaalrActionResult.handled(
                        upload.getMessage() != null ? upload.getMessage() : "Receipt upload failed. Please try again.");
            }

            TaalrReceiptDraft draft = new TaalrReceiptDraft();
            draft.setReceiptId(upload.getReceiptId());
            draft.setMerchantName(upload.getMerchantName());
            draft.setDate(upload.getDate());
            draft.setTotalAmount(upload.getTotalAmount());
            draft.setTaxAmount(upload.getTaxAmount());
            draft.setConfidenceScore(upload.getConfidenceScore());
            draft.setCategory(null);
            draft.setEntryType("OCR");
            draft.setEditing(false);

            TaalrSessionContext ctx = new TaalrSessionContext();
            ctx.setReceiptDraft(draft);
            sessionService.saveSession(request, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);

            return TaalrActionResult.handled(formatReceiptPreview(draft, upload.getSuccess()));
        } catch (Exception e) {
            log.error("Taalr receipt OCR failed for user {}", user.getUserId(), e);
            return TaalrActionResult.handled("Sorry, I couldn't process that receipt. Please try another photo or upload from the dashboard.");
        }
    }

    public TaalrActionResult continueConfirm(TaalrActionRequest request, User user,
            TaalrActionSessionEntity session, TaalrIntentParseResult parsed, String rawMessage) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        TaalrReceiptDraft draft = ctx.getReceiptDraft();
        if (draft == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please send a receipt photo or say \"manual receipt\".");
        }
        if ("MANUAL".equalsIgnoreCase(draft.getEntryType()) && draft.getReceiptId() == null) {
            return continueManualEntry(request, user, session, ctx, draft, parsed, rawMessage);
        }
        if (draft.getReceiptId() == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please send the receipt photo again.");
        }

        String message = rawMessage != null ? rawMessage.trim() : "";
        if (TaalrInputValidation.isConfusion(message)) {
            return TaalrActionResult.handled(
                    "No problem. Reply:\n• YES — save\n• NO — discard\n• EDIT — change fields\n• or a category like Travel / Office / Meals");
        }

        if (parsed.getIntent() == TaalrIntent.CONFIRM_YES) {
            String missing = missingBeforeSave(draft);
            if (missing != null) {
                sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
                return TaalrActionResult.handled(questionForReceipt(missing));
            }
            if (draft.getConfidenceScore() != null && draft.getConfidenceScore() < LOW_CONFIDENCE
                    && !draft.isEditing()) {
                return TaalrActionResult.handled(
                        "OCR confidence is low. Please reply EDIT and correct merchant/date/amount before saving.");
            }
            return confirmSave(request, user, session);
        }
        if (parsed.getIntent() == TaalrIntent.CONFIRM_NO || parsed.getIntent() == TaalrIntent.CANCEL) {
            return discard(request);
        }

        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.equals("edit") || lower.startsWith("edit ")) {
            draft.setEditing(true);
            draft.setEditField(null);
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled(
                    "What do you want to edit?\nReply: merchant / date / amount / tax / category");
        }

        if (draft.isEditing() && draft.getEditField() == null) {
            String field = mapEditField(lower);
            if (field == null) {
                return TaalrActionResult.handled("Please reply with one of: merchant, date, amount, tax, category");
            }
            draft.setEditField(field);
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled(questionForReceipt(field));
        }

        if (draft.isEditing() && draft.getEditField() != null) {
            String err = applyEdit(draft, draft.getEditField(), message);
            if (err != null) {
                return TaalrActionResult.handled(err);
            }
            draft.setEditing(false);
            draft.setEditField(null);
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled("Updated.\n\n" + formatReceiptPreview(draft, true));
        }

        if (!notBlank(draft.getCategory())) {
            if (isAllowedCategory(message)) {
                draft.setCategory(normalizeCategory(message));
                sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
                return TaalrActionResult.handled("Category set to " + draft.getCategory()
                        + ".\nReply YES to save, EDIT to change fields, or NO to discard.");
            }
            return TaalrActionResult.handled(
                    "Please choose a category: Travel / Office / Meals / General\n"
                            + "(Or reply EDIT / YES / NO)");
        }

        return TaalrActionResult.handled(
                "Please reply YES to save, NO to discard, or EDIT to correct merchant/date/amount/tax/category.");
    }

    public TaalrActionResult confirmSave(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        TaalrReceiptDraft draft = ctx.getReceiptDraft();
        if (draft == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please try again.");
        }

        String missing = missingBeforeSave(draft);
        if (missing != null) {
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled(questionForReceipt(missing));
        }

        try {
            ReceiptSaveRequestDTO saveRequest = new ReceiptSaveRequestDTO();
            saveRequest.setReceiptId(draft.getReceiptId());
            saveRequest.setUserId(user.getUserId());
            saveRequest.setMerchantName(draft.getMerchantName());
            saveRequest.setDate(draft.getDate());
            saveRequest.setTotalAmount(draft.getTotalAmount());
            saveRequest.setTaxAmount(draft.getTaxAmount());
            saveRequest.setEntryType(draft.getEntryType() != null ? draft.getEntryType() : "OCR");
            saveRequest.setStatus("SAVED");
            saveRequest.setCategory(draft.getCategory());

            MultipartFile file = null;
            if ("MANUAL".equalsIgnoreCase(saveRequest.getEntryType()) && draft.getReceiptId() == null) {
                file = placeholderManualFile();
            }

            ReceiptSaveResponseDTO saved = receiptService.saveReceiptData(saveRequest, file);
            if (Boolean.TRUE.equals(saved.getSuccess())) {
                sessionService.clearSession(request);
                return TaalrActionResult.handled("Receipt saved successfully under category \""
                        + draft.getCategory() + "\" (" + saveRequest.getEntryType()
                        + "). You can view it in Numbrics → Receipts.");
            }
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled(
                    (saved.getMessage() != null ? saved.getMessage() : "Could not save receipt.")
                            + " Reply YES to retry, EDIT to change, or NO to discard.");
        } catch (Exception e) {
            log.error("Receipt save failed for user {}", user.getUserId(), e);
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled("Could not save receipt: "
                    + (e.getMessage() != null ? e.getMessage() : "unknown error")
                    + ". Reply YES to retry, or NO to discard.");
        }
    }

    private TaalrActionResult continueManualEntry(TaalrActionRequest request, User user,
            TaalrActionSessionEntity session, TaalrSessionContext ctx, TaalrReceiptDraft draft,
            TaalrIntentParseResult parsed, String rawMessage) {
        String message = rawMessage != null ? rawMessage.trim() : "";
        if (parsed.getIntent() == TaalrIntent.CANCEL || parsed.getIntent() == TaalrIntent.CONFIRM_NO) {
            return discard(request);
        }
        if (TaalrInputValidation.isConfusion(message)) {
            return TaalrActionResult.handled("No problem. " + questionForReceipt(missingBeforeSave(draft)));
        }
        if (parsed.getIntent() == TaalrIntent.CONFIRM_YES) {
            String missing = missingBeforeSave(draft);
            if (missing != null) {
                sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
                return TaalrActionResult.handled(questionForReceipt(missing));
            }
            return confirmSave(request, user, session);
        }

        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.equals("edit") || lower.startsWith("edit ")) {
            draft.setEditing(true);
            draft.setEditField(null);
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled(
                    "What do you want to edit?\nReply: merchant / date / amount / tax / category");
        }
        if (draft.isEditing() && draft.getEditField() == null) {
            String field = mapEditField(lower);
            if (field == null) {
                return TaalrActionResult.handled("Please reply with one of: merchant, date, amount, tax, category");
            }
            draft.setEditField(field);
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled(questionForReceipt(field));
        }
        if (draft.isEditing() && draft.getEditField() != null) {
            String err = applyEdit(draft, draft.getEditField(), message);
            if (err != null) {
                return TaalrActionResult.handled(err);
            }
            draft.setEditing(false);
            draft.setEditField(null);
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled("Updated.\n\n" + formatReceiptPreview(draft, true));
        }

        String missing = missingBeforeSave(draft);
        if (missing == null) {
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled(formatReceiptPreview(draft, true)
                    + "\n\nReply YES to save, EDIT to change, or NO to cancel.");
        }
        String err = applyEdit(draft, missing, message);
        if (err != null) {
            sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
            return TaalrActionResult.handled(err + "\n\n" + questionForReceipt(missing));
        }
        sessionService.updateSession(session, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
        String next = missingBeforeSave(draft);
        if (next != null) {
            return TaalrActionResult.handled(questionForReceipt(next));
        }
        return TaalrActionResult.handled(formatReceiptPreview(draft, true)
                + "\n\nReply YES to save, EDIT to change, or NO to cancel.");
    }

    private static MultipartFile placeholderManualFile() {
        // Minimal valid 1x1 PNG so MANUAL save satisfies existing file requirement.
        byte[] png = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8,
                (byte) 0xCF, (byte) 0xC0, 0x00, 0x00, 0x00, 0x03, 0x00, 0x01, 0x00, 0x05, (byte) 0xFE, (byte) 0xD4,
                (byte) 0xEF, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
        return new InMemoryMultipartFile("file", "manual-entry.png", "image/png", png);
    }

    public TaalrActionResult discard(TaalrActionRequest request) {
        try {
            sessionService.findActiveSession(request).ifPresent(session -> {
                TaalrSessionContext ctx = sessionService.loadContext(session);
                TaalrReceiptDraft draft = ctx.getReceiptDraft();
                if (draft != null && draft.getReceiptId() != null
                        && !"MANUAL".equalsIgnoreCase(draft.getEntryType())) {
                    try {
                        // OCR upload creates Receipt row before save; remove orphan on discard.
                        receiptRepository.deleteById(draft.getReceiptId());
                    } catch (Exception e) {
                        log.warn("Could not delete discarded receipt {}: {}", draft.getReceiptId(), e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            log.warn("Receipt discard cleanup failed: {}", e.getMessage());
        }
        sessionService.clearSession(request);
        return TaalrActionResult.handled("Receipt discarded. Send another photo anytime to scan a new receipt.");
    }

    public TaalrActionResult promptForPhoto(TaalrActionChannel channel) {
        String msg = channel == TaalrActionChannel.WHATSAPP
                ? "Send me a photo of your receipt and I'll extract the details.\nOr say \"manual receipt\" to enter details without a photo."
                : "Attach a receipt image in chat (or use WhatsApp) for OCR.\nOr say \"manual receipt\" to enter details without a photo.";
        return TaalrActionResult.handled(msg);
    }

    public TaalrActionResult startManualEntry(TaalrActionRequest request, User user) {
        TaalrReceiptDraft draft = new TaalrReceiptDraft();
        draft.setEntryType("MANUAL");
        draft.setEditing(false);
        TaalrSessionContext ctx = new TaalrSessionContext();
        ctx.setReceiptDraft(draft);
        sessionService.saveSession(request, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);
        return TaalrActionResult.handled(
                "Manual receipt entry started.\n\nWhat is the merchant / store name?");
    }

    public TaalrActionResult handleList(User user) {
        ReceiptSearch search = new ReceiptSearch();
        search.setPageNumber(0);
        search.setPageSize(5);
        Page<ReceiptDataResponseDTO> page = receiptService.getUserReceipts(user.getUserId(), search);
        if (page == null || page.isEmpty()) {
            return TaalrActionResult.handled("No saved receipts yet. Send a receipt photo to get started.");
        }
        StringBuilder sb = new StringBuilder("Your recent receipts:\n\n");
        for (ReceiptDataResponseDTO row : page.getContent()) {
            sb.append("• ").append(blankToDash(row.getMerchantName()))
                    .append(" | ").append(row.getReceiptDate() != null ? row.getReceiptDate().format(DATE_FMT) : "—")
                    .append(" | ").append(formatMoney(row.getTotalAmount()))
                    .append(" | ").append(blankToDash(row.getCategory()))
                    .append('\n');
        }
        sb.append("\nSend a new photo to scan another receipt.");
        return TaalrActionResult.handled(sb.toString());
    }

    private String missingBeforeSave(TaalrReceiptDraft draft) {
        if (!notBlank(draft.getMerchantName())) {
            return "merchant";
        }
        if (draft.getDate() == null) {
            return "date";
        }
        if (draft.getTotalAmount() == null || draft.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "amount";
        }
        if (!notBlank(draft.getCategory())) {
            return "category";
        }
        return null;
    }

    private String applyEdit(TaalrReceiptDraft draft, String field, String value) {
        if (TaalrInputValidation.isConfusion(value)) {
            return "Please provide a clear value for " + field + ".";
        }
        return switch (field) {
            case "merchant" -> {
                if (value.trim().length() < 2) {
                    yield "Merchant name is too short.";
                }
                draft.setMerchantName(value.trim());
                yield null;
            }
            case "date" -> {
                LocalDate parsed = parseDate(value);
                if (parsed == null) {
                    yield "Please use date format YYYY-MM-DD (e.g. 2026-07-10).";
                }
                draft.setDate(parsed);
                yield null;
            }
            case "amount" -> {
                Double amt = TaalrInputValidation.parseAmount(value);
                if (amt == null) {
                    yield "Please enter a valid total amount (e.g. 45.99).";
                }
                draft.setTotalAmount(BigDecimal.valueOf(amt));
                yield null;
            }
            case "tax" -> {
                if (TaalrInputValidation.isSkip(value)) {
                    draft.setTaxAmount(null);
                    yield null;
                }
                Double tax = TaalrInputValidation.parseAmount(value);
                if (tax == null) {
                    yield "Please enter a valid tax amount, or reply skip.";
                }
                draft.setTaxAmount(BigDecimal.valueOf(tax));
                yield null;
            }
            case "category" -> {
                if (!isAllowedCategory(value)) {
                    yield "Please enter a category: Travel, Office, Meals, or General.";
                }
                draft.setCategory(normalizeCategory(value));
                yield null;
            }
            default -> "Unknown field.";
        };
    }

    private static String mapEditField(String lower) {
        if (lower.contains("merchant") || lower.contains("store") || lower.contains("vendor")) {
            return "merchant";
        }
        if (lower.contains("date")) {
            return "date";
        }
        if (lower.contains("amount") || lower.contains("total")) {
            return "amount";
        }
        if (lower.contains("tax")) {
            return "tax";
        }
        if (lower.contains("categor")) {
            return "category";
        }
        return null;
    }

    private String formatReceiptPreview(TaalrReceiptDraft draft, Boolean ocrSuccess) {
        StringBuilder sb = new StringBuilder();
        if (Boolean.FALSE.equals(ocrSuccess)) {
            sb.append("I saved the image but OCR had limited results.\n\n");
        } else {
            sb.append("Here's what I found on your receipt:\n\n");
        }
        sb.append("• Merchant: ").append(blankToDash(draft.getMerchantName())).append('\n');
        sb.append("• Date: ").append(draft.getDate() != null ? draft.getDate().format(DATE_FMT) : "—").append('\n');
        sb.append("• Total: ").append(formatMoney(draft.getTotalAmount())).append('\n');
        sb.append("• Tax: ").append(formatMoney(draft.getTaxAmount())).append('\n');
        sb.append("• Category: ").append(blankToDash(draft.getCategory())).append('\n');
        if (draft.getConfidenceScore() != null) {
            sb.append("• OCR confidence: ")
                    .append(String.format(Locale.US, "%.0f%%", draft.getConfidenceScore() * 100))
                    .append('\n');
            if (draft.getConfidenceScore() < LOW_CONFIDENCE) {
                sb.append("\nConfidence is low — please EDIT fields before saving.\n");
            }
        }
        sb.append("\nNext:\n");
        if (!notBlank(draft.getCategory())) {
            sb.append("1) Send a category (Travel / Office / Meals / General)\n");
        }
        sb.append("2) Reply YES to save, EDIT to correct, or NO to discard.");
        return sb.toString();
    }

    private MultipartFile resolveMediaFile(TaalrActionRequest request) throws Exception {
        if (request.getMediaUrl() != null && !request.getMediaUrl().isBlank()) {
            String contentType = request.getMediaContentType() != null ? request.getMediaContentType() : "image/jpeg";
            try (InputStream in = fileHandlerService.downloadFile(request.getMediaUrl().trim())) {
                byte[] bytes = in.readAllBytes();
                return new InMemoryMultipartFile("file", guessFilename(contentType), contentType, bytes);
            }
        }
        if (request.getMediaBase64() != null && !request.getMediaBase64().isBlank()) {
            String b64 = request.getMediaBase64().trim();
            if (b64.contains(",")) {
                b64 = b64.substring(b64.indexOf(',') + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(b64);
            String contentType = request.getMediaContentType() != null ? request.getMediaContentType() : "image/jpeg";
            String filename = request.getMediaFileName() != null ? request.getMediaFileName() : guessFilename(contentType);
            return new InMemoryMultipartFile("file", filename, contentType, bytes);
        }
        return null;
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isAllowedCategory(String value) {
        if (value == null || value.isBlank() || TaalrInputValidation.isConfusion(value)) {
            return false;
        }
        String t = value.trim().toLowerCase(Locale.ROOT);
        return t.equals("travel") || t.equals("office") || t.equals("meals") || t.equals("food")
                || t.equals("general") || t.equals("1") || t.equals("2") || t.equals("3") || t.equals("4");
    }

    private static String normalizeCategory(String value) {
        String t = value.trim().toLowerCase(Locale.ROOT);
        return switch (t) {
            case "1", "travel" -> "Travel";
            case "2", "office" -> "Office";
            case "3", "meals", "food" -> "Meals";
            default -> "General";
        };
    }

    private static String questionForReceipt(String field) {
        return switch (field) {
            case "merchant" -> "What is the merchant / store name?";
            case "date" -> "What is the receipt date? (YYYY-MM-DD)";
            case "amount" -> "What is the total amount?";
            case "tax" -> "What is the tax amount? (or reply skip)";
            case "category" -> "What category? Reply Travel / Office / Meals / General";
            default -> "Please provide the missing receipt detail.";
        };
    }

    private static String guessFilename(String contentType) {
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("png")) {
            return "receipt.png";
        }
        return "receipt.jpg";
    }

    private static String blankToDash(String s) {
        return s != null && !s.isBlank() ? s.trim() : "—";
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "—";
        }
        return "$" + amount.stripTrailingZeros().toPlainString();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
