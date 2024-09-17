package com.example.lobbyserver.game.db;

import com.example.lobbyserver.TestcontainersConfiguration;
import com.example.lobbyserver.user.UserService;
import com.example.lobbyserver.user.db.UserConfiguration;
import com.example.lobbyserver.user.db.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, UserService.class, UserConfiguration.class})
@DirtiesContext
@ActiveProfiles("test")
class GameResultRepositoryTest {

    @Autowired
    GameResultRepository gameResultRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        var dateTime = LocalDateTime.of(2021, 1, 1, 0, 0);

        userService.createUser("user1", "password", "user1@email.com");
        userService.createUser("user2", "password", "user2@email.com");
        userService.createUser("user3", "password", "user3@email.com");

        var user1 = userRepository.findById("user1").orElseThrow();
        var user2 = userRepository.findById("user2").orElseThrow();
        var user3 = userRepository.findById("user3").orElseThrow();

        var testData = List.of(
                new GameResult(null, user1, 1000L, 10, 6, 10000L, dateTime),
                new GameResult(null, user1, 1000L, 10, 6, 10000L, dateTime),
                new GameResult(null, user1, 1000L, 10, 6, 10000L, dateTime),
                new GameResult(null, user2, 1000L, 10, 6, 10000L, dateTime),
                new GameResult(null, user3, 1000L, 10, 6, 10000L, dateTime)
        );
        gameResultRepository.saveAll(testData);
    }

    @Test
    void testThatFindByUsernameReturnsCorrectResults() {
        var result = gameResultRepository.findByUser_Username("user1", Pageable.unpaged());
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().size()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(
                gameResult -> gameResult.getUser().getUsername().equals("user1")
        );
    }

    @TestConfiguration
    static class UserRepositoryTestConfiguration {
        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
    }
}