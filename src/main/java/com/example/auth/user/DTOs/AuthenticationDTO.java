package com.example.auth.user.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthenticationDTO(
        @Schema(example = "user@test.com")
        String email,
        @Schema(example = "secret123")
        String password
) {
}
