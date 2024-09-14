package com.example.lobbyserver.lobby;

import com.example.lobbyserver.game.GameInstanceService;
import com.example.lobbyserver.lobby.db.Lobby;
import com.example.lobbyserver.lobby.db.LobbyRepository;
import com.example.lobbyserver.user.db.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LobbyService {

    private static final Logger log = LoggerFactory.getLogger(LobbyService.class);
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
                lobbyRequest.name(),
                lobbyRequest.maxPlayers(),
                owner
        );

        var savedLobby = lobbyRepository.save(lobbyToCreate);

        gameInstanceService.startNewGameInstance(savedLobby.getId(), savedLobby.getMaxPlayers());

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
                .filter(Lobby::notStarted)
                .map(lobby -> {
                    lobby.setNumberOfPlayers(lobby.getNumberOfPlayers() + 1);
                    var user = userRepository.findByUsername(username);
                    if (lobby.getPlayers().contains(user)) {
                        throw new IllegalStateException("User is already in the lobby");
                    }

                    lobby.addPlayer(user);
                    log.debug("User {} joined lobby {}", username, lobbyId);

                    if (lobby.isFull()) {
                        lobby.setGameStarted(true);
                        log.debug("Lobby {} is now full", lobbyId);
                    }

                    lobbyRepository.save(lobby);

                    return new GameConnectionDetails(lobby.getGameServerHost(), lobby.getGameServerPort());
                });
    }

    public void removePlayerFromLobby(Long lobbyId, String username) {
        var lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found"));
        if (!lobby.containsUser(username)) {
            throw new IllegalStateException("User is not in the lobby");
        }

        var remaining = gameInstanceService.playerLeftGame(lobbyId);
        log.debug("User {} left lobby {}", username, lobbyId);
        if (remaining == 0) {
            log.debug("Lobby {} is now empty, deleting from database", lobbyId);
            lobbyRepository.delete(lobby);
        } else {
            log.debug("Removing user {} from users-lobby databse for lobby {}", username, lobbyId);
            var user = userRepository.findByUsername(username);
            lobby.removePlayer(user);
            lobbyRepository.save(lobby);
        }
    }

    public void saveGameResult(Long lobbyId, String username, @Valid GameResult result) {

    }
}
