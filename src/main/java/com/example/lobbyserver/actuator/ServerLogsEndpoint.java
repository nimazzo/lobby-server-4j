package com.example.lobbyserver.actuator;

import com.example.lobbyserver.game.ServerLogsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

@Component
@Endpoint(id = "serverLogs")
@SuppressWarnings("unused")
public class ServerLogsEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ServerLogsEndpoint.class);

    private final ServerLogsService serverLogsService;

    public ServerLogsEndpoint(ServerLogsService serverLogsService) {
        this.serverLogsService = serverLogsService;
    }

    @ReadOperation
    public Map<String, Object> getListOfAvailableLogs() throws IOException {
        log.info("Attempting to get list of available logs");

        var logs = serverLogsService
                .getAllLogFiles()
                .stream()
                .map(id -> {
                    var uri = ServletUriComponentsBuilder
                            .fromCurrentRequestUri()
                            .path("/{id}")
                            .buildAndExpand(id);
                    return new ServerLogInfo(id, uri.toUri());
                })
                .toList();

        return Map.of("count", logs.size(), "logs", logs);
    }

    @ReadOperation
    public ResponseEntity<String> getServerLogForLobby(@Selector String lobbyId) {
        log.info("Attempting to get logs for lobby {}", lobbyId);
        try {
            var logFile = serverLogsService.getLogFileForLobby(Long.parseLong(lobbyId));
            var result = new FileSystemResource(logFile).getContentAsString(Charset.defaultCharset());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error retrieving log file for lobby {}", lobbyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
