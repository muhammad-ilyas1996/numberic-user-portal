package com.numbericsuserportal.ai.dto;

import lombok.Data;

/**
 * In-app Taalr chat — one user turn per request (history can be added later).
 */
@Data
public class ChatRequestDto {

    /** Current user message */
    private String message;

    /** Optional client session id for future multi-turn persistence */
    private String sessionId;

    /**
     * If {@code true}, clears this user's Taalr Anthropic session mapping and Messages API chat history,
     * then this message runs in a <strong>new</strong> {@code sesn_} session. Use when user wants to restart onboarding or fresh chat.
     */
    private Boolean resetSession;

    /**
     * If {@code false}, the stored business profile is not prepended to this message (Taalr won't see Acme LLC etc.).
     * Default {@code true}. Pair with {@code resetSession} for a clean onboarding re-run; DB profile row is unchanged.
     */
    private Boolean includeProfileInPrompt;
}
