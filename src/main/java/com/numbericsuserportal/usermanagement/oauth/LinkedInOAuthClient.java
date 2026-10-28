package com.numbericsuserportal.usermanagement.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * LinkedIn OpenID Connect: exchange auth code and/or verify access token via userinfo.
 */
@Component
public class LinkedInOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(LinkedInOAuthClient.class);
    private static final String TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    private static final String USERINFO_URL = "https://api.linkedin.com/v2/userinfo";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.oauth.linkedin.client-id:}")
    private String clientId;

    @Value("${app.oauth.linkedin.client-secret:}")
    private String clientSecret;

    @Value("${app.oauth.linkedin.enabled:false}")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled && clientId != null && !clientId.isBlank();
    }

    public String getClientId() {
        return clientId;
    }

    public OAuthUserProfile verify(String accessToken, String authorizationCode, String redirectUri) {
        if (!isEnabled()) {
            throw new IllegalStateException("LinkedIn OAuth is not enabled");
        }
        String token = accessToken;
        if ((token == null || token.isBlank()) && authorizationCode != null && !authorizationCode.isBlank()) {
            token = exchangeCode(authorizationCode.trim(), redirectUri);
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("LinkedIn accessToken or authorizationCode is required");
        }
        return fetchUserInfo(token.trim());
    }

    @SuppressWarnings("unchecked")
    private String exchangeCode(String code, String redirectUri) {
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("LinkedIn client secret is required for authorization code exchange");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalArgumentException("redirectUri is required when using authorizationCode");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "authorization_code");
            form.add("code", code);
            form.add("redirect_uri", redirectUri);
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);

            ResponseEntity<Map> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(form, headers),
                    Map.class
            );
            Map<String, Object> body = response.getBody();
            if (body == null || body.get("access_token") == null) {
                throw new IllegalArgumentException("LinkedIn code exchange failed");
            }
            return String.valueOf(body.get("access_token"));
        } catch (RestClientException e) {
            log.warn("LinkedIn code exchange failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid LinkedIn authorization code");
        }
    }

    @SuppressWarnings("unchecked")
    private OAuthUserProfile fetchUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<Map> response = restTemplate.exchange(
                    USERINFO_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            Map<String, Object> body = response.getBody();
            if (body == null || body.get("sub") == null) {
                throw new IllegalArgumentException("Invalid LinkedIn access token");
            }

            String email = stringVal(body.get("email"));
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("LinkedIn account email is required (request openid email scope)");
            }

            Object verifiedObj = body.get("email_verified");
            boolean verified = verifiedObj == null
                    || (verifiedObj instanceof Boolean b && b)
                    || "true".equalsIgnoreCase(String.valueOf(verifiedObj));
            if (!verified) {
                throw new IllegalArgumentException("LinkedIn email is not verified");
            }

            String fullName = stringVal(body.get("name"));
            String given = stringVal(body.get("given_name"));
            String family = stringVal(body.get("family_name"));
            if ((given == null || given.isBlank()) && fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.trim().split("\\s+", 2);
                given = parts[0];
                family = parts.length > 1 ? parts[1] : "User";
            }

            return OAuthUserProfile.builder()
                    .provider(OAuthProvider.LINKEDIN)
                    .providerUserId(stringVal(body.get("sub")))
                    .email(email.trim().toLowerCase())
                    .emailVerified(true)
                    .firstName(firstNonBlank(given, guessFirst(email)))
                    .lastName(firstNonBlank(family, "User"))
                    .fullName(fullName)
                    .pictureUrl(stringVal(body.get("picture")))
                    .build();
        } catch (RestClientException e) {
            log.warn("LinkedIn userinfo failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired LinkedIn access token");
        }
    }

    private static String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a.trim();
        if (b != null && !b.isBlank()) return b.trim();
        return "";
    }

    private static String guessFirst(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : "User";
    }
}
