package com.example.lobbyserver.actuator;

import com.example.lobbyserver.game.GameInstanceService;
import com.example.lobbyserver.lobby.LobbyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "lobbiesReset")
@SuppressWarnings("unused")
public class ResetLobbiesEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ResetLobbiesEndpoint.class);

    private final GameInstanceService gameInstanceService;
    private final LobbyService lobbyService;

    public ResetLobbiesEndpoint(GameInstanceService gameInstanceService, LobbyService lobbyService) {
        this.gameInstanceService = gameInstanceService;
        this.lobbyService = lobbyService;
    }

    @ReadOperation
    public ResponseEntity<ResetInfo> resetLobbies() {
        log.info("Resetting all active lobbies");
        var deletedGameInstances = gameInstanceService.terminateAll();
        var deletedLobbies = lobbyService.deleteAll();
        return ResponseEntity.ok(new ResetInfo(deletedGameInstances, deletedLobbies));
    }
}
