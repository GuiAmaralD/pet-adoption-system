package com.example.auth.Pet;

import com.example.auth.Pet.DTOs.RegisterPetDTO;
import com.example.auth.Pet.DTOs.PetResponseDTO;
import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.User;
import com.example.auth.user.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.auth.Pet.enums.Sex.MALE;
import static com.example.auth.Pet.enums.Size.*;
import static com.example.auth.Pet.enums.Specie.DOG;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pet Service Unit Tests")
class PetServiceUnitTests {

    @Mock
    private PetMapper petMapper;
    @Mock
    private PetRepository petRepository;
    @Mock
    private UserService userService;
    @Mock
    private SupabaseStorageService supabaseStorageService;
    @Mock
    private Principal principal;
    @InjectMocks
    private PetService petService;

    private Pet mockPet;
    private User mockUser;
    private RegisterPetDTO registerPetDTO;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("user@test.com");
        mockUser.setRegisteredPets(new ArrayList<>());

        mockPet = new Pet(
                1L,
                "Rex",
                MALE,
                "Friendly dog",
                MEDIUM,
                new Date(System.currentTimeMillis()),
                false,
                DOG,
                mockUser
        );

        registerPetDTO = new RegisterPetDTO(
                "Rex",
                MALE,
                "Friendly dog",
                DOG,
                MEDIUM
        );
    }

    // ==================== findById() TESTS ====================

    @Test
    @DisplayName("findById should return pet when ID exists")
    void findById_shouldReturnPet_whenIdExists() {
        when(petRepository.findById(1L)).thenReturn(Optional.of(mockPet));

        Pet result = petService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Rex", result.getNickname());
        verify(petRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById should throw ResponseStatusException when ID does not exist")
    void findById_shouldThrowException_whenIdDoesNotExist() {
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> petService.findById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Pet with such Id not found"));
        verify(petRepository, times(1)).findById(999L);
    }

    // ==================== findAllByAdoptedFalse() TESTS ====================

    @Test
    @DisplayName("findAllByAdoptedFalse should return list of available pets")
    void findAllByAdoptedFalse_shouldReturnListOfAvailablePets() {
        List<Pet> mockPets = List.of(mockPet, mockPet);
        when(petRepository.findAllByAdoptedFalse()).thenReturn(mockPets);

        List<PetResponseDTO> result = petService.findAllByAdoptedFalse();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(petRepository, times(1)).findAllByAdoptedFalse();
    }

    @Test
    @DisplayName("findAllByAdoptedFalse should return empty list when no pets available")
    void findAllByAdoptedFalse_shouldReturnEmptyList_whenNoPetsAvailable() {
        when(petRepository.findAllByAdoptedFalse()).thenReturn(List.of());

        List<PetResponseDTO> result = petService.findAllByAdoptedFalse();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(petRepository, times(1)).findAllByAdoptedFalse();
    }

    // ==================== save() TESTS ====================

    @Test
    @DisplayName("save should save and return pet")
    void save_shouldSaveAndReturnPet() {
        when(petRepository.save(any(Pet.class))).thenReturn(mockPet);

        Pet result = petService.save(mockPet);

        assertNotNull(result);
        assertEquals("Rex", result.getNickname());
        verify(petRepository, times(1)).save(mockPet);
    }

    // ==================== toSendPetToClientDTO() TESTS ====================

    @Test
    @DisplayName("toSendPetToClientDTO should convert Pet to PetResponseDTO correctly")
    void toSendPetToClientDTO_shouldConvertCorrectly() {
        mockPet.setImageUrls(List.of("url1.jpg", "url2.jpg"));

        PetResponseDTO result = petMapper.toDTO(mockPet);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Rex", result.nickname());
        assertEquals(MALE, result.sex());
        assertEquals(MEDIUM, result.size());
        assertEquals(DOG, result.specie());
        assertEquals("Friendly dog", result.description());
        assertEquals(mockUser, result.user());
        assertEquals(2, result.imageUrls().size());
    }

    @Test
    @DisplayName("toSendPetToClientDTO should handle pet without images")
    void toSendPetToClientDTO_shouldHandlePetWithoutImages() {
        mockPet.setImageUrls(new ArrayList<>());

        PetResponseDTO result = petMapper.toDTO(mockPet);

        assertNotNull(result);
        assertTrue(result.imageUrls().isEmpty());
    }

    // ==================== isPetFromLoggedUser() TESTS ====================

    @Test
    @DisplayName("isPetFromLoggedUser should return true when pet belongs to logged user")
    void isPetFromLoggedUser_shouldReturnTrue_whenPetBelongsToUser() {
        when(principal.getName()).thenReturn("user@test.com");
        when(userService.findByEmail("user@test.com")).thenReturn(mockUser);
        when(petRepository.findById(1L)).thenReturn(Optional.of(mockPet));

        mockUser.getRegisteredPets().add(mockPet);

        boolean result = petService.isPetFromLoggedUser(1L, principal);

        assertTrue(result);
        verify(userService, times(1)).findByEmail("user@test.com");
        verify(petRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("isPetFromLoggedUser should return false when pet does not belong to logged user")
    void isPetFromLoggedUser_shouldReturnFalse_whenPetDoesNotBelongToUser() {
        when(principal.getName()).thenReturn("user@test.com");
        when(userService.findByEmail("user@test.com")).thenReturn(mockUser);
        when(petRepository.findById(1L)).thenReturn(Optional.of(mockPet));

        boolean result = petService.isPetFromLoggedUser(1L, principal);

        assertFalse(result);
        verify(userService, times(1)).findByEmail("user@test.com");
        verify(petRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("isPetFromLoggedUser should throw exception when pet does not exist")
    void isPetFromLoggedUser_shouldThrowException_whenPetDoesNotExist() {
        when(principal.getName()).thenReturn("user@test.com");
        when(userService.findByEmail("user@test.com")).thenReturn(mockUser);
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> petService.isPetFromLoggedUser(999L, principal));
    }

    // ==================== registerNewPet() SUCCESS TESTS ====================

    @Test
    @DisplayName("registerNewPet should throw BAD_REQUEST when no images provided")
    void registerNewPet_shouldThrowBadRequest_whenNoImagesProvided() {
        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                any(), anyString(), any(), any(), anyString(), any()
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> petService.registerNewPet(registerPetDTO, null, mockUser)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("At least one image is required"));
        verify(petRepository, never()).save(any());
        verify(supabaseStorageService, never()).uploadFile(anyString(), any());
    }

    @Test
    @DisplayName("registerNewPet should throw BAD_REQUEST when empty images list")
    void registerNewPet_shouldThrowBadRequest_whenEmptyImagesList() {
        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                any(), anyString(), any(), any(), anyString(), any()
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> petService.registerNewPet(registerPetDTO, List.of(), mockUser)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("At least one image is required"));
        verify(petRepository, never()).save(any());
        verify(supabaseStorageService, never()).uploadFile(anyString(), any());
    }

    @Test
    @DisplayName("registerNewPet should register pet successfully with valid images")
    void registerNewPet_shouldRegisterSuccessfully_withValidImages() throws IOException {
        MockMultipartFile image1 = new MockMultipartFile(
                "image1",
                "dog1.jpg",
                "image/jpeg",
                "image content 1".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "image2",
                "dog2.png",
                "image/png",
                "image content 2".getBytes()
        );
        List<MultipartFile> images = List.of(image1, image2);

        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                any(), anyString(), any(), any(), anyString(), any()
        )).thenReturn(false);
        when(supabaseStorageService.uploadFile(eq("pet-images"), any(MultipartFile.class)))
                .thenReturn("url1.jpg")
                .thenReturn("url2.png");
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet pet = invocation.getArgument(0);
            pet.setId(1L);
            return pet;
        });

        PetResponseDTO result = petService.registerNewPet(registerPetDTO, images, mockUser);

        assertNotNull(result);
        assertEquals(2, result.imageUrls().size());
        verify(supabaseStorageService, times(2)).uploadFile(eq("pet-images"), any(MultipartFile.class));
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    // ==================== registerNewPet() DUPLICATE VALIDATION ====================

    @Test
    @DisplayName("registerNewPet should throw CONFLICT when duplicate pet exists")
    void registerNewPet_shouldThrowConflict_whenDuplicatePetExists() {
        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                mockUser, "Rex", MEDIUM, DOG, "Friendly dog", MALE
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> petService.registerNewPet(registerPetDTO, null, mockUser)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already registered a pet with identical attributes"));
        verify(petRepository, never()).save(any());
    }

    // ==================== registerNewPet() IMAGE VALIDATION TESTS ====================

    @Test
    @DisplayName("registerNewPet should throw BAD_REQUEST when more than 4 images")
    void registerNewPet_shouldThrowBadRequest_whenMoreThan4Images() {
        List<MultipartFile> images = List.of(
                createMockImage("img1.jpg", "image/jpeg", 1000),
                createMockImage("img2.jpg", "image/jpeg", 1000),
                createMockImage("img3.jpg", "image/jpeg", 1000),
                createMockImage("img4.jpg", "image/jpeg", 1000),
                createMockImage("img5.jpg", "image/jpeg", 1000)
        );

        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                any(), anyString(), any(), any(), anyString(), any()
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> petService.registerNewPet(registerPetDTO, images, mockUser)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("limit of 4 images"));
    }

    @Test
    @DisplayName("registerNewPet should throw BAD_REQUEST when image exceeds 10MB")
    void registerNewPet_shouldThrowBadRequest_whenImageExceeds10MB() {
        MockMultipartFile largeImage = new MockMultipartFile(
                "large",
                "large.jpg",
                "image/jpeg",
                new byte[11 * 1024 * 1024] // 11MB
        );

        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                any(), anyString(), any(), any(), anyString(), any()
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> petService.registerNewPet(registerPetDTO, List.of(largeImage), mockUser)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("too big"));
    }

    @Test
    @DisplayName("registerNewPet should throw BAD_REQUEST for invalid file type")
    void registerNewPet_shouldThrowBadRequest_forInvalidFileType() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "doc",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                any(), anyString(), any(), any(), anyString(), any()
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> petService.registerNewPet(registerPetDTO, List.of(invalidFile), mockUser)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid file type"));
    }

    @Test
    @DisplayName("registerNewPet should throw BAD_REQUEST when duplicate images detected")
    void registerNewPet_shouldThrowBadRequest_whenDuplicateImagesDetected() {
        byte[] sameContent = "identical image content".getBytes();
        MockMultipartFile image1 = new MockMultipartFile(
                "image1",
                "dog1.jpg",
                "image/jpeg",
                sameContent
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "image2",
                "dog2.jpg",
                "image/jpeg",
                sameContent
        );

        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                any(), anyString(), any(), any(), anyString(), any()
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> petService.registerNewPet(registerPetDTO, List.of(image1, image2), mockUser)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("duplicate file detected"));
    }

    @Test
    @DisplayName("registerNewPet should accept all valid image types")
    void registerNewPet_shouldAcceptAllValidImageTypes() throws IOException {
        List<MultipartFile> images = List.of(
                createMockImage("img1.jpg", "image/jpeg", 1000),
                createMockImage("img2.png", "image/png", 1001),
                createMockImage("img3.jpg", "image/jpg", 1002),
                createMockImage("img4.gif", "image/gif", 1003)
        );

        when(petRepository.existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
                any(), anyString(), any(), any(), anyString(), any()
        )).thenReturn(false);
        when(supabaseStorageService.uploadFile(anyString(), any(MultipartFile.class)))
                .thenReturn("url1.jpg", "url2.png", "url3.jpg", "url4.gif");
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet pet = invocation.getArgument(0);
            pet.setId(1L);
            return pet;
        });

        PetResponseDTO result = petService.registerNewPet(registerPetDTO, images, mockUser);

        assertNotNull(result);
        assertEquals(4, result.imageUrls().size());
        verify(supabaseStorageService, times(4)).uploadFile(eq("pet-images"), any(MultipartFile.class));
    }

    // ==================== findByFilter() TESTS ====================//

    @Test
    @DisplayName("findByFilters should return mapped DTOs filtered by specie, sex and size")
    void findByFilters_shouldReturnMappedDTOs_whenFiltersAreApplied() {

        Specie specie = DOG;
        Sex sex = MALE;
        Size size = BIG;

        List<Pet> filteredPets = List.of(
                createPet("Rex",  BIG, DOG, MALE),
                createPet("Thor", BIG, DOG, MALE)
        );

        when(petRepository.findByFilters(specie, sex, size))
                .thenReturn(filteredPets);

        List<PetResponseDTO> result = petService.findByFilters(specie, sex, size);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Rex", result.get(0).nickname()),
                () -> assertEquals("Thor", result.get(1).nickname())
        );

        verify(petRepository).findByFilters(specie, sex, size);
    }

    // ==================== HELPER METHODS ====================

    private MockMultipartFile createMockImage(String name, String contentType, int size) {
        return new MockMultipartFile(
                name,
                name,
                contentType,
                new byte[size]
        );
    }

    private Pet createPet(String nickname, Size size, Specie specie, Sex sex) {
        Pet pet = new Pet();
        pet.setNickname(nickname);
        pet.setSize(size);
        pet.setSpecie(specie);
        pet.setSex(sex);
        return pet;
    }
}