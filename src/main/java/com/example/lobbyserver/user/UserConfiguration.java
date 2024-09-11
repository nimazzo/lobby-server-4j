package com.example.lobbyserver.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import javax.sql.DataSource;

@Configuration
public class UserConfiguration {
    private static final Logger log = LoggerFactory.getLogger(UserConfiguration.class);

    @Bean
    UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    @Profile("dev & !test")
    ApplicationRunner initDummyUsers(UserDetailsManager userDetailsManager, PasswordEncoder encoder) {
        log.info("Populating 'users' database with 'user' and 'admin' users");
        return _ -> {
            UserDetails user = User.builder()
                    .username("user")
                    .password(encoder.encode("user"))
                    .roles("USER")
                    .build();
            UserDetails admin = User.builder()
                    .username("admin")
                    .password(encoder.encode("admin"))
                    .roles("USER", "ADMIN")
                    .build();
            userDetailsManager.createUser(user);
            userDetailsManager.createUser(admin);
        };
    }
}
