package com.example.lobbyserver.lobby;

import com.example.lobbyserver.config.ApplicationConfiguration;
import com.example.lobbyserver.security.SecurityConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LobbyController.class)
@Import({ApplicationConfiguration.class, SecurityConfiguration.class})
class LobbyControllerTest {

    private static final LobbyDao DUMMY_LOBBY = new LobbyDao(
            1L,
            "Test Lobby",
            2,
            4,
            "Test Owner",
            "localhost",
            1111);

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LobbyService lobbyService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testThatContextLoads() {
    }

    /* Test @GetMapping */
    @Test
    void testThatGetLobbiesRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/lobby"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testThatGetLobbiesReturns200() throws Exception {

        var openLobbies = new PageImpl<>(List.of(DUMMY_LOBBY), PageRequest.of(0, 10), 1);
        given(lobbyService.getOpenLobbies(any())).willReturn(openLobbies);

        mockMvc.perform(get("/lobby"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Lobby"))
                .andExpect(jsonPath("$.content[0].maxPlayers").value(4));
    }

    /* Test @PostMapping("/create") */

    @Test
    void testThatCreatingLobbyRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/lobby/create").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testThatCreatingLobbyReturns201() throws Exception {
        given(lobbyService.createNewLobby(any(), anyString())).willReturn(DUMMY_LOBBY);

        mockMvc.perform(post("/lobby/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LobbyRequest("Test Lobby", 4))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/lobby/1"));
    }

    @Test
    @WithMockUser
    void testThatCreateLobbyReturns400ForInvalidLobbyRequest() throws Exception {
        var invalidLobbyRequest = new LobbyRequest("", 0);

        mockMvc.perform(post("/lobby/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidLobbyRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /* Test @PostMapping("/join/{lobbyId}") */

    @Test
    void testThatJoinLobbyRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/lobby/join/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testThatJoinLobbyReturns200() throws Exception {
        given(lobbyService.tryJoinLobby(anyLong(), anyString()))
                .willReturn(Optional.of(new GameConnectionDetails("localhost", 1111)));

        mockMvc.perform(post("/lobby/join/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.hostname").value("localhost"))
                .andExpect(jsonPath("$.port").value(1111));
    }

    @Test
    @WithMockUser
    void testThatJoinInvalidLobbyReturns400() throws Exception {
        given(lobbyService.tryJoinLobby(anyLong(), anyString()))
                .willReturn(Optional.empty());

        mockMvc.perform(post("/lobby/join/1").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    private <T> String toJson(T obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}