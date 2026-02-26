package com.example.auth.user.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record UpdateDTO(
        @NotBlank(message = "name should not be blank")
        @Pattern(regexp = "^[A-Za-záàâãéèêíïóôõöúçñÁÀÂÃÉÈÍÏÓÔÕÖÚÇÑ ]+$", message = "name should contain only letters")
        @Size(max = 55, message = "invalid name length (max = 55)")
        String name,
        @Size(min= 8, max=16, message = "invalid phone number length (min = 8, max = 16)")
        @Pattern(regexp = "^[0-9]*$", message = "invalid phoneNumber entry, only numbers are accepted")
        String phoneNumber,
        @Email(message = "email should be valid")
        @NotBlank(message = "email should not be blank")
        String email
) {
}
