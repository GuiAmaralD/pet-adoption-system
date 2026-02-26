package com.example.auth.UserTests;

import com.example.auth.user.DTOs.UpdateDTO;
import com.example.auth.user.User;
import com.example.auth.user.UserRepository;
import com.example.auth.user.UserRole;
import com.example.auth.user.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("findById should return user when id exists")
    void findById_shouldReturnUser_whenIdExists() {
        User user = user();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User result = userService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("user@test.com", result.getEmail());
    }

    @Test
    @DisplayName("findById should throw NOT_FOUND when id does not exist")
    void findById_shouldThrowNotFound_whenIdDoesNotExist() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.findById(999)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("isEmailRegistered should return repository result")
    void isEmailRegistered_shouldReturnRepositoryResult() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);
        when(userRepository.existsByEmail("missing@test.com")).thenReturn(false);

        assertTrue(userService.isEmailRegistered("user@test.com"));
        assertFalse(userService.isEmailRegistered("missing@test.com"));
    }

    @Test
    @DisplayName("findByEmail should return user details when email exists")
    void findByEmail_shouldReturnUserDetails_whenEmailExists() {
        User user = user();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userService.findByEmail("user@test.com");

        assertNotNull(result);
        assertEquals("user@test.com", result.getUsername());
    }

    @Test
    @DisplayName("findByEmail should throw NOT_FOUND when email does not exist")
    void findByEmail_shouldThrowNotFound_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.findByEmail("missing@test.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("save should persist and return user")
    void save_shouldPersistAndReturnUser() {
        User user = user();
        when(userRepository.save(user)).thenReturn(user);

        UserDetails result = userService.save(user);

        assertNotNull(result);
        assertEquals("user@test.com", result.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser should update fields and save user")
    void updateUser_shouldUpdateFieldsAndSave() {
        User user = user();
        UpdateDTO dto = new UpdateDTO("Updated Name", "11988887777", "updated@test.com");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1, dto);

        assertAll(
                () -> assertEquals("Updated Name", updated.getName()),
                () -> assertEquals("11988887777", updated.getPhoneNumber()),
                () -> assertEquals("updated@test.com", updated.getEmail())
        );
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updatePassword should throw CONFLICT when old password is incorrect")
    void updatePassword_shouldThrowConflict_whenOldPasswordIsIncorrect() {
        User user = user();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.updatePassword(1, "wrong", "newPassword")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updatePassword should throw CONFLICT when old and new passwords are equal")
    void updatePassword_shouldThrowConflict_whenOldAndNewPasswordsAreEqual() {
        User user = user();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("samePassword", user.getPassword())).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.updatePassword(1, "samePassword", "samePassword")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updatePassword should encode and save new password")
    void updatePassword_shouldEncodeAndSaveNewPassword() {
        User user = user();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded-new-password");

        userService.updatePassword(1, "oldPassword", "newPassword");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encoded-new-password", captor.getValue().getPassword());
    }

    private User user() {
        return new User(1, "User", "user@test.com", "11999999999", "encoded-old", UserRole.USER);
    }
}
