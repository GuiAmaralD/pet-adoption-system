package com.example.auth.Pet.DTOs;

import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.DTOs.UserSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PetResponseDTO(
        @Schema(example = "1")
        Long id,
        @Schema(example = "Rex")
        String nickname,
        @Schema(example = "MALE")
        Sex sex,
        @Schema(example = "MEDIUM")
        Size size,
        @Schema(example = "DOG")
        Specie specie,
        @Schema(example = "Amigável e brincalhão")
        String description,
        UserSummaryDTO user,
        @Schema(example = "[\"https://.../pet-images/uuid-dog.jpg\"]")
        List<String> imageUrls
){}
