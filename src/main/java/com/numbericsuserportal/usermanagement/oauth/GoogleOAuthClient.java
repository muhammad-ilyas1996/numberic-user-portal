package com.numbericsuserportal.usermanagement.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Verifies Google ID tokens / access tokens via Google's tokeninfo and userinfo endpoints.
 */
@Component
public class GoogleOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.oauth.google.client-id:}")
    private String clientId;

    @Value("${app.oauth.google.enabled:false}")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled && clientId != null && !clientId.isBlank();
    }

    public String getClientId() {
        return clientId;
    }

    public OAuthUserProfile verify(String idToken, String accessToken) {
        if (!isEnabled()) {
            throw new IllegalStateException("Google OAuth is not enabled");
        }
        if (idToken != null && !idToken.isBlank()) {
            return verifyIdToken(idToken.trim());
        }
        if (accessToken != null && !accessToken.isBlank()) {
            return verifyAccessToken(accessToken.trim());
        }
        throw new IllegalArgumentException("Google idToken or accessToken is required");
    }

    @SuppressWarnings("unchecked")
    private OAuthUserProfile verifyIdToken(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || body.get("sub") == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            String aud = stringVal(body.get("aud"));
            if (clientId != null && !clientId.isBlank() && !clientId.equals(aud)) {
                // Also accept aud as comma-separated (rare) or azp
                String azp = stringVal(body.get("azp"));
                if (!clientId.equals(azp)) {
                    throw new IllegalArgumentException("Google ID token audience mismatch");
                }
            }

            String email = stringVal(body.get("email"));
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Google account email is required");
            }

            boolean verified = "true".equalsIgnoreCase(stringVal(body.get("email_verified")));
            if (!verified) {
                throw new IllegalArgumentException("Google email is not verified");
            }

            return OAuthUserProfile.builder()
                    .provider(OAuthProvider.GOOGLE)
                    .providerUserId(stringVal(body.get("sub")))
                    .email(email.trim().toLowerCase())
                    .emailVerified(true)
                    .firstName(firstNonBlank(stringVal(body.get("given_name")), guessFirst(email)))
                    .lastName(firstNonBlank(stringVal(body.get("family_name")), "User"))
                    .fullName(stringVal(body.get("name")))
                    .pictureUrl(stringVal(body.get("picture")))
                    .build();
        } catch (RestClientException e) {
            log.warn("Google ID token verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired Google ID token");
        }
    }

    @SuppressWarnings("unchecked")
    private OAuthUserProfile verifyAccessToken(String accessToken) {
        try {
            // Validate token belongs to our client
            String infoUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + accessToken;
            ResponseEntity<Map> infoResp = restTemplate.getForEntity(infoUrl, Map.class);
            Map<String, Object> info = infoResp.getBody();
            if (info == null) {
                throw new IllegalArgumentException("Invalid Google access token");
            }
            String aud = firstNonBlank(stringVal(info.get("aud")), stringVal(info.get("azp")));
            if (clientId != null && !clientId.isBlank() && aud != null && !clientId.equals(aud)) {
                throw new IllegalArgumentException("Google access token audience mismatch");
            }

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(accessToken);
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            ResponseEntity<Map> userResp = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );
            Map<String, Object> body = userResp.getBody();
            if (body == null || body.get("sub") == null) {
                throw new IllegalArgumentException("Invalid Google access token");
            }

            String email = stringVal(body.get("email"));
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Google account email is required");
            }
            Object verifiedObj = body.get("email_verified");
            boolean verified = verifiedObj instanceof Boolean b ? b : "true".equalsIgnoreCase(String.valueOf(verifiedObj));
            if (!verified) {
                throw new IllegalArgumentException("Google email is not verified");
            }

            return OAuthUserProfile.builder()
                    .provider(OAuthProvider.GOOGLE)
                    .providerUserId(stringVal(body.get("sub")))
                    .email(email.trim().toLowerCase())
                    .emailVerified(true)
                    .firstName(firstNonBlank(stringVal(body.get("given_name")), guessFirst(email)))
                    .lastName(firstNonBlank(stringVal(body.get("family_name")), "User"))
                    .fullName(stringVal(body.get("name")))
                    .pictureUrl(stringVal(body.get("picture")))
                    .build();
        } catch (RestClientException e) {
            log.warn("Google access token verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired Google access token");
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
