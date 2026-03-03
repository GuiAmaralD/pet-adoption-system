package com.example.auth.Pet.DTOs;

import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Specie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePetDTO(
        @NotBlank(message = "nickname should not be blank")
        @Size(max = 30, message = "max length for nickname = 30")
        @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "name should contain only letters")
        String nickname,
        @NotNull(message = "sex must not be null")
        Sex sex,
        @Size(max = 255, message = "limit of 255 characters for description property")
        String description,
        @NotNull(message = "specie must not be null")
        Specie specie,
        @NotNull(message = "size must not be null")
        com.example.auth.Pet.enums.Size size
) {
}
