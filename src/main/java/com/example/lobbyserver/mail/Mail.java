package com.example.lobbyserver.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Mail(
        @NotNull
        @Email
        String from,
        @NotNull
        @Email
        String to,
        @NotBlank
        String subject,
        String text) {
}
