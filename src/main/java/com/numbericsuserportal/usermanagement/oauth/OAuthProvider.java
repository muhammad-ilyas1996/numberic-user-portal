package com.numbericsuserportal.usermanagement.oauth;

public enum OAuthProvider {
    GOOGLE,
    LINKEDIN;

    public static OAuthProvider fromPath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Provider is required");
        }
        return switch (path.trim().toLowerCase()) {
            case "google" -> GOOGLE;
            case "linkedin" -> LINKEDIN;
            default -> throw new IllegalArgumentException("Unsupported provider: " + path);
        };
    }
}
