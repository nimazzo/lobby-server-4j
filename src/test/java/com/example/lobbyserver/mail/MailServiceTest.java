package com.example.lobbyserver.mail;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = MailService.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@ActiveProfiles("test")
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @MockBean
    private MailSender mailSender;

    @Test
    void testThatSendMailMethodArgumentIsValidated() {
        var senderIsNull = new Mail(null, "to@example.com", "subject", "text");
        var senderIsNotAValidEmail = new Mail("invalid", "to@example.com", "subject", "text");
        var recipientIsNull = new Mail("from@example.com", null, "subject", "text");
        var recipientIsNotAValidEmail = new Mail("from@example.com", "invalid", "subject", "text");
        var subjectIsNull = new Mail("from@example.com", "to@example.com", null, "text");

        var mails = List.of(
                senderIsNull,
                senderIsNotAValidEmail,
                recipientIsNull,
                recipientIsNotAValidEmail,
                subjectIsNull
        );

        for (var mail : mails) {
            assertThatExceptionOfType(ConstraintViolationException.class)
                    .isThrownBy(() -> mailService.sendMail(mail));
        }
    }

    @Test
    void testThatSendMailWithValidArgumentWorks() {
        var mail = new Mail("from@example.com", "to@example.com", "subject", "text");
        mailService.sendMail(mail);
        // assert that the mailSender.send method is called with the correct argument
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}