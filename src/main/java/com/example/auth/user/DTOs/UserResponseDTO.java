package com.example.auth.user.DTOs;

import com.example.auth.Pet.DTOs.PetResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserResponseDTO(
        @Schema(example = "Maria Silva")
        String name,
        @Schema(example = "maria@email.com")
        String email,
        @Schema(example = "11999999999")
        String phoneNumber,
        List<PetResponseDTO> registeredPets
){
}
