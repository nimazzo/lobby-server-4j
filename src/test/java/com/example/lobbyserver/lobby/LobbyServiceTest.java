package com.example.lobbyserver.lobby;

import com.example.lobbyserver.game.GameInstanceService;
import com.example.lobbyserver.game.db.GameResultRepository;
import com.example.lobbyserver.lobby.db.Lobby;
import com.example.lobbyserver.lobby.db.LobbyRepository;
import com.example.lobbyserver.user.db.User;
import com.example.lobbyserver.user.db.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class LobbyServiceTest {
    private static final User DUMMY_USER = new User("user", "password", "email", true, Set.of());
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9999;
    private static final long LOBBY_ID = 99;

    LobbyService lobbyService;

    @Mock
    LobbyRepository lobbyRepository;

    @Mock
    GameInstanceService gameInstanceService;

    @Mock
    UserRepository userRepository;

    @Mock
    GameResultRepository gameResultRepository;

    @BeforeEach
    void setUp() {
        lobbyService = new LobbyService(lobbyRepository, gameInstanceService, userRepository, gameResultRepository);
    }

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
        assertThat(createdLobby.gameServerHost()).isNull();
        assertThat(createdLobby.gameServerPort()).isNull();

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
}