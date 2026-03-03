package com.example.auth.UserTests;

import com.example.auth.Pet.Pet;
import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.DTOs.UserResponseDTO;
import com.example.auth.user.DTOs.UserSummaryDTO;
import com.example.auth.user.User;
import com.example.auth.user.UserMapper;
import com.example.auth.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Mapper Tests")
class UserMapperTests {

    private final UserMapper userMapper = new UserMapper();

    @Test
    @DisplayName("toSummaryDTO should map basic user fields")
    void toSummaryDTO_shouldMapBasicFields() {
        User user = user();

        UserSummaryDTO dto = userMapper.toSummaryDTO(user);

        assertAll(
                () -> assertEquals("User", dto.name()),
                () -> assertEquals("user@test.com", dto.email()),
                () -> assertEquals("11999999999", dto.phoneNumber())
        );
    }

    @Test
    @DisplayName("toDTO should map user and pets with owner summary")
    void toDTO_shouldMapUserAndPets() {
        User user = user();
        Pet pet = pet(user, 1L, "Rex");
        user.setRegisteredPets(List.of(pet));

        UserResponseDTO dto = userMapper.toDTO(user);

        assertAll(
                () -> assertEquals("User", dto.name()),
                () -> assertEquals("user@test.com", dto.email()),
                () -> assertEquals("11999999999", dto.phoneNumber()),
                () -> assertNotNull(dto.registeredPets()),
                () -> assertEquals(1, dto.registeredPets().size()),
                () -> assertEquals("Rex", dto.registeredPets().get(0).nickname()),
                () -> assertNotNull(dto.registeredPets().get(0).user()),
                () -> assertEquals("User", dto.registeredPets().get(0).user().name())
        );
    }

    @Test
    @DisplayName("toDTOList should map list of users")
    void toDTOList_shouldMapUserList() {
        User first = user();
        User second = new User(2, "Other", "other@test.com", "11888888888", "secret", UserRole.USER);

        List<UserResponseDTO> result = userMapper.toDTOList(List.of(first, second));

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("user@test.com", result.get(0).email()),
                () -> assertEquals("other@test.com", result.get(1).email())
        );
    }

    private User user() {
        return new User(1, "User", "user@test.com", "11999999999", "secret", UserRole.USER);
    }

    private Pet pet(User owner, Long id, String nickname) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setNickname(nickname);
        pet.setSex(Sex.MALE);
        pet.setSize(Size.MEDIUM);
        pet.setSpecie(Specie.DOG);
        pet.setDescription("Friendly");
        pet.setUser(owner);
        pet.setImageUrls(List.of("img.jpg"));
        return pet;
    }
}
