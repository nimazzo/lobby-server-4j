package com.example.lobbyserver.lobby;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record GameConnectionDetails(
        @NotBlank
        String hostname,
        @Positive
        int port
) {
}
