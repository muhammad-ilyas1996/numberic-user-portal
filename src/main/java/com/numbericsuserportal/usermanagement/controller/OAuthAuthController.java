package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.dto.OAuthAuthRequest;
import com.numbericsuserportal.usermanagement.dto.OAuthAuthResponse;
import com.numbericsuserportal.usermanagement.dto.OAuthProvidersResponse;
import com.numbericsuserportal.usermanagement.oauth.OAuthProvider;
import com.numbericsuserportal.usermanagement.service.OAuthAuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Google + LinkedIn SSO for login and registration.
 *
 * <pre>
 * GET  /auth/oauth/providers
 * POST /auth/oauth/google
 * POST /auth/oauth/linkedin
 * </pre>
 */
@RestController
@RequestMapping("/auth/oauth")
@CrossOrigin(origins = "*")
public class OAuthAuthController {

    private static final Logger log = LoggerFactory.getLogger(OAuthAuthController.class);

    private final OAuthAuthService oAuthAuthService;

    public OAuthAuthController(OAuthAuthService oAuthAuthService) {
        this.oAuthAuthService = oAuthAuthService;
    }

    @GetMapping("/providers")
    public ResponseEntity<OAuthProvidersResponse> providers() {
        return ResponseEntity.ok(oAuthAuthService.getProviders());
    }

    @PostMapping("/{provider}")
    public ResponseEntity<?> authenticate(@PathVariable String provider,
                                          @Valid @RequestBody OAuthAuthRequest request) {
        try {
            OAuthProvider oauthProvider = OAuthProvider.fromPath(provider);
            OAuthAuthResponse response = oAuthAuthService.authenticate(oauthProvider, request);
            HttpStatus status = response.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("OAuth authenticate failed for provider={}", provider, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "SSO authentication failed. Please try again."
            ));
        }
    }
}
