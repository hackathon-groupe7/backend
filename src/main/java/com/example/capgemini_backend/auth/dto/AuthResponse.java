package com.example.capgemini_backend.auth.dto;

public record AuthResponse(
    String token,
    String tokenType
) {
}
