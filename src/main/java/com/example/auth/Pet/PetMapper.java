package com.example.auth.Pet;


import com.example.auth.Pet.DTOs.PetResponseDTO;
import com.example.auth.Pet.DTOs.RegisterPetDTO;
import com.example.auth.user.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PetMapper {

    public Pet toEntity(RegisterPetDTO dto, User user) {
        Pet pet = new Pet();
        pet.setNickname(dto.nickname());
        pet.setSex(dto.sex());
        pet.setSize(dto.size());
        pet.setSpecie(dto.specie());
        pet.setDescription(dto.description());
        pet.setUser(user);
        return pet;
    }

    public PetResponseDTO toDTO(Pet pet) {
        return new PetResponseDTO(
                pet.getId(),
                pet.getNickname(),
                pet.getSex(),
                pet.getSize(),
                pet.getSpecie(),
                pet.getDescription(),
                pet.getUser(),
                pet.getImageUrls()
        );
    }

    public List<PetResponseDTO> toDTOList(List<Pet> pets) {
        return pets.stream()
                .map(this::toDTO)
                .toList();
    }
}
