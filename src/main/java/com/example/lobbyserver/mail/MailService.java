package com.example.lobbyserver.mail;

import jakarta.validation.Valid;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Date;

@Service
@Validated
public class MailService {
    private final MailSender mailSender;

    public MailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendMail(@Valid Mail mail) {
        var message = new SimpleMailMessage();
        message.setFrom(mail.from());
        message.setTo(mail.to());
        message.setSubject(mail.subject());
        message.setText(mail.text());
        message.setSentDate(Date.from(Instant.now()));
        mailSender.send(message);
    }
}
