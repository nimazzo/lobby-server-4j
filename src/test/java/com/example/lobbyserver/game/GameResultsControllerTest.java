package com.example.lobbyserver.game;

import com.example.lobbyserver.config.ApplicationConfiguration;
import com.example.lobbyserver.game.db.GameResult;
import com.example.lobbyserver.game.db.GameResultRepository;
import com.example.lobbyserver.security.SecurityConfiguration;
import com.example.lobbyserver.user.db.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(GameResultsController.class)
@Import({ApplicationConfiguration.class, SecurityConfiguration.class})
@ActiveProfiles("test")
class GameResultsControllerTest {

    private static final LocalDateTime DUMMY_DATE = LocalDateTime.parse("2021-01-01T00:00:00");

    private static final User DUMMY_USER_1 = new User();
    private static final User DUMMY_USER_2 = new User();
    private static final User DUMMY_USER_3 = new User();

    static {
        DUMMY_USER_1.setUsername("user1");
        DUMMY_USER_2.setUsername("user2");
        DUMMY_USER_3.setUsername("user3");
    }

    private static final List<GameResult> DUMMY_RESULTS = List.of(
            new GameResult(1L, DUMMY_USER_1, 100L, 1, 2, 1000L, DUMMY_DATE),
            new GameResult(2L, DUMMY_USER_1, 200L, 1, 3, 2000L, DUMMY_DATE),
            new GameResult(3L, DUMMY_USER_2, 200L, 2, 4, 4000L, DUMMY_DATE),
            new GameResult(4L, DUMMY_USER_3, 400L, 4, 5, 5000L, DUMMY_DATE)
    );

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GameResultRepository gameResultRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testThatGetEmptyGameResultsWorks() throws Exception {
        given(gameResultRepository.findAll(any(Pageable.class))).willReturn(
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0)
        );
        mockMvc.perform(get("/game-results"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    void testThatGetAllGameResultsWorks() throws Exception {
        given(gameResultRepository.findAll(any(Pageable.class))).willReturn(
                new PageImpl<>(DUMMY_RESULTS, PageRequest.of(0, 10), DUMMY_RESULTS.size())
        );
        var result = mockMvc.perform(get("/game-results"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(DUMMY_RESULTS.size()))
                .andExpect(jsonPath("$.page.totalElements").value(DUMMY_RESULTS.size()))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andReturn();

        var contentJson = result.getResponse().getContentAsString();
        var document = JsonPath.parse(contentJson);

        var contentArrayJson = document.read("$.content").toString();
        var returnedResults = objectMapper.readValue(contentArrayJson, new TypeReference<List<GameResultDao>>() {
        });

        assertThat(returnedResults).containsExactlyInAnyOrderElementsOf(
                DUMMY_RESULTS.stream().map(GameResultDao::fromGameResult).toList()
        );
    }

    @Test
    void testThatGetAllGameResultsOfUserWorks() throws Exception {
        var filteredResults = DUMMY_RESULTS.stream().filter(r -> r.getUser().getUsername().equals("user1")).toList();
        given(gameResultRepository.findByUser_Username(anyString(), any())).willReturn(
                new PageImpl<>(filteredResults, PageRequest.of(0, 10), filteredResults.size())
        );

        var result = mockMvc.perform(get("/game-results").param("username", "user1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(filteredResults.size()))
                .andExpect(jsonPath("$.page.totalElements").value(filteredResults.size()))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andDo(print())
                .andReturn();

        var contentJson = result.getResponse().getContentAsString();
        var document = JsonPath.parse(contentJson);

        var contentArrayJson = document.read("$.content").toString();
        var returnedResults = objectMapper.readValue(contentArrayJson, new TypeReference<List<GameResultDao>>() {
        });

        assertThat(returnedResults).containsExactlyInAnyOrderElementsOf(
                filteredResults.stream().map(GameResultDao::fromGameResult).toList()
        );

        verify(gameResultRepository).findByUser_Username(eq("user1"), any());
        verifyNoMoreInteractions(gameResultRepository);
    }
}