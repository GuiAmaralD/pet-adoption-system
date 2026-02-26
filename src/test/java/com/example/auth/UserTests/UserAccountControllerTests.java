package com.example.auth.UserTests;

import com.example.auth.infra.security.SecurityConfigurations;
import com.example.auth.infra.security.SecurityFilter;
import com.example.auth.infra.security.TokenService;
import com.example.auth.user.User;
import com.example.auth.user.UserRole;
import com.example.auth.user.UserMapper;
import com.example.auth.user.controllers.UserAccountController;
import com.example.auth.user.services.UserDetailsServiceImpl;
import com.example.auth.user.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAccountController.class)
@Import({SecurityConfigurations.class, SecurityFilter.class})
@DisplayName("User Account Controller Tests")
class UserAccountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("GET /account/me should require authentication")
    void getLoggedUserInfo_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/account/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /account/me should return logged user info")
    @WithMockUser(username = "user@test.com")
    void getLoggedUserInfo_shouldReturnLoggedUserInfo() throws Exception {
        User user = user();
        when(userService.findByEmail("user@test.com")).thenReturn(user);
        when(userMapper.toDTO(user)).thenCallRealMethod();

        mockMvc.perform(get("/account/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.phoneNumber").value("11999999999"))
                .andExpect(jsonPath("$.registeredPets.length()").value(0));
    }

    @Test
    @DisplayName("PUT /account should update logged user")
    @WithMockUser(username = "user@test.com")
    void updateUser_shouldUpdateLoggedUser() throws Exception {
        User loggedUser = user();
        User updated = new User(1, "Updated User", "updated@test.com", "11988887777", "secret", UserRole.USER);

        when(userService.findByEmail("user@test.com")).thenReturn(loggedUser);
        when(userService.updateUser(eq(1), any())).thenReturn(updated);
        when(userMapper.toDTO(updated)).thenCallRealMethod();

        mockMvc.perform(put("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Updated User",
                                  "phoneNumber":"11988887777",
                                  "email":"updated@test.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.phoneNumber").value("11988887777"))
                .andExpect(jsonPath("$.registeredPets.length()").value(0));
    }

    @Test
    @DisplayName("PUT /account should return BAD_REQUEST for invalid payload")
    @WithMockUser(username = "user@test.com")
    void updateUser_shouldReturnBadRequest_whenPayloadIsInvalid() throws Exception {
        mockMvc.perform(put("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"User123",
                                  "phoneNumber":"invalid",
                                  "email":"not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /account/password should update password")
    @WithMockUser(username = "user@test.com")
    void updatePassword_shouldUpdatePassword() throws Exception {
        User user = user();
        when(userService.findByEmail("user@test.com")).thenReturn(user);

        mockMvc.perform(put("/account/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword":"oldPassword",
                                  "newPassword":"newPassword"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Password updated."));

        verify(userService).updatePassword(1, "oldPassword", "newPassword");
    }

    @Test
    @DisplayName("PUT /account/password should return BAD_REQUEST for invalid payload")
    @WithMockUser(username = "user@test.com")
    void updatePassword_shouldReturnBadRequest_whenPayloadIsInvalid() throws Exception {
        mockMvc.perform(put("/account/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword":"",
                                  "newPassword":""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updatePassword(any(), any(), any());
    }

    private User user() {
        return new User(1, "User", "user@test.com", "11999999999", "secret", UserRole.USER);
    }
}
