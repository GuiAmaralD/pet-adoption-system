package com.example.auth.SecurityTests;

import com.example.auth.infra.security.TokenService;
import com.example.auth.user.User;
import com.example.auth.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Token Service Tests")
class TokenServiceTests {

    @Test
    @DisplayName("generateToken should create valid token and extract username")
    void generateToken_shouldCreateValidToken() {
        TokenService tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret");

        User user = new User(1L, "User", "user@test.com", "11999999999", "secret", UserRole.USER);

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertTrue(tokenService.validateToken(token));
        assertEquals("user@test.com", tokenService.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("validateToken should throw when token is invalid")
    void validateToken_shouldThrow_whenTokenIsInvalid() {
        TokenService tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret");

        assertThrows(Exception.class, () -> tokenService.validateToken("invalid.token.value"));
    }
}
