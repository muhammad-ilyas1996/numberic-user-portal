package com.numbericsuserportal.taxbandit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Filesystem storage for TaxBandits webhook artifacts (zip + extracted PDFs).
 */
@Component
@ConfigurationProperties(prefix = "taxbandits.webhook.storage")
public class TaxBanditsWebhookStorageProperties {

    /**
     * Base directory (relative to working directory unless absolute path is provided).
     */
    private String baseDir = "uploads/taxbandits/webhooks";

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
