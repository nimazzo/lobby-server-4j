package com.example.lobbyserver.user;

import com.example.lobbyserver.mail.MailVerificationService;
import com.example.lobbyserver.security.SecurityConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class)
@Import(SecurityConfiguration.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    MailVerificationService mailVerificationService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "doesNotExist")
    void testThatGetUserThatDoesNotExistsReturns404() throws Exception {
        given(userService.getUser(anyString())).willReturn(Optional.empty());
        mockMvc.perform(get("/user/doesNotExist"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testThatGetForeignUserReturns403() throws Exception {
        mockMvc.perform(get("/user/notMe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void testThatGetUserThatExistsReturns200() throws Exception {
        given(userService.getUser("user"))
                .willReturn(Optional.of(new UserDao("user", "password", "user@user.com")));

        mockMvc.perform(get("/user/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void testThatCreateValidAccountWorks() throws Exception {
        var user = new UserDao("user", "password", "user@password.com");
        given(userService.getUser(user.username())).willReturn(Optional.of(user));

        var result = mockMvc.perform(
                        post("/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(user))
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/user/user"))
                .andReturn();

        verify(mailVerificationService, times(1)).sendVerificationMail(user.email());

        // check that created user exists at location
        var location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        mockMvc.perform(get(location)
                        .with(user("user").password("password").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.username()))
                .andExpect(jsonPath("$.password").value(user.password()))
                .andExpect(jsonPath("$.email").value(user.email()));
    }

    @Test
    void testThatCreateUserWithExistingUsernameOrEmailReturns409() throws Exception {
        given(userService.usernameExists("user")).willAnswer(
                invocation -> invocation.getArgument(0).equals("user")
        );
        given(userService.emailExists("user@user.com")).willAnswer(
                invocation -> invocation.getArgument(0).equals("user@user.com")
        );

        var usernameExistsUser = new UserDao("user", "password", "unused@unused.com");
        mockMvc.perform(
                        post("/user/register").contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(usernameExistsUser))
                                .with(csrf()))
                .andExpect(status().isConflict());

        var emailExistsUser = new UserDao("unused", "password", "user@user.com");
        mockMvc.perform(
                        post("/user/register").contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(emailExistsUser))
                                .with(csrf()))
                .andExpect(status().isConflict());

        verifyNoInteractions(mailVerificationService);
    }

    @Test
    void testThatRequestedUserIsValidatedBeforeCreation() throws Exception {
        var invalidUser = new UserDao("", "", "");

        mockMvc.perform(post("/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidUser)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mailVerificationService);
    }

    private <T> String toJson(T obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}