package com.example.auth.Pet;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.auth.Pet.DTOs.PetResponseDTO;
import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.infra.security.SecurityConfigurations;
import com.example.auth.infra.security.SecurityFilter;
import com.example.auth.infra.security.TokenService;
import com.example.auth.user.User;
import com.example.auth.user.UserRole;
import com.example.auth.user.services.UserDetailsServiceImpl;
import com.example.auth.user.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetController.class)
@Import({SecurityConfigurations.class, SecurityFilter.class})
@DisplayName("Pet Controller Tests")
class PetControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PetService petService;
    @MockBean
    private UserService userService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("GET /pet should return pet list")
    void findAllByAdoptedFalse_shouldReturnPets() throws Exception {
        when(petService.findAllByAdoptedFalse()).thenReturn(List.of(
                petDto(1L, "Rex"),
                petDto(2L, "Luna")
        ));

        mockMvc.perform(get("/pet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nickname").value("Rex"))
                .andExpect(jsonPath("$[1].nickname").value("Luna"));
    }

    @Test
    @DisplayName("GET /pet should return empty list when no pets exist")
    void findAllByAdoptedFalse_shouldReturnEmptyList() throws Exception {
        when(petService.findAllByAdoptedFalse()).thenReturn(List.of());

        mockMvc.perform(get("/pet"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /pet/filter should return filtered pets")
    void getPetsByCriteria_shouldReturnFilteredPets() throws Exception {
        when(petService.findByFilters(Specie.DOG, Sex.MALE, Size.BIG))
                .thenReturn(List.of(petDto(1L, "Thor")));

        mockMvc.perform(get("/pet/filter")
                        .param("specie", "DOG")
                        .param("sex", "MALE")
                        .param("size", "BIG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nickname").value("Thor"));
    }

    @Test
    @DisplayName("GET /pet/filter should return BAD_REQUEST for invalid enum")
    void getPetsByCriteria_shouldReturnBadRequest_forInvalidEnum() throws Exception {
        mockMvc.perform(get("/pet/filter").param("specie", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /pet/{id} should return pet when ID exists")
    void getPet_shouldReturnPet_whenIdExists() throws Exception {
        when(petService.findByIdAsDto(1L)).thenReturn(petDto(1L, "Rex"));

        mockMvc.perform(get("/pet/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nickname").value("Rex"));
    }

    @Test
    @DisplayName("GET /pet/{id} should return NOT_FOUND when pet does not exist")
    void getPet_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        when(petService.findByIdAsDto(999L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Pet with such Id not found"));

        mockMvc.perform(get("/pet/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /pet should require authentication")
    void registerNewPet_shouldRequireAuthentication() throws Exception {
        MockMultipartFile petPart = new MockMultipartFile(
                "pet",
                "pet.json",
                MediaType.APPLICATION_JSON_VALUE,
                validPetJson()
        );
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "dog.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mockMvc.perform(multipart("/pet")
                        .file(petPart)
                        .file(image))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /pet should return CREATED when request is valid")
    @WithMockUser(username = "user@test.com")
    void registerNewPet_shouldCreatePet_whenRequestIsValid() throws Exception {
        User loggedUser = user();
        when(userService.findByEmail("user@test.com")).thenReturn(loggedUser);
        when(petService.registerNewPet(any(), anyList(), eq(loggedUser)))
                .thenReturn(petDto(10L, "Mel"));

        MockMultipartFile petPart = new MockMultipartFile(
                "pet",
                "pet.json",
                MediaType.APPLICATION_JSON_VALUE,
                validPetJson()
        );
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "dog.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mockMvc.perform(multipart("/pet")
                        .file(petPart)
                        .file(image))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nickname").value("Mel"));
    }

    @Test
    @DisplayName("POST /pet should return BAD_REQUEST when payload is invalid")
    @WithMockUser(username = "user@test.com")
    void registerNewPet_shouldReturnBadRequest_whenPayloadIsInvalid() throws Exception {
        MockMultipartFile invalidPetPart = new MockMultipartFile(
                "pet",
                "pet.json",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "nickname":"Rex123",
                  "sex":"MALE",
                  "description":"Friendly",
                  "specie":"DOG",
                  "size":"MEDIUM"
                }
                """.getBytes()
        );
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "dog.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mockMvc.perform(multipart("/pet")
                        .file(invalidPetPart)
                        .file(image))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /pet should propagate BAD_REQUEST from service")
    @WithMockUser(username = "user@test.com")
    void registerNewPet_shouldPropagateBadRequest_fromService() throws Exception {
        User loggedUser = user();
        when(userService.findByEmail("user@test.com")).thenReturn(loggedUser);
        when(petService.registerNewPet(any(), anyList(), eq(loggedUser)))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "At least one image is required"));

        MockMultipartFile petPart = new MockMultipartFile(
                "pet",
                "pet.json",
                MediaType.APPLICATION_JSON_VALUE,
                validPetJson()
        );
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "dog.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mockMvc.perform(multipart("/pet")
                        .file(petPart)
                        .file(image))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /pet should propagate CONFLICT from service")
    @WithMockUser(username = "user@test.com")
    void registerNewPet_shouldPropagateConflict_fromService() throws Exception {
        User loggedUser = user();
        when(userService.findByEmail("user@test.com")).thenReturn(loggedUser);
        when(petService.registerNewPet(any(), anyList(), eq(loggedUser)))
                .thenThrow(new ResponseStatusException(CONFLICT, "Duplicate pet"));

        MockMultipartFile petPart = new MockMultipartFile(
                "pet",
                "pet.json",
                MediaType.APPLICATION_JSON_VALUE,
                validPetJson()
        );
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "dog.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mockMvc.perform(multipart("/pet")
                        .file(petPart)
                        .file(image))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /pet/{id}/adopted should require authentication")
    void setAdoptedTrue_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(put("/pet/1/adopted"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /pet/{id}/adopted should return OK when pet belongs to logged user")
    @WithMockUser(username = "user@test.com")
    void setAdoptedTrue_shouldReturnOk_whenPetBelongsToLoggedUser() throws Exception {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setAdopted(false);

        when(petService.findById(1L)).thenReturn(pet);
        when(petService.isPetFromLoggedUser(eq(1L), any())).thenReturn(true);

        mockMvc.perform(put("/pet/1/adopted"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pet> petCaptor = ArgumentCaptor.forClass(Pet.class);
        verify(petService).save(petCaptor.capture());
        verify(petService).isPetFromLoggedUser(eq(1L), any());
        org.junit.jupiter.api.Assertions.assertTrue(petCaptor.getValue().isAdopted());
    }

    @Test
    @DisplayName("PUT /pet/{id}/adopted should return UNAUTHORIZED when pet is not from logged user")
    @WithMockUser(username = "user@test.com")
    void setAdoptedTrue_shouldReturnUnauthorized_whenPetIsNotFromLoggedUser() throws Exception {
        Pet pet = new Pet();
        pet.setId(1L);
        when(petService.findById(1L)).thenReturn(pet);
        when(petService.isPetFromLoggedUser(eq(1L), any())).thenReturn(false);

        mockMvc.perform(put("/pet/1/adopted"))
                .andExpect(status().isUnauthorized());

        verify(petService, never()).save(any());
    }

    @Test
    @DisplayName("PUT /pet/{id}/adopted should return NOT_FOUND when pet does not exist")
    @WithMockUser(username = "user@test.com")
    void setAdoptedTrue_shouldReturnNotFound_whenPetDoesNotExist() throws Exception {
        when(petService.findById(999L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Pet with such Id not found"));

        mockMvc.perform(put("/pet/999/adopted"))
                .andExpect(status().isNotFound());

        verify(petService, never()).save(any());
    }

    @Test
    @DisplayName("POST /pet should return FORBIDDEN when bearer token is invalid")
    void registerNewPet_shouldReturnForbidden_whenTokenIsInvalid() throws Exception {
        when(tokenService.validateToken("bad-token"))
                .thenThrow(new JWTVerificationException("JWT was expired or incorrect"));

        MockMultipartFile petPart = new MockMultipartFile(
                "pet",
                "pet.json",
                MediaType.APPLICATION_JSON_VALUE,
                validPetJson()
        );
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "dog.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mockMvc.perform(multipart("/pet")
                        .file(petPart)
                        .file(image)
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isForbidden());
    }


    private byte[] validPetJson() {
        return """
        {
          "nickname": "Mel",
          "sex": "FEMALE",
          "description": "Calm and playful",
          "specie": "CAT",
          "size": "SMALL"
        }
        """.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private PetResponseDTO petDto(Long id, String nickname) {
        return new PetResponseDTO(
                id,
                nickname,
                Sex.MALE,
                Size.MEDIUM,
                Specie.DOG,
                "Friendly",
                null,
                List.of("img.jpg")
        );
    }

    private User user() {
        return new User(1, "User", "user@test.com", "11999999999", "secret", UserRole.USER);
    }
}
