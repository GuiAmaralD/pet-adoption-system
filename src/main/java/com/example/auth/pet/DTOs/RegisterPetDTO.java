package com.example.auth.pet.DTOs;

import com.example.auth.pet.enums.Sex;
import com.example.auth.pet.enums.Specie;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPetDTO(
        @NotBlank(message = "nickname should not be blank")
        @Size(max = 30, message = "max length for nickname = 30")
        @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "name should contain only letters")
        @Schema(example = "Rex")
        String nickname,
        @Schema(example = "MALE")
        Sex sex,
        @Size(max = 255, message = "limit of 255 characters for description property")
        @Schema(example = "Amigável e brincalhão")
        String description,
        @Schema(example = "DOG")
        Specie specie,
        @Schema(example = "MEDIUM")
        com.example.auth.pet.enums.Size size
) {
}
