package com.example.auth.Pet;

import com.example.auth.Pet.DTOs.PetResponseDTO;
import com.example.auth.Pet.DTOs.RegisterPetDTO;
import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.User;
import com.example.auth.user.UserMapper;
import com.example.auth.user.DTOs.UserSummaryDTO;
import com.example.auth.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pet Mapper Tests")
class PetMapperTests {

    private final UserMapper userMapper = new UserMapper();
    private final PetMapper petMapper = new PetMapper(userMapper);

    @Test
    @DisplayName("toEntity should map RegisterPetDTO and user to Pet entity")
    void toEntity_shouldMapDtoAndUserToPet() {
        RegisterPetDTO dto = new RegisterPetDTO(
                "Mel",
                Sex.FEMALE,
                "Calm and playful",
                Specie.CAT,
                Size.SMALL
        );
        User user = new User(1, "Ana", "ana@test.com", "11999999999", "secret", UserRole.USER);

        Pet result = petMapper.toEntity(dto, user);

        assertAll(
                () -> assertEquals("Mel", result.getNickname()),
                () -> assertEquals(Sex.FEMALE, result.getSex()),
                () -> assertEquals(Size.SMALL, result.getSize()),
                () -> assertEquals(Specie.CAT, result.getSpecie()),
                () -> assertEquals("Calm and playful", result.getDescription()),
                () -> assertEquals(user, result.getUser())
        );
    }

    @Test
    @DisplayName("toDTO should map Pet entity to PetResponseDTO")
    void toDto_shouldMapPetToResponseDto() {
        User user = new User(2, "Joao", "joao@test.com", "11988888888", "secret", UserRole.USER);
        Pet pet = new Pet();
        pet.setId(10L);
        pet.setNickname("Rex");
        pet.setSex(Sex.MALE);
        pet.setSize(Size.BIG);
        pet.setSpecie(Specie.DOG);
        pet.setDescription("Friendly dog");
        pet.setUser(user);
        pet.setImageUrls(List.of("img1.jpg", "img2.jpg"));

        PetResponseDTO result = petMapper.toDTO(pet);
        UserSummaryDTO expectedUser = userMapper.toSummaryDTO(user);

        assertAll(
                () -> assertEquals(10L, result.id()),
                () -> assertEquals("Rex", result.nickname()),
                () -> assertEquals(Sex.MALE, result.sex()),
                () -> assertEquals(Size.BIG, result.size()),
                () -> assertEquals(Specie.DOG, result.specie()),
                () -> assertEquals("Friendly dog", result.description()),
                () -> assertEquals(expectedUser, result.user()),
                () -> assertEquals(List.of("img1.jpg", "img2.jpg"), result.imageUrls())
        );
    }

    @Test
    @DisplayName("toDTOList should map list of pets to list of response DTOs")
    void toDtoList_shouldMapListOfPets() {
        User user = new User(3, "Maria", "maria@test.com", "11977777777", "secret", UserRole.USER);

        Pet first = new Pet();
        first.setId(1L);
        first.setNickname("Luna");
        first.setSex(Sex.FEMALE);
        first.setSize(Size.MEDIUM);
        first.setSpecie(Specie.DOG);
        first.setDescription("Very active");
        first.setUser(user);
        first.setImageUrls(List.of("luna1.jpg"));

        Pet second = new Pet();
        second.setId(2L);
        second.setNickname("Nina");
        second.setSex(Sex.FEMALE);
        second.setSize(Size.SMALL);
        second.setSpecie(Specie.CAT);
        second.setDescription("Calm");
        second.setUser(user);
        second.setImageUrls(List.of("nina1.jpg"));

        List<PetResponseDTO> result = petMapper.toDTOList(List.of(first, second));

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Luna", result.get(0).nickname()),
                () -> assertEquals("Nina", result.get(1).nickname())
        );
    }
}
