package com.example.lobbyserver.game;

import com.example.lobbyserver.lobby.db.LobbyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(classes = GameInstanceService.class)
@Import(TaskExecutionAutoConfiguration.class)
@ExtendWith(OutputCaptureExtension.class)
class GameInstanceServiceTest {

    @Autowired
    GameInstanceService gameInstanceService;

    @MockBean
    @SuppressWarnings("unused")
    LobbyRepository lobbyRepository;

    @Test
    void testThatContextLoads() {
        assertThat(gameInstanceService).isNotNull();
    }

    @Test
    @DirtiesContext
    void testThatStartNewGameInstanceGeneratesCorrectOutput(CapturedOutput output) {
        gameInstanceService.startNewGameInstance(1, 2);

        assertThat(output).contains("Game server connected for lobby 1");
        assertThat(output).containsPattern("Game server port for lobby 1 is *");
    }

    @Test
    @DirtiesContext
    void testThatStartingTheSameGameTwiceThrowsException() {
        gameInstanceService.startNewGameInstance(1, 2);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> gameInstanceService.startNewGameInstance(1, 2))
                .withMessage("Game instance for lobby 1 already exists");
    }

    @Test
    void testThatLeavingNonExistingGameThrowsException() {
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> gameInstanceService.playerLeftGame(1L))
                .withMessage("Game instance for lobby 1 does not exist");
    }

    @Test
    void testThatLeavingGameWorks(CapturedOutput output) {
        gameInstanceService.startNewGameInstance(1, 3);

        var remaining = gameInstanceService.playerLeftGame(1L);
        assertThat(remaining).isEqualTo(2);

        remaining = gameInstanceService.playerLeftGame(1L);
        assertThat(remaining).isEqualTo(1);

        remaining = gameInstanceService.playerLeftGame(1L);
        assertThat(remaining).isZero();

        assertThat(output).contains("All players left the game 1, shutting down server");
    }
}