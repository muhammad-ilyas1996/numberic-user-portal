package com.numbericsuserportal.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-user Taalr chat limits. Change via {@code application.properties} or env (e.g. {@code APP_CHAT_RATE_LIMIT_MAX_MESSAGES}).
 */
@ConfigurationProperties(prefix = "app.chat.rate-limit")
public class ChatRateLimitProperties {

    private boolean enabled = true;

    /** Max chat requests counted in the sliding window (default: 15 per hour). */
    private int maxMessages = 15;

    /** Sliding window length in minutes (default: 60 = 1 hour). */
    private int windowMinutes = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }
}
