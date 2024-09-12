package com.example.lobbyserver.mail;

import com.example.lobbyserver.mail.db.MailVerification;
import com.example.lobbyserver.mail.db.MailVerificationRepository;
import com.example.lobbyserver.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MailVerificationService {

    @Value("${mail.verification.expiration.hours}")
    private int EXPIRATION_HOURS;

    private final MailService mailService;
    private final MailVerificationRepository mailVerificationRepository;
    private final UserService userService;

    public MailVerificationService(MailService mailService, MailVerificationRepository mailVerificationRepository, UserService userService) {
        this.mailService = mailService;
        this.mailVerificationRepository = mailVerificationRepository;
        this.userService = userService;
    }

    public void sendVerificationMail(String email) {
        var verificationToken = UUID.randomUUID().toString();
        var expiryDate = LocalDateTime.now().plusHours(EXPIRATION_HOURS);

        mailVerificationRepository.save(new MailVerification(verificationToken, email, expiryDate));

        var verificationLink = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/verify").queryParam("token", verificationToken)
                .toUriString();

        var verificationMail = new Mail(
                "noreply@lobby-server.com",
                email,
                "Account Verification",
                """
                        To verify your account, please click the following link: %s%n\
                        The link will expire in %d hours.\
                        """
                        .formatted(verificationLink, EXPIRATION_HOURS)
        );
        mailService.sendMail(verificationMail);
    }

    @Transactional
    public boolean verifyMail(String token) {
        var verificationOptional = mailVerificationRepository.findById(token);

        if (verificationOptional.isEmpty()) {
            return false;
        }

        var verification = verificationOptional.get();
        var valid = verification.getExpiryDate().isAfter(LocalDateTime.now());
        mailVerificationRepository.delete(verification);

        if (valid) {
            userService.activateUser(verification.getEmail());
        }

        return valid;
    }
}
