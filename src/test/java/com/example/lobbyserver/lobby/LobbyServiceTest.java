package com.example.lobbyserver.lobby;

import com.example.lobbyserver.game.GameInstanceService;
import com.example.lobbyserver.game.db.GameResult;
import com.example.lobbyserver.game.db.GameResultRepository;
import com.example.lobbyserver.lobby.db.Lobby;
import com.example.lobbyserver.lobby.db.LobbyRepository;
import com.example.lobbyserver.user.db.User;
import com.example.lobbyserver.user.db.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = LobbyService.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@ActiveProfiles("test")
class LobbyServiceTest {
    private static final User DUMMY_USER = new User("user", "password", "email", true, Set.of());
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9999;
    private static final long LOBBY_ID = 99;

    @Autowired
    LobbyService lobbyService;

    @MockitoBean
    LobbyRepository lobbyRepository;

    @MockitoBean
    GameInstanceService gameInstanceService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    GameResultRepository gameResultRepository;

    @Test
    void testThatCreateNewLobbyWorks() {
        var username = "user";
        var lobbyRequest = new LobbyCreationRequest("Lobby 1", 4);

        var entityCapture = ArgumentCaptor.forClass(Lobby.class);
        given(lobbyRepository.save(entityCapture.capture())).willAnswer(_ -> {
            var input = entityCapture.getValue();
            input.setId(1L);
            return input;
        });

        given(userRepository.findById(username))
                .willReturn(Optional.of(DUMMY_USER));

        var createdLobby = lobbyService.createNewLobby(lobbyRequest, username);

        assertThat(createdLobby.id()).isEqualTo(1L);
        assertThat(createdLobby.name()).isEqualTo(lobbyRequest.name());
        assertThat(createdLobby.numberOfPlayers()).isEqualTo(0);
        assertThat(createdLobby.maxPlayers()).isEqualTo(lobbyRequest.maxPlayers());
        assertThat(createdLobby.owner()).isEqualTo(username);

        verify(gameInstanceService).startNewGameInstance(1L, lobbyRequest.maxPlayers());
    }

    @Test
    void testThatTryJoinLobbyReturnsConnectionResults() {
        var username = "user";

        given(userRepository.findById(username)).willReturn(Optional.of(DUMMY_USER));
        given(lobbyRepository.findById(LOBBY_ID)).willReturn(Optional.of(
                new Lobby(LOBBY_ID,
                        "Lobby 1",
                        0,
                        4,
                        DUMMY_USER,
                        Set.of(),
                        HOSTNAME,
                        PORT,
                        false,
                        null)
        ));

        var result = lobbyService.tryJoinLobby(LOBBY_ID, username);
        assertThat(result).isPresent();

        var connectionDetails = result.get();
        assertThat(connectionDetails.hostname()).isEqualTo(HOSTNAME);
        assertThat(connectionDetails.port()).isEqualTo(PORT);
    }

