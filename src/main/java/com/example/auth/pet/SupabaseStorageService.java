package com.example.auth.pet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SupabaseStorageService {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String supabaseKey;

    public SupabaseStorageService(
            WebClient.Builder builder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String supabaseKey
    ) {
        this.webClient = builder.build();
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
    }

    public String uploadFile(String bucket, MultipartFile file) {
        try {
            String filePath = UUID.randomUUID() + "-" + file.getOriginalFilename();
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath;

            HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);
            connection.setRequestProperty("Content-Type", file.getContentType());

            try (OutputStream os = connection.getOutputStream()) {
                os.write(file.getBytes());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 201) {
                throw new RuntimeException("Failed to upload file: " + responseCode);
            }

            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filePath;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading to Supabase", e);
        }
    }

    public void deleteFile(String bucket, String filePath) {
        if (filePath == null || filePath.isBlank()) return;

        webClient.delete()
                .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodePath(filePath)))
                .header("Authorization", "Bearer " + supabaseKey)
                .header("apikey", supabaseKey)
                .exchangeToMono(response -> {
                    int code = response.statusCode().value();
                    if (code == 404) return Mono.empty();
                    if (code >= 200 && code < 300) return Mono.empty();
                    return response.createException().flatMap(Mono::error);
                })
                .block();
    }

    public void deleteByPublicUrl(String bucket, String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;

        String prefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (!publicUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("url is not from bucket '" + bucket + "': " + publicUrl);
        }

        String filePath = publicUrl.substring(prefix.length());
        deleteFile(bucket, filePath);
    }


    public void deleteAllByPublicUrls(String bucket, List<String> publicUrls) {
        if (publicUrls == null || publicUrls.isEmpty()) return;

        for (String url : publicUrls) {
            try {
                deleteByPublicUrl(bucket, url);
            } catch (Exception ignored) {
            }
        }
    }

    private String encodePath(String path) {
        return Arrays.stream(path.split("/"))
                .map(seg -> URLEncoder.encode(seg, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }

}
