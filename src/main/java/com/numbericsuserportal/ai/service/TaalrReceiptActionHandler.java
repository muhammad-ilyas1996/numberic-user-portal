package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrActionChannel;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.ai.action.dto.TaalrReceiptDraft;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.ai.util.InMemoryMultipartFile;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveRequestDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptSaveResponseDTO;
import com.numbericsuserportal.recieptupload.dto.ReceiptUploadResponseDTO;
import com.numbericsuserportal.recieptupload.service.ReceiptService;
import com.numbericsuserportal.twilio.service.FileHandlerService;
import com.numbericsuserportal.usermanagement.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@Slf4j
public class TaalrReceiptActionHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @Autowired
    private ReceiptService receiptService;

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

            TaalrSessionContext ctx = new TaalrSessionContext();
            ctx.setReceiptDraft(draft);
            sessionService.saveSession(request, TaalrPendingAction.RECEIPT_SAVE_CONFIRM, ctx);

            return TaalrActionResult.handled(formatReceiptPreview(draft, upload.getSuccess()));
        } catch (Exception e) {
            log.error("Taalr receipt OCR failed for user {}", user.getUserId(), e);
            return TaalrActionResult.handled("Sorry, I couldn't process that receipt. Please try another photo or upload from the dashboard.");
        }
    }

    public TaalrActionResult confirmSave(TaalrActionRequest request, User user, TaalrActionSessionEntity session) {
        TaalrSessionContext ctx = sessionService.loadContext(session);
        TaalrReceiptDraft draft = ctx.getReceiptDraft();
        if (draft == null || draft.getReceiptId() == null) {
            sessionService.clearSession(request);
            return TaalrActionResult.handled("Session expired. Please send the receipt photo again.");
        }

        ReceiptSaveRequestDTO saveRequest = new ReceiptSaveRequestDTO();
        saveRequest.setReceiptId(draft.getReceiptId());
        saveRequest.setMerchantName(draft.getMerchantName());
        saveRequest.setDate(draft.getDate());
        saveRequest.setTotalAmount(draft.getTotalAmount() != null ? draft.getTotalAmount() : BigDecimal.ZERO);
        saveRequest.setTaxAmount(draft.getTaxAmount());
        saveRequest.setEntryType("OCR");
        saveRequest.setStatus("SAVED");
        saveRequest.setCategory("General");

        ReceiptSaveResponseDTO saved = receiptService.saveReceiptData(saveRequest, null);
        sessionService.clearSession(request);

        if (Boolean.TRUE.equals(saved.getSuccess())) {
            return TaalrActionResult.handled("Receipt saved successfully. You can view it in your Numbrics dashboard under Receipts.");
        }
        return TaalrActionResult.handled(
                saved.getMessage() != null ? saved.getMessage() : "Could not save receipt. Please try again from the dashboard.");
    }

    public TaalrActionResult discard(TaalrActionRequest request) {
        sessionService.clearSession(request);
        return TaalrActionResult.handled("Receipt discarded. Send another photo anytime to scan a new receipt.");
    }

    public TaalrActionResult promptForPhoto(TaalrActionChannel channel) {
        String msg = channel == TaalrActionChannel.WHATSAPP
                ? "Send me a photo of your receipt and I'll extract the details for you."
                : "Attach a receipt image in chat (or use WhatsApp) and I'll extract the details for you.";
        return TaalrActionResult.handled(msg);
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
        if (draft.getTaxAmount() != null) {
            sb.append("• Tax: ").append(formatMoney(draft.getTaxAmount())).append('\n');
        }
        sb.append("\nReply YES to save to your account, or NO to discard.");
        return sb.toString();
    }

    private MultipartFile resolveMediaFile(TaalrActionRequest request) throws Exception {
        if (request.getMediaUrl() != null && !request.getMediaUrl().isBlank()) {
            String contentType = request.getMediaContentType() != null ? request.getMediaContentType() : "image/jpeg";
            try (InputStream in = fileHandlerService.downloadFile(request.getMediaUrl().trim())) {
                byte[] bytes = in.readAllBytes();
                String filename = guessFilename(contentType);
                return new InMemoryMultipartFile("file", filename, contentType, bytes);
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

    private static String guessFilename(String contentType) {
        if (contentType != null && contentType.toLowerCase().contains("png")) {
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
}