    @Test
    void testThatTryingToJoinALobbyTwiceThrowsException() {
        var username = "user";

        given(userRepository.findById(username)).willReturn(Optional.of(DUMMY_USER));
        given(lobbyRepository.findById(LOBBY_ID)).willReturn(Optional.of(
                new Lobby(LOBBY_ID,
                        "Lobby 1",
                        0,
                        4,
                        DUMMY_USER,
                        Set.of(DUMMY_USER),
                        HOSTNAME,
                        PORT,
                        false,
                        null)
        ));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> lobbyService.tryJoinLobby(LOBBY_ID, username))
                .withMessage("User is already in the lobby");
    }

    @Test
    void testThatTryJoinLobbyReturnsEmptyForFullLobby() {
        var username = "user";

        given(userRepository.findById(username)).willReturn(Optional.of(DUMMY_USER));
        given(lobbyRepository.findById(LOBBY_ID)).willReturn(Optional.of(
                new Lobby(LOBBY_ID,
                        "Lobby 1",
                        4,
                        4,
                        DUMMY_USER,
                        Set.of(DUMMY_USER),
                        HOSTNAME,
                        PORT,
                        true,
                        null)
        ));

        var result = lobbyService.tryJoinLobby(LOBBY_ID, username);
        assertThat(result).isEmpty();
    }

    @Test
    void testThatTryJoinLobbyReturnsEmptyForNonExistingLobby() {
        var result = lobbyService.tryJoinLobby(99L, "user");
        assertThat(result).isEmpty();
    }

    @Test
    void testThatFullLobbyGetsSetToGameStarted() {
        var username = "user2";

        var capture = ArgumentCaptor.forClass(Lobby.class);
        given(userRepository.findById(username)).willReturn(Optional.of(
                new User(username, "password", "email@email.com", true, Set.of())
        ));
        given(lobbyRepository.findById(LOBBY_ID)).willReturn(Optional.of(
                new Lobby(LOBBY_ID,
                        "Lobby 1",
                        1,
                        2,
                        DUMMY_USER,
                        Set.of(DUMMY_USER),
                        HOSTNAME,
                        PORT,
                        false,
                        null)
        ));
        given(lobbyRepository.save(capture.capture())).willReturn(null);

        var result = lobbyService.tryJoinLobby(LOBBY_ID, username);

        assertThat(result).isPresent();
        assertThat(capture.getValue().getGameStarted()).isTrue();
    }

    @Test
    void testThatCannotJoinLobbyThatHasAlreadyStarted() {
        var username = "user2";

        given(userRepository.findById(username)).willReturn(Optional.of(
                new User(username, "password", "email@email.com", true, Set.of())
        ));
        given(lobbyRepository.findById(LOBBY_ID)).willReturn(Optional.of(
                new Lobby(LOBBY_ID,
                        "Lobby 1",
                        1,
                        2,
                        DUMMY_USER,
                        Set.of(DUMMY_USER),
                        HOSTNAME,
                        PORT,
                        true,
                        null)
        ));

        var result = lobbyService.tryJoinLobby(LOBBY_ID, username);
        assertThat(result).isEmpty();
    }

    @Test
    void testThatSaveGameResultValidatesInput() {
        var invalidGameResult = new GameResultRequest(null, null, null, null);

        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> lobbyService.saveGameResult("user", invalidGameResult));
    }

    @Test
    void testThatSavingValidGameResultWorks() {
        var validGameResult = new GameResultRequest(9999L, 11, 123, 60000L);
        var capture = ArgumentCaptor.forClass(GameResult.class);
        var dummyTime = LocalDateTime.of(2021, 1, 1, 0, 0);

        given(gameResultRepository.save(capture.capture())).willReturn(null);
        given(userRepository.getReferenceById("user")).willReturn(DUMMY_USER);

        try (var staticLocalDateTime = mockStatic(LocalDateTime.class)) {
            staticLocalDateTime.when(LocalDateTime::now).thenReturn(dummyTime);
            lobbyService.saveGameResult("user", validGameResult);
        }

        var savedResult = capture.getValue();
        assertThat(savedResult.getId()).isNull();
        assertThat(savedResult.getUser().getUsername()).isEqualTo("user");
        assertThat(savedResult.getScore()).isEqualTo(validGameResult.score());
        assertThat(savedResult.getLevel()).isEqualTo(validGameResult.level());
        assertThat(savedResult.getTime()).isEqualTo(validGameResult.time());
        assertThat(savedResult.getDateTime()).isEqualTo(dummyTime);
    }

    @Test
    void testThatRemovingPlayerFromLobbyWorks() {
        var otherUser = new User();
        otherUser.setUsername("otherUser");

        var lobby = new Lobby(LOBBY_ID,
                "Lobby 1",
                2,
                2,
                DUMMY_USER,
                Set.of(DUMMY_USER, otherUser),
                HOSTNAME,
                PORT,
                true,
                null);

        var capture = ArgumentCaptor.forClass(Lobby.class);

        given(lobbyRepository.findById(LOBBY_ID)).willReturn(Optional.of(lobby));
        given(gameInstanceService.playerLeftGame(LOBBY_ID)).willReturn(1);
        given(userRepository.findById("user")).willReturn(Optional.of(DUMMY_USER));
        given(lobbyRepository.save(capture.capture())).willReturn(null);

        lobbyService.removePlayerFromLobby(LOBBY_ID, "user");

        var updatedLobby = capture.getValue();
        assertThat(updatedLobby.getNumberOfPlayers()).isEqualTo(2);
        assertThat(updatedLobby.getPlayers()).noneMatch(user -> user.getUsername().equals("user"));
    }

    @Test
    void testThatRemovePlayerFromInvalidLobbyThrowsException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> lobbyService.removePlayerFromLobby(LOBBY_ID, "user"))
                .withMessage("Lobby not found");
    }

    @Test
    void testThatRemovePlayerFromOtherLobbyThrowsException() {
        given(lobbyRepository.findById(LOBBY_ID)).willReturn(Optional.of(
                new Lobby(LOBBY_ID,
                        "Lobby 1",
                        0,
                        2,
                        DUMMY_USER,
                        Set.of(),
                        HOSTNAME,
                        PORT,
                        false,
                        null)
        ));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> lobbyService.removePlayerFromLobby(LOBBY_ID, "user"))
                .withMessage("User is not in the lobby");
    }

    @Test
    void testThatEmptyLobbyGetsDeletedFromDatabase() {
        var lobby = new Lobby(LOBBY_ID,
                "Lobby 1",
                1,
                2,
                DUMMY_USER,
                Set.of(DUMMY_USER),
                HOSTNAME,
                PORT,
                true,
                null);

        given(lobbyRepository.findById(LOBBY_ID)).willReturn(Optional.of(lobby));
        given(gameInstanceService.playerLeftGame(LOBBY_ID)).willReturn(0);

        lobbyService.removePlayerFromLobby(LOBBY_ID, "user");

        verify(lobbyRepository).delete(lobby);
    }
}