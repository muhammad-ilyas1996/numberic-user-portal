package com.numbericsuserportal.invoice.impl;

import com.numbericsuserportal.invoice.dto.InvoicePayByTokenDto;
import com.numbericsuserportal.invoice.dto.InvoicePayWithTokenRequestDto;
import com.numbericsuserportal.invoice.dto.InvoiceSendHistoryItemDto;
import com.numbericsuserportal.invoice.dto.InvoiceSendHistorySearch;
import com.numbericsuserportal.invoice.dto.SendInvoiceRequestDto;
import com.numbericsuserportal.invoice.dto.SendInvoiceResponseDto;
import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import com.numbericsuserportal.invoice.entity.InvoiceSendLog;
import com.numbericsuserportal.invoice.repo.InvoiceAndTaxRepo;
import com.numbericsuserportal.invoice.repo.InvoiceSendLogRepo;
import com.numbericsuserportal.invoice.entity.MerchantNmiConfig;
import com.numbericsuserportal.invoice.service.EmailService;
import com.numbericsuserportal.invoice.service.InvoiceSendService;
import com.numbericsuserportal.invoice.service.MerchantNmiConfigService;
import com.numbericsuserportal.invoice.service.PaymentTransactionService;
import com.numbericsuserportal.stripeintegration.service.NMIPaymentService;
import com.numbericsuserportal.twilio.dto.TwilioResponse;
import com.numbericsuserportal.twilio.service.TwilioService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceSendServiceImpl implements InvoiceSendService {

    public static final String CHANNEL_WHATSAPP = "WHATSAPP";
    public static final String CHANNEL_EMAIL = "EMAIL";

    @Autowired
    private InvoiceAndTaxRepo invoiceAndTaxRepo;
    @Autowired
    private InvoiceSendLogRepo invoiceSendLogRepo;
    @Autowired
    private TwilioService twilioService;
    @Autowired
    private NMIPaymentService nmiPaymentService;
    @Autowired
    private PaymentTransactionService paymentTransactionService;
    @Autowired(required = false)
    private EmailService emailService;
    @Autowired
    private MerchantNmiConfigService merchantNmiConfigService;

    @Value("${app.invoice.payment-base-url:}")
    private String paymentBaseUrl;

    @Override
    public SendInvoiceResponseDto sendInvoice(SendInvoiceRequestDto request, User currentUser) {
        if (request.getInvoiceId() == null) {
            return new SendInvoiceResponseDto(false, "Invoice ID is required", null);
        }
        String channel = request.getChannel() != null ? request.getChannel().trim().toUpperCase() : "";
        if (!CHANNEL_WHATSAPP.equals(channel) && !CHANNEL_EMAIL.equals(channel)) {
            return new SendInvoiceResponseDto(false, "Channel must be WHATSAPP or EMAIL", null);
        }
        String recipient = request.getRecipientPhoneOrEmail();
        if (recipient == null || recipient.trim().isEmpty()) {
            return new SendInvoiceResponseDto(false,
                CHANNEL_EMAIL.equals(channel) ? "Recipient email is required" : "Recipient phone number is required",
                null);
        }
        recipient = recipient.trim();

        Optional<InvoiceAndTaxEntity> invoiceOpt = invoiceAndTaxRepo.findByIdAndIsActiveTrue(request.getInvoiceId());
        if (invoiceOpt.isEmpty()) {
            return new SendInvoiceResponseDto(false, "Invoice not found or inactive", null);
        }

        InvoiceAndTaxEntity invoice = invoiceOpt.get();
        String token = UUID.randomUUID().toString().replace("-", "");
        String paymentLink = paymentBaseUrl != null && !paymentBaseUrl.isEmpty()
            ? (paymentBaseUrl + (paymentBaseUrl.contains("?") ? "&" : "?") + "token=" + token)
            : "";

        InvoiceSendLog log = new InvoiceSendLog();
        log.setInvoiceId(invoice.getId());
        log.setChannel(channel);
        log.setSentTo(recipient);
        log.setSentAt(new Date());
        log.setToken(token);
        log.setCreatedBy(currentUser != null ? currentUser.getUserId().toString() : null);
        log.setCreatedOn(new Date());

        if (CHANNEL_EMAIL.equals(channel)) {
            if (emailService == null) {
                return new SendInvoiceResponseDto(false, "Email service is not configured", null);
            }
            String subject = "Invoice " + (invoice.getInvoiceNum() != null ? "#" + invoice.getInvoiceNum() : invoice.getId()) + " – Pay now";
            String htmlBody = buildInvoiceEmailBody(invoice, paymentLink);
            boolean sent = emailService.sendEmail(recipient, subject, htmlBody);
            log.setMessageSid(null);
            InvoiceSendLog saved = invoiceSendLogRepo.save(log);
            return sent
                ? new SendInvoiceResponseDto(true, "Invoice sent via email successfully", saved.getId())
                : new SendInvoiceResponseDto(false, "Failed to send email. Check SMTP configuration.", saved.getId());
        }

        // WHATSAPP
        String messageBody = buildWhatsAppMessage(invoice, paymentLink);
        TwilioResponse twilioResponse = twilioService.sendWhatsApp(recipient, messageBody);
        log.setMessageSid(twilioResponse != null ? twilioResponse.getMessageSid() : null);
        InvoiceSendLog saved = invoiceSendLogRepo.save(log);

        if (twilioResponse != null && twilioResponse.isSuccess()) {
            org.slf4j.LoggerFactory.getLogger(InvoiceSendServiceImpl.class)
                .info("WhatsApp sent to {} for invoice {}; Twilio SID: {}", recipient, invoice.getId(), twilioResponse.getMessageSid());
            return new SendInvoiceResponseDto(true, "Invoice sent via WhatsApp successfully", saved.getId());
        }
        if (twilioResponse != null && twilioResponse.getError() != null) {
            org.slf4j.LoggerFactory.getLogger(InvoiceSendServiceImpl.class)
                .warn("WhatsApp send failed to {}: {}", recipient, twilioResponse.getError());
        }
        return new SendInvoiceResponseDto(
            false,
            twilioResponse != null && twilioResponse.getError() != null ? twilioResponse.getError() : "Failed to send WhatsApp message",
            saved.getId()
        );
    }

    private String buildInvoiceEmailBody(InvoiceAndTaxEntity invoice, String paymentLink) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif;'>");
        html.append("<h2>Your invoice is ready</h2>");
        if (invoice.getInvoiceNum() != null && !invoice.getInvoiceNum().isEmpty()) {
            html.append("<p>Invoice #").append(escapeHtml(invoice.getInvoiceNum())).append("</p>");
        }
        if (invoice.getCustomerName() != null) {
            html.append("<p>Dear ").append(escapeHtml(invoice.getCustomerName())).append(",</p>");
        }
        html.append("<p>Please click the link below to view and pay your invoice.</p>");
        if (paymentLink != null && !paymentLink.isEmpty()) {
            html.append("<p><a href=\"").append(escapeHtml(paymentLink)).append("\" style='display:inline-block;padding:10px 20px;background:#007bff;color:white;text-decoration:none;border-radius:5px;'>Pay now</a></p>");
            html.append("<p>Or copy this link: ").append(escapeHtml(paymentLink)).append("</p>");
        }
        html.append("<p>Thank you.</p>");
        html.append("</body></html>");
        return html.toString();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    @Override
    public InvoicePayByTokenDto getInvoiceByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return invalidDto();
        }
        Optional<InvoiceSendLog> logOpt = invoiceSendLogRepo.findByToken(token.trim());
        if (logOpt.isEmpty()) {
            return invalidDto();
        }
        Optional<InvoiceAndTaxEntity> invoiceOpt = invoiceAndTaxRepo.findByIdAndIsActiveTrue(logOpt.get().getInvoiceId());
        if (invoiceOpt.isEmpty()) {
            return invalidDto();
        }
        InvoiceAndTaxEntity inv = invoiceOpt.get();
        if ("PAID".equalsIgnoreCase(inv.getInvoiceStatus())) {
            InvoicePayByTokenDto dto = toPayByTokenDto(inv);
            dto.setValid(false);
            return dto;
        }
        InvoicePayByTokenDto dto = toPayByTokenDto(inv);
        dto.setValid(true);
        return dto;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> payWithToken(InvoicePayWithTokenRequestDto request) {
        Map<String, Object> result = new HashMap<>();
        if (request == null || request.getToken() == null || request.getToken().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Token is required");
            return result;
        }
        if (request.getCardNumber() == null || request.getCardExpiry() == null || request.getCardCvv() == null) {
            result.put("success", false);
            result.put("message", "Card details are required");
            return result;
        }
        Optional<InvoiceSendLog> logOpt = invoiceSendLogRepo.findByToken(request.getToken().trim());
        if (logOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Invalid or expired link");
            return result;
        }
        Optional<InvoiceAndTaxEntity> invoiceOpt = invoiceAndTaxRepo.findByIdAndIsActiveTrue(logOpt.get().getInvoiceId());
        if (invoiceOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Invoice not found");
            return result;
        }
        InvoiceAndTaxEntity invoice = invoiceOpt.get();
        if ("PAID".equalsIgnoreCase(invoice.getInvoiceStatus())) {
            result.put("success", false);
            result.put("message", "This invoice is already paid");
            return result;
        }
        double amount = getPayableAmount(invoice);
        if (amount <= 0) {
            result.put("success", false);
            result.put("message", "Invalid invoice amount");
            return result;
        }
        NMIPaymentService.CustomerInfo customerInfo = null;
        if (request.getCustomerInfo() != null && !request.getCustomerInfo().isEmpty()) {
            Map<String, String> c = request.getCustomerInfo();
            customerInfo = new NMIPaymentService.CustomerInfo();
            customerInfo.setFirstName(c.get("firstName"));
            customerInfo.setLastName(c.get("lastName"));
            customerInfo.setEmail(c.get("email"));
            customerInfo.setAddress(c.get("address"));
            customerInfo.setCity(c.get("city"));
            customerInfo.setState(c.get("state"));
            customerInfo.setZip(c.get("zip"));
            customerInfo.setCountry(c.get("country"));
            customerInfo.setPhone(c.get("phone"));
        }
        try {
            NMIPaymentService.NMIPaymentResponse nmiResponse;
            Long merchantUserId = parseUserId(invoice.getCreatedBy());
            java.util.Optional<MerchantNmiConfig> merchantConfig = merchantUserId != null
                ? merchantNmiConfigService.getEntityByUserId(merchantUserId) : java.util.Optional.empty();
            if (merchantConfig.isPresent() && isMerchantNmiConfigured(merchantConfig.get())) {
                NMIPaymentService.NmiCredentials creds = toNmiCredentials(merchantConfig.get());
                nmiResponse = nmiPaymentService.processPaymentWithCredentials(
                    amount,
                    request.getCardNumber().trim().replaceAll("\\s", ""),
                    request.getCardExpiry().trim(),
                    request.getCardCvv().trim(),
                    "Invoice #" + (invoice.getInvoiceNum() != null ? invoice.getInvoiceNum() : invoice.getId()),
                    customerInfo,
                    creds
                );
            } else {
                nmiResponse = nmiPaymentService.processPayment(
                    amount,
                    request.getCardNumber().trim().replaceAll("\\s", ""),
                    request.getCardExpiry().trim(),
                    request.getCardCvv().trim(),
                    "Invoice #" + (invoice.getInvoiceNum() != null ? invoice.getInvoiceNum() : invoice.getId()),
                    customerInfo
                );
            }
            if (nmiResponse != null && nmiResponse.isSuccess()) {
                invoice.setInvoiceStatus("PAID");
                invoice.setModifiedBy("INVOICE_PAY");
                invoice.setModifiedOn(new Date());
                invoiceAndTaxRepo.save(invoice);
                String payerEmail = customerInfo != null ? customerInfo.getEmail() : null;
                paymentTransactionService.saveSuccess(
                    invoice.getId(),
                    invoice.getInvoiceNum(),
                    amount,
                    invoice.getCurrency() != null ? invoice.getCurrency() : "USD",
                    "NMI",
                    nmiResponse.getTransactionId(),
                    nmiResponse.getAuthCode(),
                    payerEmail,
                    "Invoice #" + (invoice.getInvoiceNum() != null ? invoice.getInvoiceNum() : invoice.getId())
                );
                result.put("success", true);
                result.put("message", "Payment successful");
                result.put("transactionId", nmiResponse.getTransactionId());
                result.put("authCode", nmiResponse.getAuthCode());
                return result;
            }
            result.put("success", false);
            result.put("message", nmiResponse != null ? nmiResponse.getMessage() : "Payment failed");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Payment failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            return result;
        }
    }

    private double getPayableAmount(InvoiceAndTaxEntity invoice) {
        if (invoice.getTotalTaxAmountCalculated() != null && invoice.getTotalTaxAmountCalculated() > 0) {
            return invoice.getTotalTaxAmountCalculated();
        }
        if (invoice.getTaxableAmount() != null && invoice.getTaxableAmount() > 0) {
            return invoice.getTaxableAmount();
        }
        return 0.0;
    }

    private InvoicePayByTokenDto invalidDto() {
        InvoicePayByTokenDto dto = new InvoicePayByTokenDto();
        dto.setValid(false);
        return dto;
    }

    private InvoicePayByTokenDto toPayByTokenDto(InvoiceAndTaxEntity inv) {
        InvoicePayByTokenDto dto = new InvoicePayByTokenDto();
        dto.setInvoiceId(inv.getId());
        dto.setInvoiceNum(inv.getInvoiceNum());
        dto.setAmount(getPayableAmount(inv));
        dto.setCurrency(inv.getCurrency() != null ? inv.getCurrency() : "USD");
        dto.setCustomerName(inv.getCustomerName());
        dto.setDescription(inv.getDescription());
        dto.setDueDate(inv.getInvoiceDueDate());
        return dto;
    }

    @Override
    public Page<InvoiceSendHistoryItemDto> getSendHistory(InvoiceSendHistorySearch search) {
        if (search.getInvoiceId() == null) {
            return Page.empty(PageRequest.of(0, search.getPageSize() != null ? search.getPageSize() : 20));
        }
        int page = search.getPageNumber() != null && search.getPageNumber() > 0 ? search.getPageNumber() - 1 : 0;
        int size = search.getPageSize() != null && search.getPageSize() > 0 ? search.getPageSize() : 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<InvoiceSendLog> logPage = invoiceSendLogRepo.findByInvoiceIdOrderBySentAtDesc(search.getInvoiceId(), pageable);
        return logPage.map(this::toHistoryItemDto);
    }

    private String buildWhatsAppMessage(InvoiceAndTaxEntity invoice, String paymentLink) {
        StringBuilder sb = new StringBuilder();
        sb.append("Your invoice ");
        if (invoice.getInvoiceNum() != null && !invoice.getInvoiceNum().isEmpty()) {
            sb.append("#").append(invoice.getInvoiceNum()).append(" ");
        }
        sb.append("is ready.");
        if (paymentLink != null && !paymentLink.isEmpty()) {
            sb.append(" Pay here: ").append(paymentLink);
        }
        return sb.toString();
    }

    private InvoiceSendHistoryItemDto toHistoryItemDto(InvoiceSendLog log) {
        InvoiceSendHistoryItemDto dto = new InvoiceSendHistoryItemDto();
        dto.setId(log.getId());
        dto.setInvoiceId(log.getInvoiceId());
        dto.setChannel(log.getChannel());
        dto.setSentTo(log.getSentTo());
        dto.setSentAt(log.getSentAt());
        dto.setMessageSid(log.getMessageSid());
        return dto;
    }

    private static Long parseUserId(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) return null;
        try {
            return Long.parseLong(createdBy.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isMerchantNmiConfigured(MerchantNmiConfig c) {
        if (c.getSecurityKey() != null && !c.getSecurityKey().isEmpty()) return true;
        return c.getNmiUsername() != null && !c.getNmiUsername().isEmpty()
            && c.getNmiPassword() != null && !c.getNmiPassword().isEmpty();
    }

    private static NMIPaymentService.NmiCredentials toNmiCredentials(MerchantNmiConfig c) {
        NMIPaymentService.NmiCredentials creds = new NMIPaymentService.NmiCredentials();
        creds.setAuthMethod(c.getAuthMethod() != null ? c.getAuthMethod() : "api_key");
        creds.setSecurityKey(c.getSecurityKey());
        creds.setNmiUsername(c.getNmiUsername());
        creds.setNmiPassword(c.getNmiPassword());
        creds.setTransactionUrl(c.getTransactionUrl());
        return creds;
    }
}
