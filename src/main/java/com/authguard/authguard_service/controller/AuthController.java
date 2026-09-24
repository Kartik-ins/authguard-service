package com.authguard.authguard_service.controller;

import com.authguard.authguard_service.dto.AuthResponse;
import com.authguard.authguard_service.dto.LoginRequest;
import com.authguard.authguard_service.dto.MessageResponse;
import com.authguard.authguard_service.dto.RegisterRequest;
import com.authguard.authguard_service.service.AuthService;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        MessageResponse response = authService.logout(authHeader);
        return ResponseEntity.ok(response);
    }
}
