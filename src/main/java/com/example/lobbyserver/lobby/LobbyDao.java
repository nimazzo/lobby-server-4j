package com.example.lobbyserver.lobby;

import com.example.lobbyserver.lobby.db.Lobby;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;

public record LobbyDao(
        @NotNull
        @Positive
        Long id,

        @NotBlank
        String name,

        @Range(min = 0, max = 6)
        int numberOfPlayers,

        @Range(min = 1, max = 6)
        int maxPlayers,

        @NotBlank
        String owner,

        @NotBlank
        String gameServerHost,

        @NotNull
        Integer gameServerPort
) {
    public static LobbyDao fromLobby(Lobby lobby) {
        return new LobbyDao(
                lobby.getId(),
                lobby.getName(),
                lobby.getNumberOfPlayers(),
                lobby.getMaxPlayers(),
                lobby.getOwner().getUsername(),
                lobby.getGameServerHost(),
                lobby.getGameServerPort()
        );
    }
}
