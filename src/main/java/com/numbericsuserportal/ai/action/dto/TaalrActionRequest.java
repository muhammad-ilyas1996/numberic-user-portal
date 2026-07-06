package com.numbericsuserportal.ai.action.dto;

import com.numbericsuserportal.ai.action.TaalrActionChannel;
import com.numbericsuserportal.ai.action.TaalrChatMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaalrActionRequest {

    private TaalrChatMode mode;
    private TaalrActionChannel channel;
    private Long userId;
    private String phoneNumber;
    private String message;

    /** Twilio media URL (WhatsApp) */
    private String mediaUrl;
    private String mediaContentType;

    /** Base64 image bytes without data-URI prefix (app chat) */
    private String mediaBase64;
    private String mediaFileName;
}
