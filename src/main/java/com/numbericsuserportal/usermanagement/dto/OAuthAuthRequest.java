package com.numbericsuserportal.usermanagement.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for Google / LinkedIn SSO login or registration.
 *
 * <p>Google: send {@code idToken} (preferred) and/or {@code accessToken}.
 * LinkedIn: send {@code accessToken}, or {@code authorizationCode} + {@code redirectUri}
 * for backend code exchange.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthRequest {

    /** LOGIN = existing account only; REGISTER = create if missing (requires register fields). */
    @Pattern(regexp = "^(LOGIN|REGISTER)$", message = "intent must be LOGIN or REGISTER")
    private String intent = "LOGIN";

    /** Google ID token from GIS / One Tap */
    private String idToken;

    /** OAuth access token (Google or LinkedIn) */
    private String accessToken;

    /** LinkedIn authorization code (optional — backend exchanges for token) */
    private String authorizationCode;

    /** Required when using authorizationCode */
    private String redirectUri;

    // ---- Register-only fields (required when intent=REGISTER and user is new) ----

    @Pattern(regexp = "^(Founder|Business_Owner|Accountant_Pro|NUMBRICS_FOUNDER|NUMBRICS_BUSINESS_OWNER|NUMBRICS_ACCOUNTANT_PRO)$",
            message = "Role must be one of: Founder, Business_Owner, Accountant_Pro")
    private String preferredRole;

    /** individual | business */
    private String accountType;

    private Boolean termsAccepted;

    private Boolean marketingOptIn = false;

    private String phone;

    private String companyName;
}
