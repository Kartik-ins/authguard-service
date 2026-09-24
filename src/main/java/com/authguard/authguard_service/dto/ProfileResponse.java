package com.authguard.authguard_service.dto;

public record ProfileResponse(
        String email,
        String role,
        String message
) {
}
