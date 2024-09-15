package com.example.lobbyserver.game;

import com.example.lobbyserver.game.db.GameResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GameResultDao(
        @NotBlank
        String username,
        @NotNull
        Long score,
        @NotNull
        Integer level,
        @NotNull
        Long time,
        @NotNull
        LocalDateTime dateTime
) {
    public static GameResultDao fromGameResult(GameResult gameResult) {
        return new GameResultDao(
                gameResult.getUser().getUsername(),
                gameResult.getScore(),
                gameResult.getLevel(),
                gameResult.getTime(),
                gameResult.getDateTime()
        );
    }
}
