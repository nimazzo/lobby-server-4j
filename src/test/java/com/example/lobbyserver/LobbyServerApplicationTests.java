package com.example.lobbyserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
class LobbyServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
