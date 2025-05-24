package com.example.lobbyserver.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

@Schema(description = "Data required to register a new user")
public record UserDao(
        @NotBlank
        @Length(max = 50)
        @Schema(description = "Desired username", example = "Alice", maxLength = 50)
        String username,

        @NotBlank
        @Length(max = 50)
        @Schema(description = "Password for the account", example = "StrongP@ssw0rd", maxLength = 50)
        String password,

        @Email
        @Schema(description = "Valid email address", example = "alice@example.com")
        String email) {
}
