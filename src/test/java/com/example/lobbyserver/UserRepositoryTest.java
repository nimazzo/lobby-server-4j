package com.example.lobbyserver;

import com.example.lobbyserver.user.User;
import com.example.lobbyserver.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private final PasswordEncoder encoder;

    public UserRepositoryTest() {
        this.encoder = new BCryptPasswordEncoder();
    }

    @BeforeEach
    void setUp() {
        userRepository.save(new User(null, "admin", encoder.encode("admin"), true));
        userRepository.save(new User(null, "user", encoder.encode("user"), true));
    }

    @Test
    void testThatContextLoads() {
    }

    @Test
    void testThatDatabaseWorks() {
        var admin = userRepository.findUserByUsername("admin");
        assertThat(admin).isNotNull();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(encoder.matches("admin", admin.getPassword())).isTrue();

        var user = userRepository.findUserByUsername("user");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("user");
        assertThat(encoder.matches("user", user.getPassword())).isTrue();

        var numberOfRows = userRepository.count();
        assertThat(numberOfRows).isEqualTo(2);
    }
}