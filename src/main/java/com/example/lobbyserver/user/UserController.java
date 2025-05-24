package com.example.lobbyserver.user;

import com.example.lobbyserver.mail.MailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "Endpoints for user registration and retrieval")
public class UserController {

    private final UserService userService;
    private final MailVerificationService mailVerificationService;

    public UserController(UserService userService, MailVerificationService mailVerificationService) {
        this.userService = userService;
        this.mailVerificationService = mailVerificationService;
    }

    @Operation(
            summary = "Get current user info",
            description = "Returns information about the currently authenticated user.",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDao.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized (invalid or missing credentials)",
                    content = @Content(schema = @Schema())
            )
    })
    @GetMapping
    public ResponseEntity<UserDao> getUser(Authentication authentication) {
        var currentUser = authentication.getName();
        return ResponseEntity.of(userService.getUser(currentUser));
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and sends a verification email. Returns 201 if successful."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDao.class))
            )
            @Valid @RequestBody UserDao user, UriComponentsBuilder ucb) {
        if (userService.usernameExists(user.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (userService.emailExists(user.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        userService.createUser(user.username(), user.password(), user.email(), "USER");
        mailVerificationService.sendVerificationMail(user.email());

        var location = ucb.path("/user").buildAndExpand(user.username()).toUri();
        return ResponseEntity.created(location).build();
    }
}
