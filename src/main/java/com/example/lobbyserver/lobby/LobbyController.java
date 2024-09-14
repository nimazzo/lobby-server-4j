package com.example.lobbyserver.lobby;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/lobby")
public class LobbyController {

    private static final Logger log = LoggerFactory.getLogger(LobbyController.class);
    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createNewLobby(@Valid @RequestBody LobbyRequest lobby, Authentication auth, UriComponentsBuilder ucb) {
        var owner = auth.getName();

        var savedLobby = lobbyService.createNewLobby(lobby, owner);

        var location = ucb.path("/lobby/{id}")
                .buildAndExpand(savedLobby.id())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public Page<LobbyDao> listAllLobbies(Pageable pageable) {
        return lobbyService.getOpenLobbies(pageable);
    }

    @PostMapping("/join/{lobbyId}")
    public ResponseEntity<GameConnectionDetails> joinLobby(@PathVariable Long lobbyId, Authentication auth) {
        var username = auth.getName();
        log.debug("User {} attempted to joining lobby {}", username, lobbyId);

        return lobbyService.tryJoinLobby(lobbyId, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/leave/{lobbyId}")
    public ResponseEntity<Void> leaveLobby(@PathVariable Long lobbyId, @Valid @RequestBody GameResult result, Authentication auth) {
        var username = auth.getName();
        log.debug("User {} attempted to leave lobby {}", username, lobbyId);

        lobbyService.removePlayerFromLobby(lobbyId, username);
        lobbyService.saveGameResult(lobbyId, username, result);

        return ResponseEntity.ok().build();
    }
}
