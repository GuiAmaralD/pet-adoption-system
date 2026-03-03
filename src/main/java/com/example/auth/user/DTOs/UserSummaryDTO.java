package com.example.auth.user.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserSummaryDTO(
        @Schema(example = "Maria Silva")
        String name,
        @Schema(example = "maria@email.com")
        String email,
        @Schema(example = "11999999999")
        String phoneNumber
){
}
