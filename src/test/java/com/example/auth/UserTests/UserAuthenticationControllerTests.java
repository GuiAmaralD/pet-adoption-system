package com.example.auth.UserTests;

import com.example.auth.infra.security.SecurityConfigurations;
import com.example.auth.infra.security.SecurityFilter;
import com.example.auth.infra.security.TokenService;
import com.example.auth.user.User;
import com.example.auth.user.UserRole;
import com.example.auth.user.controllers.UserAuthenticationController;
import com.example.auth.user.services.UserDetailsServiceImpl;
import com.example.auth.user.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAuthenticationController.class)
@Import({SecurityConfigurations.class, SecurityFilter.class})
@DisplayName("User Authentication Controller Tests")
class UserAuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserService userService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("POST /auth/login should return token when credentials are valid")
    void login_shouldReturnToken_whenCredentialsAreValid() throws Exception {
        User user = new User(1, "User", "user@test.com", "11999999999", "secret", UserRole.USER);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.generateToken(user)).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@test.com",
                                  "password":"secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /auth/login should return NOT_FOUND when email is not registered")
    void login_shouldReturnNotFound_whenEmailIsNotRegistered() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("not found"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"missing@test.com",
                                  "password":"secret"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /auth/login should return NOT_FOUND when auth service cannot find user")
    void login_shouldReturnNotFound_whenAuthServiceCannotFindUser() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new InternalAuthenticationServiceException("not found"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"missing@test.com",
                                  "password":"secret"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /auth/login should return UNAUTHORIZED for bad credentials")
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@test.com",
                                  "password":"wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/register should create user when email is available")
    void register_shouldCreateUser_whenEmailIsAvailable() throws Exception {
        when(userService.isEmailRegistered("new@test.com")).thenReturn(false);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"New User",
                                  "phoneNumber":"11999999999",
                                  "email":"new@test.com",
                                  "password":"plainPassword"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        User savedUser = captor.getValue();
        assertAll(
                () -> assertEquals("New User", savedUser.getName()),
                () -> assertEquals("new@test.com", savedUser.getEmail()),
                () -> assertEquals(UserRole.USER, savedUser.getRole()),
                () -> assertNotEquals("plainPassword", savedUser.getPassword())
        );
    }

    @Test
    @DisplayName("POST /auth/register should return CONFLICT when email is already registered")
    void register_shouldReturnConflict_whenEmailAlreadyRegistered() throws Exception {
        when(userService.isEmailRegistered("existing@test.com")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Existing User",
                                  "phoneNumber":"11999999999",
                                  "email":"existing@test.com",
                                  "password":"plainPassword"
                                }
                                """))
                .andExpect(status().isConflict());

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @DisplayName("POST /auth/register should return BAD_REQUEST for invalid payload")
    void register_shouldReturnBadRequest_whenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"User123",
                                  "phoneNumber":"invalid-phone",
                                  "email":"invalid-email",
                                  "password":"plainPassword"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).save(any(User.class));
    }
}
