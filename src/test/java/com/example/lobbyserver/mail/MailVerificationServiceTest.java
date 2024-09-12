package com.example.lobbyserver.mail;

import com.example.lobbyserver.mail.db.MailVerification;
import com.example.lobbyserver.mail.db.MailVerificationRepository;
import com.example.lobbyserver.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MailVerificationServiceTest {

    private MailVerificationService mailVerificationService;

    @Mock
    private MailService mailService;

    @Mock
    private MailVerificationRepository mailVerificationRepository;

    @Mock
    private UserService userService;

    private static final String TEST_TOKEN =
            UUID.nameUUIDFromBytes("test-token".getBytes(Charset.defaultCharset())).toString();

    @BeforeEach
    void setUp() {
        mailVerificationService = new MailVerificationService(mailService, mailVerificationRepository, userService);
    }

    @Test
    void sendVerificationMail() {
        var mailCaptor = ArgumentCaptor.forClass(Mail.class);
        var verificationCaptor = ArgumentCaptor.forClass(MailVerification.class);
        var tokenCaptor = ArgumentCaptor.forClass(String.class);

        try (var staticUcb = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            var mockUcb = mock(ServletUriComponentsBuilder.class);
            staticUcb.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockUcb);
            given(mockUcb.path(anyString())).willReturn(mockUcb);
            given(mockUcb.queryParam(eq("token"), tokenCaptor.capture())).willReturn(mockUcb);
            given(mockUcb.toUriString()).willAnswer(_ -> "localhost:8080/verify?token=" + tokenCaptor.getValue());
            mailVerificationService.sendVerificationMail("test@test.com");
        }

        verify(mailService).sendMail(mailCaptor.capture());
        verify(mailVerificationRepository).save(verificationCaptor.capture());

        var mail = mailCaptor.getValue();
        var verification = verificationCaptor.getValue();

        assertThat(mail.to()).isEqualTo("test@test.com");
        assertThat(mail.text()).contains("/verify?token=" + verification.getToken());
    }

    @Test
    void testVerifyValidTokenReturnsTrue() {
        var expiryDate = LocalDateTime.now().plusHours(1);
        var validVerification = new MailVerification(TEST_TOKEN, "test@test.com", expiryDate);
        given(mailVerificationRepository.findById(TEST_TOKEN)).willReturn(Optional.of(validVerification));

        var verificationResult = mailVerificationService.verifyMail(TEST_TOKEN);
        assertThat(verificationResult).isTrue();

        verify(mailVerificationRepository).delete(validVerification);
        verify(userService).activateUser(validVerification.getEmail());
    }

    @Test
    void testVerifyExpiredTokenReturnsFalse() {
        var expiryDate = LocalDateTime.now().minusHours(1);
        var expiredVerification = new MailVerification(TEST_TOKEN, "test@test.com", expiryDate);
        given(mailVerificationRepository.findById(TEST_TOKEN)).willReturn(Optional.of(expiredVerification));

        var verificationResult = mailVerificationService.verifyMail(TEST_TOKEN);
        assertThat(verificationResult).isFalse();

        verify(mailVerificationRepository).delete(expiredVerification);
        verify(userService, never()).activateUser(anyString());
    }

    @Test
    void testVerifyInvalidTokenReturnsFalse() {
        given(mailVerificationRepository.findById(TEST_TOKEN)).willReturn(Optional.empty());

        var verificationResult = mailVerificationService.verifyMail(TEST_TOKEN);
        assertThat(verificationResult).isFalse();

        verify(mailVerificationRepository, never()).delete(any());
        verify(userService, never()).activateUser(anyString());
    }
}