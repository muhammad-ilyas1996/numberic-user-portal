package com.numbericsuserportal.usermanagement.jwt;

import com.numbericsuserportal.stripeintegration.filter.SubscriptionRequiredFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final SubscriptionRequiredFilter subscriptionRequiredFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          SubscriptionRequiredFilter subscriptionRequiredFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.subscriptionRequiredFilter = subscriptionRequiredFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/forgot-password").permitAll() // Forgot password endpoint
                        .requestMatchers("/auth/reset-password").permitAll() // Reset password endpoint
                        .requestMatchers("/auth/forgot-username").permitAll() // Forgot username endpoint
                        .requestMatchers("/webhooks/**").permitAll() // Allow Twilio webhooks without authentication
                        .requestMatchers("/api/auth/register/**").permitAll() // Allow registration endpoints
                        .requestMatchers("/api/taxbandits/**").permitAll() // Allow all TaxBandits APIs (test-auth, form1099nec, and future endpoints)
                        .requestMatchers("/api/public/chat/**").permitAll() // Public website chat endpoint (no JWT)
                        .requestMatchers("/api/subscription/plans").permitAll() // Public pricing plans catalog
                        .requestMatchers("/v1/invoice/pay-by-token").permitAll() // Invoice pay page: get invoice by link token
                        .requestMatchers("/v1/invoice/pay-with-token").permitAll() // Invoice pay: process payment by token
                        .requestMatchers("/pay-invoice", "/pay-invoice.html").permitAll() // Public payment page (link from WhatsApp/email)

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, ex1) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                );

        // JWT auth must run first, then subscription gate enforces business-API access
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(subscriptionRequiredFilter, JwtAuthFilter.class);
        return http.build();
    }
}


