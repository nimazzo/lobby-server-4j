package com.example.lobbyserver.mail;

import com.example.lobbyserver.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MailVerificationController.class)
@Import(SecurityConfiguration.class)
@ActiveProfiles("test")
class MailVerificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MailVerificationService mailVerificationService;

    @Test
    void testThatVerifyValidTokenReturns200() throws Exception {
        given(mailVerificationService.verifyMail("test-token")).willReturn(true);
        mockMvc.perform(get("/verify?token=test-token"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?verified"));
    }

    @Test
    void testThatVerifyInvalidTokenReturns400() throws Exception {
        given(mailVerificationService.verifyMail("test-token")).willReturn(false);
        mockMvc.perform(get("/verify?token=test-token"))
                .andExpect(status().isBadRequest());
    }
}