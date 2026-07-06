package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.TaalrActionChannel;

import java.util.regex.Pattern;

/** Welcome menu when user greets Taalr or asks what it can do. */
public final class TaalrCapabilitiesService {

    private static final Pattern GREETING = Pattern.compile(
            "^(hi|hello|hey|hiya|howdy|yes|ok|okay|start|menu|help)"
                    + "(\\s+there)?[!?.\\s]*$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CAPABILITIES_QUESTION = Pattern.compile(
            ".*(what can you (do|help)|how can you help|what do you do|kya kar sakte|help me|get started).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private TaalrCapabilitiesService() {
    }

    public static boolean shouldShowWelcome(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String trimmed = message.trim();
        if (GREETING.matcher(trimmed).matches()) {
            return true;
        }
        return trimmed.length() <= 100 && CAPABILITIES_QUESTION.matcher(trimmed).matches();
    }

    public static String buildWelcomeMessage(TaalrActionChannel channel) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello! I'm Taalr, your Numbrics assistant.\n\n");
        sb.append("Do you want me to do this through chat automation, or guide you to do it manually on the dashboard?\n\n");
        sb.append("Tell me what you need:\n");
        sb.append("1. Invoice — create, send, list, or resend reminder\n");
        sb.append("2. Receipt OCR — scan and save a receipt\n");
        sb.append("3. Sales tax — guidance and questions\n");
        sb.append("4. Something else — general Numbrics help\n\n");
        sb.append("Examples:\n");
        sb.append("• Create invoice for Jane $500 via email jane@example.com\n");
        sb.append("• Show my unpaid invoices\n");
        sb.append("• I want to upload a receipt\n");
        if (channel == TaalrActionChannel.WHATSAPP) {
            sb.append("\nOn WhatsApp you can also send a receipt photo directly.");
        } else {
            sb.append("\nFor receipts in chat, attach an image or use WhatsApp.");
        }
        return sb.toString();
    }
}
