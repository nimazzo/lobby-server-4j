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
}