package com.example.auth.user.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDTO(

        @NotBlank(message = "name should not be blank")
        @Pattern(regexp = "^[A-Za-zÃ¡Ã Ã¢Ã£Ã©Ã¨ÃªÃ­Ã¯Ã³Ã´ÃµÃ¶ÃºÃ§Ã±ÃÃ€Ã‚ÃƒÃ‰ÃˆÃÃÃ“Ã”Ã•Ã–ÃšÃ‡Ã‘ ]+$", message = "name should contain only letters")
        @Size(max = 55, message = "invalid name length (max = 55)")
        @Schema(example = "Maria Silva")
        String name,
        @Size(min = 8, max = 16, message = "invalid phone number length (min = 8, max = 16)")
        @Pattern(regexp = "^[0-9]*$", message = "invalid phoneNumber entry, only numbers are accepted")
        @Schema(example = "11999999999")
        String phoneNumber,
        @Email(message = "email should be valid")
        @NotBlank(message = "email should not be blank")
        @Schema(example = "maria@email.com")
        String email,
        @Size(max = 30, message = "maximum 30 characters allowed for password")
        @Schema(example = "secret123")
        String password
) {
}
