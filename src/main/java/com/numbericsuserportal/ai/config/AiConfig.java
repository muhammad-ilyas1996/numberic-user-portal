package com.numbericsuserportal.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({ AnthropicProperties.class, ChatRateLimitProperties.class, TaalrActionProperties.class })
public class AiConfig {

    /**
     * Hard timeouts — without these a hung Anthropic call can block WhatsApp replies for many minutes.
     */
    @Bean
    public RestClient anthropicRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .requestFactory(factory)
                .build();
    }
}
