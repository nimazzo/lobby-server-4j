package com.example.lobbyserver.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

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
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/register").permitAll()
                        .requestMatchers("/verify").permitAll()
                        .requestMatchers("/game-results").permitAll()
                        .requestMatchers("/csrf").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        // require authentication
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/lobby/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lobby/create").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lobby/join/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/lobby/leave/*").authenticated()
                        // denied for all
                        .anyRequest().denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())
                        .failureHandler(new CustomAuthenticationFailureHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout")
                        .permitAll())
                .csrf(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    static class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
            String errorMessage = "Unknown error occurred.";
            if (exception instanceof BadCredentialsException) {
                errorMessage = "Invalid username or password.";
            } else if (exception instanceof DisabledException) {
                errorMessage = "Your account is disabled. Please verify your email.";
            }
            request.getSession().setAttribute("error", errorMessage);
            response.sendRedirect(request.getContextPath() + "/login?error");
        }
    }
}
