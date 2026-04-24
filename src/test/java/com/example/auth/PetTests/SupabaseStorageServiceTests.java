package com.example.auth.PetTests;

import com.example.auth.pet.SupabaseStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Supabase Storage Service Tests")
class SupabaseStorageServiceTests {

    @Test
    @DisplayName("uploadFile should throw RuntimeException when upload fails")
    void uploadFile_shouldThrow_whenUploadFails() {
        SupabaseStorageService service = new SupabaseStorageService(
                WebClient.builder(),
                "http://localhost:1",
                "test-key"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        assertThrows(RuntimeException.class, () -> service.uploadFile("pet-images", file));
    }

    @Test
    @DisplayName("deleteFile should ignore 404")
    void deleteFile_shouldIgnoreNotFound() {
        SupabaseStorageService service = new SupabaseStorageService(
                webClientBuilderReturning(HttpStatus.NOT_FOUND),
                "https://supabase.test",
                "test-key"
        );

        assertDoesNotThrow(() -> service.deleteFile("pet-images", "missing.jpg"));
    }

    @Test
    @DisplayName("deleteFile should succeed on 204")
    void deleteFile_shouldSucceedOnNoContent() {
        SupabaseStorageService service = new SupabaseStorageService(
                webClientBuilderReturning(HttpStatus.NO_CONTENT),
                "https://supabase.test",
                "test-key"
        );

        assertDoesNotThrow(() -> service.deleteFile("pet-images", "exists.jpg"));
    }

    @Test
    @DisplayName("deleteFile should throw on 500")
    void deleteFile_shouldThrowOnServerError() {
        SupabaseStorageService service = new SupabaseStorageService(
                webClientBuilderReturning(HttpStatus.INTERNAL_SERVER_ERROR),
                "https://supabase.test",
                "test-key"
        );

        assertThrows(WebClientResponseException.class,
                () -> service.deleteFile("pet-images", "broken.jpg"));
    }

    @Test
    @DisplayName("deleteByPublicUrl should validate bucket prefix")
    void deleteByPublicUrl_shouldValidateBucketPrefix() {
        SupabaseStorageService service = new SupabaseStorageService(
                webClientBuilderReturning(HttpStatus.NO_CONTENT),
                "https://supabase.test",
                "test-key"
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteByPublicUrl("pet-images", "https://other.test/storage/v1/object/public/other-bucket/file.jpg"));
    }

    @Test
    @DisplayName("deleteByPublicUrl should delete encoded path")
    void deleteByPublicUrl_shouldDeleteEncodedPath() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            captured.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
        };

        SupabaseStorageService service = new SupabaseStorageService(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "https://supabase.test",
                "test-key"
        );

        service.deleteByPublicUrl("pet-images", "https://supabase.test/storage/v1/object/public/pet-images/folder name/file 1.jpg");

        String rawUrl = captured.get().url().toString();
        String decodedUrl = URLDecoder.decode(rawUrl, StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("folder name/file 1.jpg"),
                () -> "rawUrl=" + rawUrl + " decodedUrl=" + decodedUrl);
    }

    @Test
    @DisplayName("deleteAllByPublicUrls should ignore failures and continue")
    void deleteAllByPublicUrls_shouldIgnoreFailures() {
        AtomicInteger calls = new AtomicInteger(0);
        ExchangeFunction exchangeFunction = request -> {
            int count = calls.incrementAndGet();
            HttpStatus status = (count == 1) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.NO_CONTENT;
            return Mono.just(ClientResponse.create(status).build());
        };

        SupabaseStorageService service = new SupabaseStorageService(
                WebClient.builder().exchangeFunction(exchangeFunction),
                "https://supabase.test",
                "test-key"
        );

        assertDoesNotThrow(() -> service.deleteAllByPublicUrls("pet-images", List.of(
                "https://supabase.test/storage/v1/object/public/pet-images/a.jpg",
                "https://supabase.test/storage/v1/object/public/pet-images/b.jpg"
        )));

        assertEquals(2, calls.get());
    }


    private WebClient.Builder webClientBuilderReturning(HttpStatus status) {
        ExchangeFunction exchangeFunction = request ->
                Mono.just(ClientResponse.create(status).build());
        return WebClient.builder().exchangeFunction(exchangeFunction);
    }
}
