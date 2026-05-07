package com.numbericsuserportal.LlcNorthwest.LLCFormation.catalog;

import com.numbericsuserportal.LlcNorthwest.service.CorporateToolsApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Step 0 — warms Northwest reference data once at startup (websites, default filing products, all-state RA products).
 */
@Component
@Order(20)
public class NorthwestCatalogWarmupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NorthwestCatalogWarmupRunner.class);

    @Value("${llc.northwest.website-url:https://www.numbrics.ai}")
    private String websiteUrl;

    @Autowired
    private CorporateToolsApiService corporateToolsApiService;

    @Autowired
    private NorthwestCatalogCache catalogCache;

    @Override
    public void run(ApplicationArguments args) {
        catalogCache.setLastWarmupError(null);
        StringBuilder errors = new StringBuilder();

        try {
            catalogCache.setWebsites(corporateToolsApiService.getWebsites(websiteUrl));
        } catch (Exception e) {
            appendErr(errors, "websites", e);
        }

        try {
            catalogCache.setFilingProductsAllStates(
                    corporateToolsApiService.getFilingProducts(websiteUrl, null, "Limited Liability Company"));
        } catch (Exception e) {
            appendErr(errors, "filing-products", e);
        }

        try {
            catalogCache.setRegisteredAgentProductsAllStates(
                    corporateToolsApiService.getRegisteredAgentProducts(websiteUrl));
        } catch (Exception e) {
            appendErr(errors, "registered-agent-products", e);
        }

        if (errors.length() > 0) {
            catalogCache.setLastWarmupError(errors.toString());
            log.warn("Northwest catalog warmup partial/failed for websiteUrl={}: {}", websiteUrl, errors);
        } else {
            log.info("Northwest catalog warmup completed for websiteUrl={}", websiteUrl);
        }
    }

    private static void appendErr(StringBuilder sb, String step, Exception e) {
        if (sb.length() > 0) {
            sb.append("; ");
        }
        sb.append(step).append(": ").append(e.getMessage());
    }
}
