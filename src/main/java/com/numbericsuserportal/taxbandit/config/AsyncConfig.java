package com.numbericsuserportal.taxbandit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Enables async processing for TaxBandits webhook work (download/unzip) without blocking the HTTP thread.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taxBanditsWebhookExecutor")
    public TaskExecutor taxBanditsWebhookExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("tb-webhook-");
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(8);
        ex.setQueueCapacity(200);
        ex.initialize();
        return ex;
    }
}
