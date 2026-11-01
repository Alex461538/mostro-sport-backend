package com.backend.ms_security.service;

public record GoogleTokenInfo(
        String issuer,
        String audience,
        String email,
        Boolean emailVerified,
        String name,
        String picture,
        String subject
) {
    public boolean isValidFor(String expectedAudience) {
        return expectedAudience != null
                && expectedAudience.equals(audience)
                && email != null && !email.isBlank()
                && Boolean.TRUE.equals(emailVerified);
    }
}
