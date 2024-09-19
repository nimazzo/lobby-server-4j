package com.example.lobbyserver.game;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ServerLogsService {

    private static final Logger log = LoggerFactory.getLogger(ServerLogsService.class);
    private Path tempDirectory;

    @PostConstruct
    void createTempDirectory() throws IOException {
        tempDirectory = Files.createTempDirectory("game-server-logs");
        log.info("Temp directory for game server logs created at: {}", tempDirectory);
    }

    @PreDestroy
    void cleanupTempDirectory() throws IOException {
        Files.walkFileTree(tempDirectory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                log.info("Temp file cleanup - deleting file: {}", file);
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e == null) {
                    log.warn("Temp file cleanup - deleting directory: {}", dir);
                    Files.delete(dir);
                }
                return super.postVisitDirectory(dir, e);
            }
        });
    }

    public File createLogFileForLobby(long lobbyId) throws IOException {
        var timeStamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(':', '-');
        var prefix = String.format("game-server-%d-%s", lobbyId, timeStamp);
        return File.createTempFile(prefix, ".log", tempDirectory.toFile());
    }

    public File getLogFileForLobby(long lobbyId) throws IOException {
        try (var files = Files.walk(tempDirectory, 1)) {
            return files.map(Path::toFile)
                    .filter(File::isFile)
                    .filter(file -> {
                        log.warn("comparing file: {}, looking for id {}", file.getName(), lobbyId);
                        return file.getName().startsWith(
                                "game-server-" + lobbyId + "-");
                    })
                    .findFirst().orElseThrow();
        }
    }

    public List<Long> getAllLogFiles() throws IOException {
        try (var files = Files.walk(tempDirectory, 1)) {
            return files.map(Path::toFile)
                    .filter(File::isFile)
                    .map(File::getName)
                    .filter(name -> name.startsWith("game-server-"))
                    .map(name -> {
                        var parts = name.split("-");
                        return Long.parseLong(parts[2]);
                    }).toList();
        }
    }
}
