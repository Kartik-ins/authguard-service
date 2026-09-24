package com.authguard.authguard_service.service;

import com.authguard.authguard_service.dto.AuthResponse;
import com.authguard.authguard_service.dto.LoginRequest;
import com.authguard.authguard_service.dto.MessageResponse;
import com.authguard.authguard_service.dto.RegisterRequest;
import com.authguard.authguard_service.entity.Role;
import com.authguard.authguard_service.entity.User;
import com.authguard.authguard_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public MessageResponse logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        long remainingTtlMs = jwtService.getRemainingTtlMs(token);

        tokenBlacklistService.blacklistToken(token, remainingTtlMs);
        return new MessageResponse("Logged out successfully. Token invalidated.");
    }
}
