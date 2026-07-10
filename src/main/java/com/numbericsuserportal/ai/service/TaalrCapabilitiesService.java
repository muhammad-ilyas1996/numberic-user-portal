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
        sb.append("You can either:\n");
        sb.append("• Automate in chat (I'll collect required fields with validation), or\n");
        sb.append("• Ask for guidance (I'll explain how to do it on the dashboard)\n\n");
        sb.append("What do you need?\n");
        sb.append("1. Invoice — create, send, list, resend\n");
        sb.append("2. Receipt OCR — scan, edit, categorize, save\n");
        sb.append("3. LLC Formation — draft, name check, prepare\n");
        sb.append("4. Sales tax / general Numbrics help\n\n");
        sb.append("Examples:\n");
        sb.append("• Create invoice for Jane $500 via email jane@example.com\n");
        sb.append("• Show my unpaid invoices\n");
        sb.append("• Upload a receipt / manual receipt / show my receipts\n");
        sb.append("• Start LLC formation in TX\n");
        sb.append("• Guide me on invoices (no automation)\n");
        if (channel == TaalrActionChannel.WHATSAPP) {
            sb.append("\nOn WhatsApp you can also send a receipt photo directly.");
        } else {
            sb.append("\nFor receipts in chat, attach an image or use WhatsApp.");
        }
        return sb.toString();
    }
}
