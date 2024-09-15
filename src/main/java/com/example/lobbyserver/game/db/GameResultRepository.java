package com.example.lobbyserver.game.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    Page<GameResult> findByUser_Username(String username, Pageable pageable);
}
