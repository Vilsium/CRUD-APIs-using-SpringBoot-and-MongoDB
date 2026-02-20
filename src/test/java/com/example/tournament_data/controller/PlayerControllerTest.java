package com.example.tournament_data.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.tournament_data.dto.PlayerCreateRequest;
import com.example.tournament_data.dto.PlayerPatchRequest;
import com.example.tournament_data.dto.PlayerResponse;
import com.example.tournament_data.exception.GlobalExceptionHandler;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Stats;
import com.example.tournament_data.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PlayerController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("PlayerController Tests")
class PlayerControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Mock
        private PlayerService playerService;

        private static final String BASE_URL = "/api/v1/players";

        private PlayerResponse playerResponse;
        private PlayerResponse playerResponse2;
        private PlayerCreateRequest createRequest;
        private PlayerPatchRequest patchRequest;
        private Stats testStats;

        @BeforeEach
        void setUp() {
                // Initialize test stats
                testStats = Stats.builder()
                                .matchesPlayed(50)
                                .runsScored(2000)
                                .wicketsTaken(30)
                                .catchesTaken(25)
                                .build();

                // Initialize player response
                playerResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Mumbai Indians")
                                .role("Batsman")
                                .battingStyle("Right-handed")
                                .bowlingStyle("Right-arm medium")
                                .stats(testStats)
                                .build();

                // Initialize second player response
                playerResponse2 = PlayerResponse.builder()
                                .id(2)
                                .name("Rohit Sharma")
                                .teamName("Mumbai Indians")
                                .role("Batsman")
                                .battingStyle("Right-handed")
                                .bowlingStyle(null)
                                .stats(testStats)
                                .build();

                // Initialize create request
                createRequest = new PlayerCreateRequest();
                createRequest.setName("Virat Kohli");
                createRequest.setTeamName("Mumbai Indians");
                createRequest.setRole("Batsman");
                createRequest.setBattingStyle("Right-handed");
                createRequest.setBowlingStyle("Right-arm medium");
                createRequest.setStats(testStats);

                // Initialize patch request
                patchRequest = new PlayerPatchRequest();
        }

        // ==================== CREATE PLAYER TESTS ====================

        @Test
        @DisplayName("POST /api/v1/players - Should create player successfully")
        void createPlayer_Success() throws Exception {
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenReturn(playerResponse);

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Player created successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Virat Kohli"))
                                .andExpect(jsonPath("$.data.teamName").value("Mumbai Indians"));

                verify(playerService).create(any(PlayerCreateRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/players - Should create player with stats")
        void createPlayer_WithStats() throws Exception {
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenReturn(playerResponse);

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.stats.matchesPlayed").value(50))
                                .andExpect(jsonPath("$.data.stats.runsScored").value(2000));
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when team not found")
        void createPlayer_TeamNotFound_Returns400() throws Exception {
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenThrow(new InvalidRequestException("teamName", "Team not found"));

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when duplicate player name")
        void createPlayer_DuplicateName_Returns400() throws Exception {
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenThrow(new InvalidRequestException("name", "Player already exists"));

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());
        }

        // ==================== GET ALL PLAYERS TESTS ====================

        @Test
        @DisplayName("GET /api/v1/players - Should return all players")
        void getAllPlayers_Success() throws Exception {
                List<PlayerResponse> players = Arrays.asList(playerResponse, playerResponse2);
                when(playerService.getAllPlayers()).thenReturn(players);

                mockMvc.perform(get(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Players retrieved successfully"))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(2))
                                .andExpect(jsonPath("$.data[0].name").value("Virat Kohli"))
                                .andExpect(jsonPath("$.data[1].name").value("Rohit Sharma"));

                verify(playerService).getAllPlayers();
        }

        @Test
        @DisplayName("GET /api/v1/players - Should return empty list")
        void getAllPlayers_EmptyList() throws Exception {
                when(playerService.getAllPlayers()).thenReturn(Collections.emptyList());

                mockMvc.perform(get(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));

                verify(playerService).getAllPlayers();
        }

        // ==================== GET PLAYER BY ID TESTS ====================

        @Test
        @DisplayName("GET /api/v1/players/{id} - Should return player when found")
        void getPlayerById_Success() throws Exception {
                when(playerService.getPlayerById(1)).thenReturn(playerResponse);

                mockMvc.perform(get(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Player retrieved successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Virat Kohli"));

                verify(playerService).getPlayerById(1);
        }

        @Test
        @DisplayName("GET /api/v1/players/{id} - Should return 404 when not found")
        void getPlayerById_NotFound_Returns404() throws Exception {
                when(playerService.getPlayerById(999))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 999));

                mockMvc.perform(get(BASE_URL + "/999")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNotFound());

                verify(playerService).getPlayerById(999);
        }

        @Test
        @DisplayName("GET /api/v1/players/{id} - Should return player with stats")
        void getPlayerById_WithStats() throws Exception {
                when(playerService.getPlayerById(1)).thenReturn(playerResponse);

                mockMvc.perform(get(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.stats.matchesPlayed").value(50))
                                .andExpect(jsonPath("$.data.stats.runsScored").value(2000));
        }

        // ==================== UPDATE PLAYER TESTS ====================

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should update player successfully")
        void updatePlayer_Success() throws Exception {
                PlayerResponse updatedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Chennai Super Kings")
                                .role("All-rounder")
                                .build();

                when(playerService.updatePlayer(eq(1), any(PlayerCreateRequest.class)))
                                .thenReturn(updatedResponse);

                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Player updated successfully"))
                                .andExpect(jsonPath("$.data.id").value(1));

                verify(playerService).updatePlayer(eq(1), any(PlayerCreateRequest.class));
        }

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should return 404 when not found")
        void updatePlayer_NotFound_Returns404() throws Exception {
                when(playerService.updatePlayer(eq(999), any(PlayerCreateRequest.class)))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 999));

                mockMvc.perform(put(BASE_URL + "/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should return 400 when team not found")
        void updatePlayer_TeamNotFound_Returns400() throws Exception {
                when(playerService.updatePlayer(eq(1), any(PlayerCreateRequest.class)))
                                .thenThrow(new InvalidRequestException("teamName", "Team not found"));

                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());
        }

        // ==================== PATCH PLAYER TESTS ====================

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch player name")
        void patchPlayer_Name_Success() throws Exception {
                patchRequest.setName("Updated Name");

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Updated Name")
                                .teamName("Mumbai Indians")
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Updated Name"));

                verify(playerService).patchPlayer(eq(1), any(PlayerPatchRequest.class));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch player role")
        void patchPlayer_Role_Success() throws Exception {
                patchRequest.setRole("All-rounder");

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Mumbai Indians")
                                .role("All-rounder")
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.role").value("All-rounder"));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch player team")
        void patchPlayer_Team_Success() throws Exception {
                patchRequest.setTeamName("Chennai Super Kings");

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Chennai Super Kings")
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.teamName").value("Chennai Super Kings"));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch multiple fields")
        void patchPlayer_MultipleFields_Success() throws Exception {
                patchRequest.setName("Updated Name");
                patchRequest.setRole("Bowler");

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Updated Name")
                                .teamName("Mumbai Indians")
                                .role("Bowler")
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                                .andExpect(jsonPath("$.data.role").value("Bowler"));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should return 404 when not found")
        void patchPlayer_NotFound_Returns404() throws Exception {
                patchRequest.setName("Updated Name");

                when(playerService.patchPlayer(eq(999), any(PlayerPatchRequest.class)))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 999));

                mockMvc.perform(patch(BASE_URL + "/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should return 400 when team not found")
        void patchPlayer_TeamNotFound_Returns400() throws Exception {
                patchRequest.setTeamName("Unknown Team");

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenThrow(new InvalidRequestException("teamName", "Team not found"));

                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should return 400 when duplicate name")
        void patchPlayer_DuplicateName_Returns400() throws Exception {
                patchRequest.setName("Existing Player");

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenThrow(new InvalidRequestException("name", "Player already exists"));

                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());
        }

        // ==================== DELETE PLAYER TESTS ====================

        @Test
        @DisplayName("DELETE /api/v1/players/{id} - Should delete player successfully")
        void deletePlayer_Success() throws Exception {
                when(playerService.deletePlayer(1)).thenReturn(playerResponse);

                mockMvc.perform(delete(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Player deleted successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Virat Kohli"));

                verify(playerService).deletePlayer(1);
        }

        @Test
        @DisplayName("DELETE /api/v1/players/{id} - Should return 404 when not found")
        void deletePlayer_NotFound_Returns404() throws Exception {
                when(playerService.deletePlayer(999))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 999));

                mockMvc.perform(delete(BASE_URL + "/999")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNotFound());

                verify(playerService).deletePlayer(999);
        }

        @Test
        @DisplayName("DELETE /api/v1/players/{id} - Should return deleted player details")
        void deletePlayer_ReturnsFullDetails() throws Exception {
                when(playerService.deletePlayer(1)).thenReturn(playerResponse);

                mockMvc.perform(delete(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Virat Kohli"))
                                .andExpect(jsonPath("$.data.teamName").value("Mumbai Indians"))
                                .andExpect(jsonPath("$.data.stats.matchesPlayed").value(50));
        }
}
