package com.example.lobbyserver.user;

import com.example.lobbyserver.user.db.UserRepository;
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

    public UserDao getUser(String username) {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        return new UserDao(user.getUsername(), user.getPassword(), user.getEmail());
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
