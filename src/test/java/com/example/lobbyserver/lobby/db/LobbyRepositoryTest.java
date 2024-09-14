package com.example.lobbyserver.lobby.db;

import com.example.lobbyserver.TestcontainersConfiguration;
import com.example.lobbyserver.user.db.User;
import com.example.lobbyserver.user.db.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@DirtiesContext
@ActiveProfiles("test")
class LobbyRepositoryTest {

    @Autowired
    LobbyRepository lobbyRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        var owner = new User("admin", "admin", "admin@admin.com", true, Set.of());
        userRepository.save(owner);

        var lobby = new Lobby();
        lobby.setName("Lobby 1");
        lobby.setNumberOfPlayers(0);
        lobby.setMaxPlayers(4);
        lobby.setOwner(owner);
        lobbyRepository.save(lobby);
    }

    @Test
    void testThatContextLoads() {
    }

    @Test
    void testThatDatabaseWorks() {
        var lobbies = lobbyRepository.findAll();
        assertThat(lobbies).hasSize(1);

        var lobby = lobbies.getFirst();
        assertThat(lobby.getName()).isEqualTo("Lobby 1");
        assertThat(lobby.getNumberOfPlayers()).isEqualTo(0);
        assertThat(lobby.getMaxPlayers()).isEqualTo(4);
        assertThat(lobby.getOwner().getUsername()).isEqualTo("admin");
        assertThat(lobby.getPlayers()).isEmpty();
    }

    @Test
    @SuppressWarnings("SqlResolve")
    void testThatPlayerLobbyMappingTableGetClearedWhenPlayerLeavesLobby() {
        var lobby = lobbyRepository.findByName("Lobby 1").orElseThrow();
        var player = userRepository.findByUsername("admin");
        assertThat(player).isNotNull();
        lobby.addPlayer(player);
        lobbyRepository.save(lobby);

        var results = entityManager.createNativeQuery("SELECT COUNT(*) FROM lobby_players").getResultList();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isEqualTo(1L);

        lobby = lobbyRepository.findByName("Lobby 1").orElseThrow();
        lobby.removePlayer(player);
        lobbyRepository.save(lobby);

        results = entityManager.createNativeQuery("SELECT COUNT(*) FROM lobby_players").getResultList();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isEqualTo(0L);
    }
}