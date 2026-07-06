package com.numbericsuserportal.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Feature flags and tuning for Taalr automated actions (receipt OCR, invoice create/send).
 */
@ConfigurationProperties(prefix = "taalr.actions")
public class TaalrActionProperties {

    /** Master switch for receipt + invoice automation on app chat and WhatsApp. */
    private boolean enabled = true;

    /** Pending action session TTL in minutes (YES confirm, invoice draft, etc.). */
    /** Pending automation state TTL (invoice draft, receipt confirm, etc.). */
    private int sessionTtlMinutes = 120;

    /** Model for lightweight intent parsing (falls back to anthropic.api.model if blank). */
    private String intentModel = "";

    /** Max tokens for intent JSON response. */
    private int intentMaxTokens = 1024;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSessionTtlMinutes() {
        return sessionTtlMinutes;
    }

    public void setSessionTtlMinutes(int sessionTtlMinutes) {
        this.sessionTtlMinutes = sessionTtlMinutes;
    }

    public String getIntentModel() {
        return intentModel;
    }

    public void setIntentModel(String intentModel) {
        this.intentModel = intentModel;
    }

    public int getIntentMaxTokens() {
        return intentMaxTokens;
    }

    public void setIntentMaxTokens(int intentMaxTokens) {
        this.intentMaxTokens = intentMaxTokens;
    }
}
