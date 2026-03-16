package com.example.capgemini_backend.security;

import java.util.List;

public record JwtPrincipal(String email, List<String> roles) {
}
