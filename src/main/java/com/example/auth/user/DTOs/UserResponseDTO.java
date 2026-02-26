package com.example.auth.user.DTOs;

import com.example.auth.Pet.DTOs.PetResponseDTO;

import java.util.List;

public record UserResponseDTO(
        String name,
        String email,
        String phoneNumber,
        List<PetResponseDTO> registeredPets
){
}
