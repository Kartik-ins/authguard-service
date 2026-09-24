package com.authguard.authguard_service.controller;

import com.authguard.authguard_service.dto.ProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

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
