package com.example.capgemini_backend.auth;

import com.example.capgemini_backend.auth.dto.AuthResponse;
import com.example.capgemini_backend.auth.dto.LoginRequest;
import com.example.capgemini_backend.auth.dto.RegisterRequest;
import com.example.capgemini_backend.security.JwtService;
import com.example.capgemini_backend.user.AppUser;
import com.example.capgemini_backend.user.UserRepository;
import com.example.capgemini_backend.user.UserRole;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        AppUser user = new AppUser();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        AppUser saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getEmail(), List.of(saved.getRole().name()));
        return new AuthResponse(token, "Bearer");
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail(), List.of(user.getRole().name()));
        return new AuthResponse(token, "Bearer");
    }
}
