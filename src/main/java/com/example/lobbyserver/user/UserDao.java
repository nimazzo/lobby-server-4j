package com.example.lobbyserver.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserDao(
        @NotBlank
        @Length(max = 50)
        String username,

        @NotBlank
        @Length(max = 50)
        String password,

        @Email
        String email) {
}
