package com.example.lobbyserver.game;

import com.example.lobbyserver.lobby.db.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.NonNull;
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
import java.util.concurrent.Phaser;

@Service
public class GameInstanceService implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GameInstanceService.class);

    private final LobbyRepository lobbyRepository;
    private final Environment env;
    private final Executor taskScheduler;

    private final Map<Long, ProcessHandle> gameInstances = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    private final Phaser stopGate = new Phaser(1);

    public GameInstanceService(LobbyRepository lobbyRepository, Environment env, Executor taskScheduler) {
        this.lobbyRepository = lobbyRepository;
        this.env = env;
        this.taskScheduler = taskScheduler;
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
        stopGate.register();
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
        } finally {
            stopGate.arriveAndDeregister();
        }
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop(@NonNull Runnable callback) {
        stop();
        stopGate.arriveAndAwaitAdvance();
        callback.run();
    }

    @Override
    public void stop() {
        shutdown();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    void shutdown() {
        gameInstances.values().forEach(ProcessHandle::destroy);
    }
}
