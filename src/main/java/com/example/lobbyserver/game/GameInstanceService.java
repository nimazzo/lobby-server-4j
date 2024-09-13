package com.example.lobbyserver.game;

import com.example.lobbyserver.lobby.db.LobbyRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class GameInstanceService {

    private final LobbyRepository lobbyRepository;

    public GameInstanceService(LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    @Async
    public void startNewGameInstance(long lobbyId) {
        // TODO: change these dummy values to real values
        var host = "localhost";
        var port = 1234;
        lobbyRepository.updateGameServerHostAndGameServerPortById(host, port, lobbyId);
    }

}
