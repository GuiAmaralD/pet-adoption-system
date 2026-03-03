package com.example.auth.PetTests;

import com.example.auth.Pet.SupabaseStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Supabase Storage Service Tests")
class SupabaseStorageServiceTests {

    @Test
    @DisplayName("uploadFile should throw RuntimeException when upload fails")
    void uploadFile_shouldThrow_whenUploadFails() {
        SupabaseStorageService service = new SupabaseStorageService();
        ReflectionTestUtils.setField(service, "supabaseUrl", "http://localhost:1");
        ReflectionTestUtils.setField(service, "supabaseKey", "test-key");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        assertThrows(RuntimeException.class, () -> service.uploadFile("pet-images", file));
    }
}
