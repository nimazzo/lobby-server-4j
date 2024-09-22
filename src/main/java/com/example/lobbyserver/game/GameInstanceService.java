package com.example.lobbyserver.game;

import com.example.lobbyserver.lobby.db.LobbyRepository;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
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
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GameInstanceService implements SmartLifecycle {

    @Value("${game.server.executable-name}")
    private String gameServerExecutable;

    @Value("${game.server.instance.host}")
    private String gameServerInstanceHost;

    private static final Logger log = LoggerFactory.getLogger(GameInstanceService.class);

    private final LobbyRepository lobbyRepository;
    private final ServerLogsService serverLogsService;
    private final Executor taskScheduler;

    private final Map<Long, GameInstanceInfo> gameInstances = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    private final Phaser stopGate = new Phaser(1);

    public GameInstanceService(LobbyRepository lobbyRepository, ServerLogsService serverLogsService, Executor taskScheduler) {
        this.lobbyRepository = lobbyRepository;
        this.serverLogsService = serverLogsService;
        this.taskScheduler = taskScheduler;
    }

    @Async
    public void startNewGameInstance(long lobbyId, int lobbySize) {
        if (gameInstances.putIfAbsent(lobbyId, new GameInstanceInfo(lobbySize)) != null) {
            throw new IllegalStateException("Game instance for lobby " + lobbyId + " already exists");
        }

        try (var serverSocket = new ServerSocket(0)) {

            var localLobbyPort = serverSocket.getLocalPort();
            taskScheduler.execute(() -> launchGameServer(localLobbyPort, lobbyId));

            serverSocket.setSoTimeout(2000);
            var gameServer = serverSocket.accept();
            log.debug("Game server connected for lobby {}", lobbyId);

            var out = new DataOutputStream(gameServer.getOutputStream());
            var in = new DataInputStream(gameServer.getInputStream());
            out.writeShort(lobbySize);

            var port = in.readShort() & 0xFFFF;
            log.debug("Game server port for lobby {} is {}", lobbyId, port);

            lobbyRepository.updateGameServerHostAndGameServerPortById(
                    gameServerInstanceHost, port, lobbyId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int playerLeftGame(Long lobbyId) {
        var gameInstanceInfo = gameInstances.get(lobbyId);
        if (gameInstanceInfo == null) {
            throw new IllegalStateException("Game instance for lobby " + lobbyId + " does not exist");
        }

        var remaining = gameInstanceInfo.connectedPlayers.decrementAndGet();

        if (remaining == 0) {
            log.debug("All players left the game {}, shutting down server", lobbyId);
            gameInstanceInfo.getProcess().destroy();
            gameInstances.remove(lobbyId);
        }

        return remaining;
    }

    private void launchGameServer(int localLobbyPort, long lobbyId) {
        stopGate.register();
        try {
            var serverResource = new FileSystemResource(Objects.requireNonNull(gameServerExecutable));
            if (!serverResource.exists()) {
                throw new IllegalStateException("Game server executable not found at " + serverResource.getPath());
            }

            var tempFile = serverLogsService.createLogFileForLobby(lobbyId);
            var process = new ProcessBuilder(serverResource.getFile().getPath(), Integer.toString(localLobbyPort))
                    .redirectErrorStream(true)
                    .redirectOutput(tempFile)
                    .start();
            log.debug("Game server for lobby {} started", lobbyId);
            gameInstances.get(lobbyId).setProcess(process.toHandle());

            var exit = process.waitFor();
            gameInstances.remove(lobbyId);

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
        gameInstances.values()
                .stream()
                .map(GameInstanceInfo::getProcess)
                .filter(Objects::nonNull)
                .forEach(ProcessHandle::destroy);
    }

    public int terminateAll() {
        shutdown();
        int size = gameInstances.size();
        gameInstances.clear();
        return size;
    }

    @SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed"})
    @EqualsAndHashCode
    static class GameInstanceInfo {
        private final AtomicInteger connectedPlayers;
        private ProcessHandle process;

        GameInstanceInfo(int connectedPlayers) {
            this.connectedPlayers = new AtomicInteger(connectedPlayers);
        }

        public ProcessHandle getProcess() {
            return process;
        }

        public void setProcess(ProcessHandle process) {
            this.process = process;
        }
    }
}
