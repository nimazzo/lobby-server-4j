package com.example.lobbyserver.user.db;

import com.example.lobbyserver.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
    ApplicationRunner initDummyUsers(UserService userService) {
        log.info("Populating 'users' database with 'user' and 'admin' users");
        return _ -> {
            userService.createActivatedUser("user", "user", "user@user.com", "USER");
            userService.createActivatedUser("admin", "admin", "admin@admin.com", "USER", "ADMIN");
        };
    }
}
