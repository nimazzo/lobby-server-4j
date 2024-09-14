package com.example.lobbyserver.lobby.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LobbyRepository extends JpaRepository<Lobby, Long> {

    @Query("""
            select l from Lobby l
            where l.numberOfPlayers < l.maxPlayers
                and l.gameServerHost is not null
                and l.gameServerPort is not null
            """)
    Page<Lobby> findOpenLobbies(Pageable pageable);

    @Transactional
    @Modifying
    @Query("update Lobby l set l.gameServerHost = ?1, l.gameServerPort = ?2 where l.id = ?3")
    void updateGameServerHostAndGameServerPortById(String gameServerHost, Integer gameServerPort, Long id);

    Optional<Lobby> findByName(String name);
}
