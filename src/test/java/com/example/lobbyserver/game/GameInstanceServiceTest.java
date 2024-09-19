package com.example.lobbyserver.game;

import com.example.lobbyserver.lobby.db.LobbyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = GameInstanceService.class)
@Import(TaskExecutionAutoConfiguration.class)
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
class GameInstanceServiceTest {

    @Autowired
    GameInstanceService gameInstanceService;

    @MockBean
    @SuppressWarnings("unused")
    LobbyRepository lobbyRepository;

    @MockBean
    @SuppressWarnings("unused")
    ServerLogsService serverLogsService;

    File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("temp-log", ".log").toFile();
        given(serverLogsService.createLogFileForLobby(anyLong()))
                .willReturn(tempFile);
    }

    @AfterEach
    void tearDown() {
        var _ = tempFile.delete();
    }

    @Test
    void testThatContextLoads() {
        assertThat(gameInstanceService).isNotNull();
    }

    @Test
    @DirtiesContext
    void testThatStartNewGameInstanceGeneratesCorrectOutput(CapturedOutput output) throws IOException {
        gameInstanceService.startNewGameInstance(1, 2);

        assertThat(output).contains("Game server connected for lobby 1");
        assertThat(output).containsPattern("Game server port for lobby 1 is *");
    }

    @Test
    @DirtiesContext
    void testThatStartingTheSameGameTwiceThrowsException() throws IOException {
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
    void testThatLeavingGameWorks(CapturedOutput output) throws IOException {
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