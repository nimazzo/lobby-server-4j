package com.example.lobbyserver.user;

import com.example.lobbyserver.TestcontainersConfiguration;
import com.example.lobbyserver.user.db.Authority;
import com.example.lobbyserver.user.db.AuthorityRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@Import({TestcontainersConfiguration.class, UserConfiguration.class, UserService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthorityRepository authorityRepository;
    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        userService.createUser("admin", "admin", "admin@admin.com", "USER", "ADMIN");
        userService.createUser("user", "user", "user@user.com", "USER");
    }

    @Test
    void testThatContextLoads() {
    }

    @Test
    void testThatDatabaseWorks() {
        var admin = userRepository.findById("admin").orElseThrow();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getEmail()).isEqualTo("admin@admin.com");
        assertThat(admin.getAuthorities()).hasSize(2);
        assertThat(admin.getAuthorities().stream().map(Authority::getAuthority)).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        assertThat(encoder.matches("admin", admin.getPassword())).isTrue();

        var user = userRepository.findById("user").orElseThrow();
        assertThat(user.getUsername()).isEqualTo("user");
        assertThat(user.getEmail()).isEqualTo("user@user.com");
        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.getAuthorities().stream().map(Authority::getAuthority)).containsExactly("ROLE_USER");
        assertThat(encoder.matches("user", user.getPassword())).isTrue();

        var numberOfRows = userRepository.count();
        assertThat(numberOfRows).isEqualTo(2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testThatTransactionsWork() {
        // Transaction is disabled for this test method because we expect the createUser method being tested
        // to already cause a transaction rollback due to the DataIntegrityViolationException
        // which would otherwise causes a JpaSystemException when trying to read from the database in the
        // following lines
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> userService.createUser("admin2",
                        "admin2",
                        "admin@admin.com",
                        "USER", "ADMIN")
                );

        var invalidUser = userRepository.findById("admin2");
        assertThat(invalidUser).isEmpty();

        // cleanup
        userRepository.deleteAll();
    }

    @Test
    void testThatDeletingUsersCascadesToAuthorities() {
        var userCount = userRepository.count();
        assertThat(userCount).isEqualTo(2);
        var authorityCount = authorityRepository.count();
        assertThat(authorityCount).isEqualTo(3);

        userRepository.deleteAll();
        var newAuthorityCount = authorityRepository.count();
        assertThat(newAuthorityCount).isEqualTo(0);
    }

    @Test
    void testThatEmptyAuthoritiesWork() {
        userService.createActivatedUser("user2", "user2", "user2@user2.com");

        var userFromDb = userRepository.findById("user2").orElseThrow();
        assertThat(userFromDb.getUsername()).isEqualTo("user2");
        assertThat(userFromDb.getEmail()).isEqualTo("user2@user2.com");
        assertThat(encoder.matches("user2", userFromDb.getPassword())).isTrue();
        assertThat(userFromDb.getAuthorities()).isEmpty();
    }

    @TestConfiguration
    static class UserRepositoryTestConfiguration {
        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
    }
}