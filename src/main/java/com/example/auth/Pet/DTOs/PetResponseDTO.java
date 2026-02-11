package com.example.auth.Pet.DTOs;


import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.User;

import java.util.List;

public record PetResponseDTO(
        Long id,
        String nickname,
        Sex sex,
        Size size,
        Specie specie,
        String description,
        User user,
        List<String> imageUrls
){}
