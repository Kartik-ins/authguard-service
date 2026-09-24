package com.authguard.authguard_service.controller;

import com.authguard.authguard_service.config.OpenApiConfig;
import com.authguard.authguard_service.dto.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Protected Resources", description = "Endpoints requiring JWT authentication and subject to Redis sliding-window rate limiting")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(
            summary = "Get current user profile",
            description = "Protected endpoint returning current user identity and role. Enforces Redis sliding-window rate limiting (60 requests / minute) and validates token against Redis blacklist.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted to protected endpoint"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing, invalid, or blacklisted token"),
            @ApiResponse(responseCode = "429", description = "Too Many Requests - Rate limit exceeded")
    })
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = (authentication != null) ? authentication.getName() : "anonymous";
        String role = (authentication != null && authentication.getAuthorities() != null)
                ? authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER")
                : "ROLE_USER";

        ProfileResponse response = new ProfileResponse(
                email,
                role,
                "Access granted to protected endpoint. Token and rate limit successfully validated."
        );

        return ResponseEntity.ok(response);
    }
}
