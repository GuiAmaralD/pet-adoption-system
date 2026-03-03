package com.example.auth.user.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @NotBlank(message = "oldpassword must not be blank")
        @Schema(example = "oldPassword123")
        String oldPassword,
        @Size(max = 30, message = "maximum 30 characters allowed for password")
        @NotBlank(message = "new password must not be blank")
        @Schema(example = "newPassword123")
        String newPassword
) {
}
