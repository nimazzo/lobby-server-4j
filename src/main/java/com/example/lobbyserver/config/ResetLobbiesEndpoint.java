package com.example.lobbyserver.config;

import com.example.lobbyserver.game.GameInstanceService;
import com.example.lobbyserver.lobby.LobbyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "reset-lobbies")
public class ResetLobbiesEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ResetLobbiesEndpoint.class);

    private final GameInstanceService gameInstanceService;
    private final LobbyService lobbyService;

    public ResetLobbiesEndpoint(GameInstanceService gameInstanceService, LobbyService lobbyService) {
        this.gameInstanceService = gameInstanceService;
        this.lobbyService = lobbyService;
    }

    @WriteOperation
    public void resetLobbies() {
        log.info("Resetting all active lobbies");
        gameInstanceService.terminateAll();
        lobbyService.deleteAll();
    }

}
