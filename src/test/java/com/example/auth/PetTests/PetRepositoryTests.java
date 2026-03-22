package com.example.auth.PetTests;

import com.example.auth.pet.Pet;
import com.example.auth.pet.PetRepository;
import com.example.auth.pet.enums.Sex;
import com.example.auth.pet.enums.Size;
import com.example.auth.pet.enums.Specie;
import com.example.auth.user.User;
import com.example.auth.user.UserRepository;
import com.example.auth.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Pet Repository Tests")
class PetRepositoryTests {

    @Autowired
    private PetRepository petRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findAllByAdoptedFalse should return only not adopted pets")
    void findAllByAdoptedFalse_shouldReturnOnlyNotAdopted() {
        User user = userRepository.save(user());

        Pet adopted = pet(false);
        adopted.setAdopted(true);
        adopted.setUser(user);
        petRepository.save(adopted);

        Pet notAdopted = pet(true);
        notAdopted.setAdopted(false);
        notAdopted.setUser(user);
        petRepository.save(notAdopted);

        List<Pet> result = petRepository.findAllByAdoptedFalse();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isAdopted());
    }

    @Test
    @DisplayName("existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex should return true when match exists")
    void existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex_shouldReturnTrue() {
        User user = userRepository.save(user());
        Pet pet = pet(true);
        pet.setUser(user);
        petRepository.save(pet);

        boolean exists = petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                user,
                pet.getNickname(),
                pet.getSize(),
                pet.getSpecie(),
                pet.getDescription(),
                pet.getSex()
        );

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByIdAndUserId should return true when pet belongs to user")
    void existsByIdAndUserId_shouldReturnTrue_whenPetBelongsToUser() {
        User user = userRepository.save(user());
        Pet pet = pet(true);
        pet.setUser(user);
        Pet saved = petRepository.save(pet);

        assertTrue(petRepository.existsByIdAndUserId(saved.getId(), user.getId()));
        assertFalse(petRepository.existsByIdAndUserId(saved.getId(), 999));
    }

    @Test
    @DisplayName("findByFilters should filter by specie, sex and size")
    void findByFilters_shouldFilterBySpecieSexAndSize() {
        User user = userRepository.save(user());

        Pet match = pet(true);
        match.setUser(user);
        match.setSpecie(Specie.DOG);
        match.setSex(Sex.MALE);
        match.setSize(Size.BIG);
        petRepository.save(match);

        Pet nonMatch = pet(true);
        nonMatch.setUser(user);
        nonMatch.setSpecie(Specie.CAT);
        nonMatch.setSex(Sex.FEMALE);
        nonMatch.setSize(Size.SMALL);
        petRepository.save(nonMatch);

        List<Pet> result = petRepository.findByFilters(Specie.DOG, Sex.MALE, Size.BIG);

        assertEquals(1, result.size());
        assertEquals("Rex", result.get(0).getNickname());
    }

    @Test
    @DisplayName("findByFilters should ignore null filters")
    void findByFilters_shouldIgnoreNullFilters() {
        User user = userRepository.save(user());

        Pet first = pet(true);
        first.setUser(user);
        petRepository.save(first);

        Pet second = pet(true);
        second.setUser(user);
        second.setSpecie(Specie.CAT);
        petRepository.save(second);

        List<Pet> result = petRepository.findByFilters(null, null, null);

        assertEquals(2, result.size());
    }

    private User user() {
        return new User(null, "User", "user@test.com", "11999999999", "secret", UserRole.USER);
    }

    private Pet pet(boolean withDescription) {
        Pet pet = new Pet();
        pet.setNickname("Rex");
        pet.setSex(Sex.MALE);
        pet.setSpecie(Specie.DOG);
        pet.setSize(Size.MEDIUM);
        pet.setDescription(withDescription ? "Friendly" : null);
        pet.setAdopted(false);
        return pet;
    }
}
