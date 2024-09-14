package com.example.lobbyserver.lobby;

import jakarta.validation.constraints.NotNull;

public record GameResultRequest(
        @NotNull
        Long score,
        @NotNull
        Integer level,
        @NotNull
        Long time
) {
}