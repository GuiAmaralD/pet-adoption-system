package com.example.auth.UserTests;

import com.example.auth.infra.security.SecurityConfigurations;
import com.example.auth.infra.security.SecurityFilter;
import com.example.auth.infra.security.TokenService;
import com.example.auth.user.User;
import com.example.auth.user.UserMapper;
import com.example.auth.user.UserRole;
import com.example.auth.user.controllers.UserController;
import com.example.auth.user.services.UserDetailsServiceImpl;
import com.example.auth.user.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfigurations.class, SecurityFilter.class})
@DisplayName("User Controller Tests")
class UserControllerTests {

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
    @DisplayName("GET /user/{id} should return user info")
    void getUserById_shouldReturnUserInfo() throws Exception {
        User user = user();
        when(userService.findById(1)).thenReturn(user);
        when(userMapper.toDTO(user)).thenCallRealMethod();

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.phoneNumber").value("11999999999"))
                .andExpect(jsonPath("$.registeredPets.length()").value(0));
    }

    @Test
    @DisplayName("GET /user/{id} should return NOT_FOUND when user does not exist")
    void getUserById_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        when(userService.findById(999))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "User not found"));

        mockMvc.perform(get("/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /user/{id} should be publicly accessible")
    void getUserById_shouldBePublic() throws Exception {
        User user = user();
        when(userService.findById(1)).thenReturn(user);
        when(userMapper.toDTO(user)).thenCallRealMethod();

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk());
    }

    private User user() {
        return new User(1, "User", "user@test.com", "11999999999", "secret", UserRole.USER);
    }
}
