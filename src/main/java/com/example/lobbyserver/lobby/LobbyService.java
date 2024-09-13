package com.example.lobbyserver.lobby;

import com.example.lobbyserver.game.GameInstanceService;
import com.example.lobbyserver.lobby.db.Lobby;
import com.example.lobbyserver.lobby.db.LobbyRepository;
import com.example.lobbyserver.user.db.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class LobbyService {

    private final LobbyRepository lobbyRepository;
    private final GameInstanceService gameInstanceService;
    private final UserRepository userRepository;

    public LobbyService(LobbyRepository lobbyRepository, GameInstanceService gameInstanceService, UserRepository userRepository) {
        this.lobbyRepository = lobbyRepository;
        this.gameInstanceService = gameInstanceService;
        this.userRepository = userRepository;
    }

    public LobbyDao createNewLobby(LobbyRequest lobbyRequest, String username) {
        var owner = userRepository.findByUsername(username);

        var lobbyToCreate = new Lobby(
                null,
                lobbyRequest.name(),
                0,
                lobbyRequest.maxPlayers(),
                owner,
                Set.of(),
                null,
                null,
                null
        );

        var savedLobby = lobbyRepository.save(lobbyToCreate);

        gameInstanceService.startNewGameInstance(savedLobby.getId());

        return LobbyDao.fromLobby(savedLobby);
    }

    public Page<LobbyDao> getOpenLobbies(Pageable pageable) {
        return lobbyRepository.findOpenLobbies(pageable)
                .map(LobbyDao::fromLobby);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public Optional<GameConnectionDetails> tryJoinLobby(Long lobbyId, String username) {
        var lobbyOptional = lobbyRepository.findById(lobbyId);

        return lobbyOptional
                .filter(Lobby::isGameActive)
                .filter(Lobby::isNotFull)
                .map(lobby -> {
                    lobby.setNumberOfPlayers(lobby.getNumberOfPlayers() + 1);
                    var user = userRepository.findByUsername(username);
                    if (lobby.getPlayers().contains(user)) {
                        throw new IllegalStateException("User is already in the lobby");
                    }

                    lobby.addPlayer(user);
                    lobbyRepository.save(lobby);
                    return new GameConnectionDetails(lobby.getGameServerHost(), lobby.getGameServerPort());
                });
    }
}
