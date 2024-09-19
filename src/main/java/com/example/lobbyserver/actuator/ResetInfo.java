package com.example.lobbyserver.actuator;

public record ResetInfo(
        long deletedGameInstances,
        long deletedLobbies
) {
}
