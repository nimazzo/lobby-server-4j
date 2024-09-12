package com.example.lobbyserver.mail;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("verify")
public class MailVerificationController {

    private final MailVerificationService mailVerificationService;

    public MailVerificationController(MailVerificationService mailVerificationService) {
        this.mailVerificationService = mailVerificationService;
    }

    @GetMapping
    public ResponseEntity<Void> verifyMail(@RequestParam("token") String token) {
        if (!mailVerificationService.verifyMail(token)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

}
