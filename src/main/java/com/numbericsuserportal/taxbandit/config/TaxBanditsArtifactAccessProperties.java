package com.numbericsuserportal.taxbandit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Controls access to locally stored TaxBandits PDF artifacts (generated via webhook ingestion).
 */
@Component
@ConfigurationProperties(prefix = "taxbandits.artifact")
public class TaxBanditsArtifactAccessProperties {

    /**
     * When true, downloads require header {@code X-Numbrics-Artifact-Key} matching {@link #apiKey}.
     * Recommended: true in production once apiKey is configured.
     */
    private boolean requireApiKey = true;

    /**
     * Shared secret for artifact downloads (set via env/config). If blank and requireApiKey=true,
     * downloads will be disabled until configured.
     */
    private String apiKey = "";

    public boolean isRequireApiKey() {
        return requireApiKey;
    }

    public void setRequireApiKey(boolean requireApiKey) {
        this.requireApiKey = requireApiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
