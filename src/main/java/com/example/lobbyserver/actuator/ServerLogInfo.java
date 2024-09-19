package com.example.lobbyserver.actuator;

import java.net.URI;

public record ServerLogInfo(
        long lobbyId,
        URI logFileUri
) {
}