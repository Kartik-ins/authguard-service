package com.authguard.authguard_service.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        String role,
        String message
) {
    public AuthResponse(String token, String email, String role) {
        this(token, "Bearer", email, role, "Authentication successful");
    }
}
