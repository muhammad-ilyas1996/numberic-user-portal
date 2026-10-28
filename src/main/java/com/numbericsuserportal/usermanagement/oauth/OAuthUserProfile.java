package com.numbericsuserportal.usermanagement.oauth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthUserProfile {
    private OAuthProvider provider;
    /** Provider stable subject id (Google sub / LinkedIn sub) */
    private String providerUserId;
    private String email;
    private boolean emailVerified;
    private String firstName;
    private String lastName;
    private String fullName;
    private String pictureUrl;
}
