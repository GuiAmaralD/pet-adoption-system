package com.example.auth.user;

import com.example.auth.Pet.DTOs.PetResponseDTO;
import com.example.auth.Pet.Pet;
import com.example.auth.user.DTOs.UserResponseDTO;
import com.example.auth.user.DTOs.UserSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                toPetResponseList(user.getRegisteredPets(), user)
        );
    }

    public UserSummaryDTO toSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }

    private List<PetResponseDTO> toPetResponseList(List<Pet> pets, User owner) {
        if (pets == null) {
            return null;
        }
        UserSummaryDTO ownerSummary = toSummaryDTO(owner);
        return pets.stream()
                .map(pet -> new PetResponseDTO(
                        pet.getId(),
                        pet.getNickname(),
                        pet.getSex(),
                        pet.getSize(),
                        pet.getSpecie(),
                        pet.getDescription(),
                        ownerSummary,
                        pet.getImageUrls()
                ))
                .toList();
    }

    public List<UserResponseDTO> toDTOList(List<User> users) {
        return users.stream()
                .map(this::toDTO)
                .toList();
    }
}
