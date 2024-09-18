package com.example.lobbyserver.mail;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("verify")
public class MailVerificationController {

    private final MailVerificationService mailVerificationService;

    public MailVerificationController(MailVerificationService mailVerificationService) {
        this.mailVerificationService = mailVerificationService;
    }

    @GetMapping
    public RedirectView verifyMail(@RequestParam("token") String token) {
        if (!mailVerificationService.verifyMail(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        return new RedirectView("/login?verified");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleInvalidToken() {
        return ResponseEntity.badRequest().build();
    }
}
