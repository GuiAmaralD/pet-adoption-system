package com.example.auth.UserTests;

import com.example.auth.user.User;
import com.example.auth.user.UserRepository;
import com.example.auth.user.UserRole;
import com.example.auth.Pet.Pet;
import com.example.auth.Pet.PetRepository;
import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("User Repository Tests")
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PetRepository petRepository;

    @Test
    @DisplayName("findById should return user when it exists")
    void findById_shouldReturnUser_whenExists() {
        User saved = userRepository.save(user());

        Optional<User> result = userRepository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getEmail(), result.get().getEmail());
    }

    @Test
    @DisplayName("findByEmail should return user details when email exists")
    void findByEmail_shouldReturnUserDetails_whenExists() {
        User saved = userRepository.save(user());

        Optional<UserDetails> result = userRepository.findByEmail(saved.getEmail());

        assertTrue(result.isPresent());
        assertEquals(saved.getEmail(), result.get().getUsername());
    }

    @Test
    @DisplayName("existsByEmail should return true when email exists")
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        User saved = userRepository.save(user());

        assertTrue(userRepository.existsByEmail(saved.getEmail()));
        assertFalse(userRepository.existsByEmail("missing@test.com"));
    }

    @Test
    @Transactional
    @DisplayName("delete should cascade and remove user's pets")
    void delete_shouldCascadeAndRemovePets() {
        User savedUser = userRepository.save(user());

        Pet pet1 = pet("Rex");
        pet1.setUser(savedUser);
        Pet pet2 = pet("Luna");
        pet2.setUser(savedUser);
        savedUser.getRegisteredPets().addAll(List.of(pet1, pet2));

        petRepository.save(pet1);
        petRepository.save(pet2);

        userRepository.delete(savedUser);
        userRepository.flush();

        assertTrue(petRepository.findAll().isEmpty());
    }

    private User user() {
        return new User(null, "User", "user@test.com", "11999999999", "secret", UserRole.USER);
    }

    private Pet pet(String nickname) {
        Pet pet = new Pet();
        pet.setNickname(nickname);
        pet.setSex(Sex.MALE);
        pet.setSpecie(Specie.DOG);
        pet.setSize(Size.MEDIUM);
        pet.setDescription("Friendly");
        pet.setAdopted(false);
        return pet;
    }
}
