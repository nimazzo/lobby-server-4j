package com.example.lobbyserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, Environment env) throws Exception {
        boolean isDevEnvironment = env.matchesProfiles("dev");
        if (isDevEnvironment) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/**").authenticated()
                    .requestMatchers("/testing/**").permitAll()
            );
        }

        http
                .authorizeHttpRequests(auth -> auth
                        // permitted for all
                        .requestMatchers(HttpMethod.POST, "/user/register").permitAll()
                        .requestMatchers("/verify").permitAll()
                        .requestMatchers("/game-results").permitAll()
                        .requestMatchers("/csrf").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        // require authentication
                        .requestMatchers("/").authenticated()
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/lobby/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lobby/create").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lobby/join/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lobby/leave/*").authenticated()
                        // denied for all
                        .anyRequest().denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .csrf(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
