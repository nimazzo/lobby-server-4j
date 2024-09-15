package com.example.lobbyserver.user;

import com.example.lobbyserver.user.db.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        createUser(username, password, email, false, roles);
    }

    @Transactional
    public void createActivatedUser(String username, String password, String email, String... roles) {
        createUser(username, password, email, true, roles);
    }

    private void createUser(String username, String password, String email, boolean activated, String... roles) {
        var user = User.builder()
                .username(username)
                .password(encoder.encode(password))
                .disabled(!activated)
                .roles(roles)
                .build();
        userDetailsManager.createUser(user);
        userRepository.updateEmailByUsername(email, username);
    }

    public Optional<UserDao> getUser(String username) {
        return userRepository.findById(username)
                .map(u -> new UserDao(u.getUsername(), u.getPassword(), u.getEmail()));
    }

    public boolean usernameExists(String username) {
        return userRepository.existsById(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void activateUser(String email) {
        userRepository.updateEnabledByEmail(true, email);
    }
}
