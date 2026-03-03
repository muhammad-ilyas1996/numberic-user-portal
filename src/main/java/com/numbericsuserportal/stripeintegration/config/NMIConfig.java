package com.numbericsuserportal.stripeintegration.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * NMI Payment Gateway Configuration
 * Based on NMI official documentation
 */
@Configuration
public class NMIConfig {

    @Value("${nmi.api.username:}")
    private String nmiUsername;

    @Value("${nmi.api.password:}")
    private String nmiPassword;

    @Value("${nmi.api.key:}")
    private String nmiApiKey;

    @Value("${nmi.transaction.url:https://secure.networkmerchants.com/api/transact.php}")
    private String nmiTransactionUrl;

    @Value("${nmi.environment:sandbox}")
    private String nmiEnvironment; // sandbox or production

    /** Optional: api_key to force API key; omit or username_password to use username+password when both are set. */
    @Value("${nmi.auth.method:}")
    private String nmiAuthMethod;

    @PostConstruct
    public void init() {
        // Validate NMI configuration
        if ((nmiUsername == null || nmiUsername.isEmpty()) && 
            (nmiApiKey == null || nmiApiKey.isEmpty())) {
            System.out.println("Warning: NMI credentials not configured. NMI payment gateway will not work.");
        } else {
            System.out.println("NMI Payment Gateway configured for: " + nmiEnvironment);
        }
    }

    public String getNmiUsername() {
        return nmiUsername;
    }

    public String getNmiPassword() {
        return nmiPassword;
    }

    public String getNmiApiKey() {
        return nmiApiKey;
    }

    public String getNmiTransactionUrl() {
        return nmiTransactionUrl;
    }

    public String getNmiEnvironment() {
        return nmiEnvironment;
    }

    /** Use username+password when both are set (default). Set nmi.auth.method=api_key to use API key instead. */
    public boolean isUseUsernamePassword() {
        if (nmiAuthMethod != null && !nmiAuthMethod.trim().isEmpty()) {
            return "username_password".equalsIgnoreCase(nmiAuthMethod.trim());
        }
        return nmiUsername != null && !nmiUsername.isEmpty()
            && nmiPassword != null && !nmiPassword.isEmpty();
    }

    public boolean isConfigured() {
        return (nmiUsername != null && !nmiUsername.isEmpty() && 
                nmiPassword != null && !nmiPassword.isEmpty()) ||
               (nmiApiKey != null && !nmiApiKey.isEmpty());
    }
}

