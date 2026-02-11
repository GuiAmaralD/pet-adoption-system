package com.example.auth.Pet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

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
}