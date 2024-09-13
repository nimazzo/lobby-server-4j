package com.example.lobbyserver.game;

import com.example.lobbyserver.lobby.db.LobbyRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class GameInstanceService {

    private static final Logger log = LoggerFactory.getLogger(GameInstanceService.class);

    private final LobbyRepository lobbyRepository;
    private final Environment env;
    private final Executor taskScheduler;

    private final Map<Long, ProcessHandle> gameInstances = new ConcurrentHashMap<>();

    public GameInstanceService(LobbyRepository lobbyRepository, Environment env, Executor taskScheduler) {
        this.lobbyRepository = lobbyRepository;
        this.env = env;
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    void shutdown() {
        gameInstances.values().forEach(ProcessHandle::destroy);
    }

    @Async
    public void startNewGameInstance(long lobbyId, int lobbySize) {
        try (var serverSocket = new ServerSocket(0)) {

            var localLobbyPort = serverSocket.getLocalPort();
            taskScheduler.execute(() -> launchGameServer(localLobbyPort, lobbyId));

            var gameServer = serverSocket.accept();
            log.debug("Game server connected for lobby {}", lobbyId);

            var out = new DataOutputStream(gameServer.getOutputStream());
            var in = new DataInputStream(gameServer.getInputStream());
            out.writeShort(lobbySize);

            var port = in.readShort() & 0xFFFF;
            log.debug("Game server port for lobby {} is {}", lobbyId, port);

            lobbyRepository.updateGameServerHostAndGameServerPortById(
                    env.getProperty("game.server.instance.host"), port, lobbyId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void launchGameServer(int localLobbyPort, long lobbyId) {
        try {
            var serverResource = new FileSystemResource(Objects.requireNonNull(env.getProperty("game.server.executable-name")));
            if (!serverResource.exists()) {
                throw new IllegalStateException("Game server executable not found at " + serverResource.getPath());
            }
            var process = new ProcessBuilder(serverResource.getFile().getPath(), Integer.toString(localLobbyPort))
                    .start();
            log.debug("Game server for lobby {} started", lobbyId);
            gameInstances.put(lobbyId, process.toHandle());
            var exit = process.waitFor();
            log.debug("Game server for lobby {} exited with code {}", lobbyId, exit);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
