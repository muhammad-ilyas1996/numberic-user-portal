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

    /** WhatsApp inbound: ack Twilio fast, build/send Taalr reply off the HTTP thread. */
    @Bean(name = "whatsappWebhookExecutor")
    public TaskExecutor whatsappWebhookExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("wa-webhook-");
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(500);
        ex.initialize();
        return ex;
    }
}
