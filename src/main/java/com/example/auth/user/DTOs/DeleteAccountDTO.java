package com.example.auth.user.DTOs;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountDTO(
        @NotBlank(message = "password must not be blank")
        String password
) {
}
