package com.example.lobbyserver.user;

import com.example.lobbyserver.mail.MailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final MailVerificationService mailVerificationService;

    public UserController(UserService userService, MailVerificationService mailVerificationService) {
        this.userService = userService;
        this.mailVerificationService = mailVerificationService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDao> getUser(@PathVariable String username, Authentication authentication) {
        var currentUser = authentication.getName();
        if (!currentUser.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ofNullable(userService.getUser(username));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody UserDao user, UriComponentsBuilder ucb) {
        if (userService.usernameExists(user.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (userService.emailExists(user.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        userService.createUser(user.username(), user.password(), user.email(), "USER");
        mailVerificationService.sendVerificationMail(user.email());
        
        var location = ucb.path("/user/{username}").buildAndExpand(user.username()).toUri();
        return ResponseEntity.created(location).build();
    }
}
