package com.example.lobbyserver.game;

import com.example.lobbyserver.game.db.GameResultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/game-results")
public class GameResultsController {

    private final GameResultRepository gameResultRepository;

    public GameResultsController(GameResultRepository gameResultRepository) {
        this.gameResultRepository = gameResultRepository;
    }

    @GetMapping
    public Page<GameResultDao> getGameResults(Pageable pageable, @RequestParam("username") Optional<String> usernameOptional) {
        return usernameOptional.map(username -> gameResultRepository.findByUser_Username(username, pageable))
                .orElseGet(() -> gameResultRepository.findAll(pageable))
                .map(GameResultDao::fromGameResult);
    }
}
