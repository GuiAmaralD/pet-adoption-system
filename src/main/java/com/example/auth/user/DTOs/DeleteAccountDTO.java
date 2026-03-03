package com.example.auth.user.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeleteAccountDTO(
        @NotBlank(message = "password must not be blank")
        @Schema(example = "secret123")
        String password
) {
}
