package com.example.lobbyserver.lobby;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/lobby")
public class LobbyController {

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

    @GetMapping("/join/{lobbyId}")
    public ResponseEntity<GameConnectionDetails> joinLobby(@PathVariable Long lobbyId, Authentication auth) {
        var username = auth.getName();

        return lobbyService.tryJoinLobby(lobbyId, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

}
