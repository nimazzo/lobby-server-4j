package com.example.lobbyserver.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserDetailsManager userDetailsManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserDetailsManager userDetailsManager, UserRepository userRepository, PasswordEncoder encoder) {
        this.userDetailsManager = userDetailsManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Transactional
    public void createUser(String username, String password, String email, String... roles) {
        var user = User.builder()
                .username(username)
                .password(encoder.encode(password))
                .roles(roles)
                .build();
        userDetailsManager.createUser(user);
        userRepository.updateEmailByUsername(email, username);
    }

}
