package com.example.lobbyserver.lobby;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record LobbyRequest(
        @NotBlank
        String name,

        @NotNull
        @Range(min = 1, max = 6)
        Integer maxPlayers
) {
}
