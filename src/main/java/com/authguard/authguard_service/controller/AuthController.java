package com.authguard.authguard_service.controller;

import com.authguard.authguard_service.dto.AuthResponse;
import com.authguard.authguard_service.dto.LoginRequest;
import com.authguard.authguard_service.dto.MessageResponse;
import com.authguard.authguard_service.dto.RegisterRequest;
import com.authguard.authguard_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token revocation")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register user", description = "Validates constraints, hashes password with BCrypt, saves user in Neon PostgreSQL, and returns a signed JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Validation failed for request body"),
            @ApiResponse(responseCode = "409", description = "Email is already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login user", description = "Verifies email and password credentials, and returns a signed JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Logout user",
            description = "Extracts Bearer token from Authorization header, calculates exact remaining TTL, and blacklists the token in Redis.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token revoked and blacklisted successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header")
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        MessageResponse response = authService.logout(authHeader);
        return ResponseEntity.ok(response);
    }
}
