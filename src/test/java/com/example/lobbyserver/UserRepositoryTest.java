package com.example.lobbyserver;

import com.example.lobbyserver.user.Authority;
import com.example.lobbyserver.user.UserConfiguration;
import com.example.lobbyserver.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, UserConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    UserDetailsManager userDetailsManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        userDetailsManager.createUser(
                User.builder()
                        .username("admin")
                        .password(encoder.encode("admin"))
                        .roles("USER", "ADMIN")
                        .build());
        userRepository.updateEmailByUsername("admin@admin.com", "admin");
        userDetailsManager.createUser(
                User.builder()
                        .username("user")
                        .password(encoder.encode("user"))
                        .roles("USER")
                        .build());
        userRepository.updateEmailByUsername("user@user.com", "user");
    }

    @Test
    void testThatContextLoads() {
    }

    @Test
    void testThatDatabaseWorks() {
        var admin = userRepository.findByUsername("admin");
        assertThat(admin).isNotNull();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getEmail()).isEqualTo("admin@admin.com");
        assertThat(admin.getAuthorities()).hasSize(2);
        assertThat(admin.getAuthorities().stream().map(Authority::getAuthority)).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        assertThat(encoder.matches("admin", admin.getPassword())).isTrue();

        var user = userRepository.findByUsername("user");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("user");
        assertThat(user.getEmail()).isEqualTo("user@user.com");
        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.getAuthorities().stream().map(Authority::getAuthority)).containsExactly("ROLE_USER");
        assertThat(encoder.matches("user", user.getPassword())).isTrue();

        var numberOfRows = userRepository.count();
        assertThat(numberOfRows).isEqualTo(2);
    }

    @TestConfiguration
    static class UserRepositoryTestConfiguration {
        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
    }
}