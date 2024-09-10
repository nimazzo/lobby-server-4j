package com.example.lobbyserver.user;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class UserConfiguration {
    @Bean
    ApplicationRunner initUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return _ -> {
            var admin = new User(null, "admin", encoder.encode("admin"), true);
            var user = new User(null, "user", encoder.encode("user"), true);
            userRepository.saveAllAndFlush(List.of(admin, user));
        };
    }
}
