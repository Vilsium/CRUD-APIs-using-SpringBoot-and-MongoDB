package com.example.tournament_data.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.tournament_data.dto.PlayerCreateRequest;
import com.example.tournament_data.dto.PlayerPatchRequest;
import com.example.tournament_data.dto.PlayerResponse;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Stats;
import com.example.tournament_data.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PlayerController.class)
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
                // Arrange
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenReturn(playerResponse);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Player created successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Virat Kohli"))
                                .andExpect(jsonPath("$.data.teamName").value("Mumbai Indians"))
                                .andExpect(jsonPath("$.data.role").value("Batsman"))
                                .andExpect(jsonPath("$.data.battingStyle").value("Right-handed"))
                                .andExpect(jsonPath("$.data.bowlingStyle").value("Right-arm medium"));

                verify(playerService).create(any(PlayerCreateRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/players - Should create player with stats")
        void createPlayer_WithStats() throws Exception {
                // Arrange
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenReturn(playerResponse);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.stats.matchesPlayed").value(50))
                                .andExpect(jsonPath("$.data.stats.runsScored").value(2000))
                                .andExpect(jsonPath("$.data.stats.wicketsTaken").value(30))
                                .andExpect(jsonPath("$.data.stats.catchesTaken").value(25));
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when name is blank")
        void createPlayer_BlankName_Returns400() throws Exception {
                // Arrange
                createRequest.setName("");

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).create(any());
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when name is null")
        void createPlayer_NullName_Returns400() throws Exception {
                // Arrange
                createRequest.setName(null);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).create(any());
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when team name is blank")
        void createPlayer_BlankTeamName_Returns400() throws Exception {
                // Arrange
                createRequest.setTeamName("");

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).create(any());
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when team name is null")
        void createPlayer_NullTeamName_Returns400() throws Exception {
                // Arrange
                createRequest.setTeamName(null);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).create(any());
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when team not found")
        void createPlayer_TeamNotFound_Returns400() throws Exception {
                // Arrange
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenThrow(new InvalidRequestException("teamName",
                                                "Team not found with name: Unknown Team"));

                createRequest.setTeamName("Unknown Team");

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when duplicate player name in team")
        void createPlayer_DuplicateName_Returns400() throws Exception {
                // Arrange
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenThrow(new InvalidRequestException("name",
                                                "Player with name 'Virat Kohli' already exists in team"));

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/v1/players - Should return 400 when team is full")
        void createPlayer_TeamFull_Returns400() throws Exception {
                // Arrange
                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenThrow(new InvalidRequestException("teamName",
                                                "Team has reached maximum player limit of 25"));

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/v1/players - Should create player without optional fields")
        void createPlayer_WithoutOptionalFields() throws Exception {
                // Arrange
                PlayerCreateRequest minimalRequest = new PlayerCreateRequest();
                minimalRequest.setName("New Player");
                minimalRequest.setTeamName("Mumbai Indians");

                PlayerResponse minimalResponse = PlayerResponse.builder()
                                .id(1)
                                .name("New Player")
                                .teamName("Mumbai Indians")
                                .build();

                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenReturn(minimalResponse);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(minimalRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.name").value("New Player"))
                                .andExpect(jsonPath("$.data.teamName").value("Mumbai Indians"));
        }

        // ==================== GET ALL PLAYERS TESTS ====================

        @Test
        @DisplayName("GET /api/v1/players - Should return all players")
        void getAllPlayers_Success() throws Exception {
                // Arrange
                List<PlayerResponse> players = Arrays.asList(playerResponse, playerResponse2);
                when(playerService.getAllPlayers()).thenReturn(players);

                // Act & Assert
                mockMvc.perform(get(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Players retrieved successfully"))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(2))
                                .andExpect(jsonPath("$.data[0].id").value(1))
                                .andExpect(jsonPath("$.data[0].name").value("Virat Kohli"))
                                .andExpect(jsonPath("$.data[1].id").value(2))
                                .andExpect(jsonPath("$.data[1].name").value("Rohit Sharma"));

                verify(playerService).getAllPlayers();
        }

        @Test
        @DisplayName("GET /api/v1/players - Should return empty list when no players exist")
        void getAllPlayers_EmptyList() throws Exception {
                // Arrange
                when(playerService.getAllPlayers()).thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));

                verify(playerService).getAllPlayers();
        }

        @Test
        @DisplayName("GET /api/v1/players - Should return players with stats")
        void getAllPlayers_WithStats() throws Exception {
                // Arrange
                List<PlayerResponse> players = Arrays.asList(playerResponse);
                when(playerService.getAllPlayers()).thenReturn(players);

                // Act & Assert
                mockMvc.perform(get(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].stats.matchesPlayed").value(50))
                                .andExpect(jsonPath("$.data[0].stats.runsScored").value(2000));
        }

        // ==================== GET PLAYER BY ID TESTS ====================

        @Test
        @DisplayName("GET /api/v1/players/{id} - Should return player when found")
        void getPlayerById_Success() throws Exception {
                // Arrange
                when(playerService.getPlayerById(1)).thenReturn(playerResponse);

                // Act & Assert
                mockMvc.perform(get(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Player retrieved successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Virat Kohli"))
                                .andExpect(jsonPath("$.data.teamName").value("Mumbai Indians"))
                                .andExpect(jsonPath("$.data.role").value("Batsman"));

                verify(playerService).getPlayerById(1);
        }

        @Test
        @DisplayName("GET /api/v1/players/{id} - Should return 404 when player not found")
        void getPlayerById_NotFound_Returns404() throws Exception {
                // Arrange
                when(playerService.getPlayerById(999))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 999));

                // Act & Assert
                mockMvc.perform(get(BASE_URL + "/999")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(playerService).getPlayerById(999);
        }

        @Test
        @DisplayName("GET /api/v1/players/{id} - Should return player with stats")
        void getPlayerById_WithStats() throws Exception {
                // Arrange
                when(playerService.getPlayerById(1)).thenReturn(playerResponse);

                // Act & Assert
                mockMvc.perform(get(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.stats.matchesPlayed").value(50))
                                .andExpect(jsonPath("$.data.stats.runsScored").value(2000))
                                .andExpect(jsonPath("$.data.stats.wicketsTaken").value(30))
                                .andExpect(jsonPath("$.data.stats.catchesTaken").value(25));
        }

        @Test
        @DisplayName("GET /api/v1/players/{id} - Should return player without stats")
        void getPlayerById_WithoutStats() throws Exception {
                // Arrange
                PlayerResponse responseWithoutStats = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Mumbai Indians")
                                .stats(null)
                                .build();

                when(playerService.getPlayerById(1)).thenReturn(responseWithoutStats);

                // Act & Assert
                mockMvc.perform(get(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.stats").doesNotExist());
        }

        // ==================== UPDATE PLAYER TESTS ====================

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should update player successfully")
        void updatePlayer_Success() throws Exception {
                // Arrange
                PlayerResponse updatedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Chennai Super Kings")
                                .role("All-rounder")
                                .battingStyle("Right-handed")
                                .bowlingStyle("Right-arm medium")
                                .stats(testStats)
                                .build();

                when(playerService.updatePlayer(eq(1), any(PlayerCreateRequest.class)))
                                .thenReturn(updatedResponse);

                createRequest.setTeamName("Chennai Super Kings");
                createRequest.setRole("All-rounder");

                // Act & Assert
                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Player updated successfully"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.teamName").value("Chennai Super Kings"))
                                .andExpect(jsonPath("$.data.role").value("All-rounder"));

                verify(playerService).updatePlayer(eq(1), any(PlayerCreateRequest.class));
        }

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should return 404 when player not found")
        void updatePlayer_NotFound_Returns404() throws Exception {
                // Arrange
                when(playerService.updatePlayer(eq(999), any(PlayerCreateRequest.class)))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 999));

                // Act & Assert
                mockMvc.perform(put(BASE_URL + "/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should return 400 when name is blank")
        void updatePlayer_BlankName_Returns400() throws Exception {
                // Arrange
                createRequest.setName("");

                // Act & Assert
                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).updatePlayer(any(), any());
        }

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should return 400 when team name is blank")
        void updatePlayer_BlankTeamName_Returns400() throws Exception {
                // Arrange
                createRequest.setTeamName("");

                // Act & Assert
                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).updatePlayer(any(), any());
        }

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should return 400 when team not found")
        void updatePlayer_TeamNotFound_Returns400() throws Exception {
                // Arrange
                when(playerService.updatePlayer(eq(1), any(PlayerCreateRequest.class)))
                                .thenThrow(new InvalidRequestException("teamName",
                                                "Team not found with name: Unknown Team"));

                createRequest.setTeamName("Unknown Team");

                // Act & Assert
                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should return 400 when duplicate name in new team")
        void updatePlayer_DuplicateNameInTeam_Returns400() throws Exception {
                // Arrange
                when(playerService.updatePlayer(eq(1), any(PlayerCreateRequest.class)))
                                .thenThrow(new InvalidRequestException("name",
                                                "Player with name already exists in team"));

                // Act & Assert
                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        // ==================== PATCH PLAYER TESTS ====================

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch player name successfully")
        void patchPlayer_Name_Success() throws Exception {
                // Arrange
                patchRequest.setName("Updated Name");

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Updated Name")
                                .teamName("Mumbai Indians")
                                .role("Batsman")
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Player updated successfully"))
                                .andExpect(jsonPath("$.data.name").value("Updated Name"));

                verify(playerService).patchPlayer(eq(1), any(PlayerPatchRequest.class));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch player role successfully")
        void patchPlayer_Role_Success() throws Exception {
                // Arrange
                patchRequest.setRole("All-rounder");

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Mumbai Indians")
                                .role("All-rounder")
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.role").value("All-rounder"));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch player team successfully")
        void patchPlayer_Team_Success() throws Exception {
                // Arrange
                patchRequest.setTeamName("Chennai Super Kings");

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Chennai Super Kings")
                                .role("Batsman")
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.teamName").value("Chennai Super Kings"));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch player stats successfully")
        void patchPlayer_Stats_Success() throws Exception {
                // Arrange
                Stats newStats = Stats.builder()
                                .matchesPlayed(100)
                                .runsScored(5000)
                                .build();
                patchRequest.setStats(newStats);

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Virat Kohli")
                                .teamName("Mumbai Indians")
                                .stats(newStats)
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.stats.matchesPlayed").value(100))
                                .andExpect(jsonPath("$.data.stats.runsScored").value(5000));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should patch multiple fields successfully")
        void patchPlayer_MultipleFields_Success() throws Exception {
                // Arrange
                patchRequest.setName("Updated Name");
                patchRequest.setRole("Bowler");
                patchRequest.setBattingStyle("Left-handed");

                PlayerResponse patchedResponse = PlayerResponse.builder()
                                .id(1)
                                .name("Updated Name")
                                .teamName("Mumbai Indians")
                                .role("Bowler")
                                .battingStyle("Left-handed")
                                .build();

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(patchedResponse);

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                                .andExpect(jsonPath("$.data.role").value("Bowler"))
                                .andExpect(jsonPath("$.data.battingStyle").value("Left-handed"));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should return 404 when player not found")
        void patchPlayer_NotFound_Returns404() throws Exception {
                // Arrange
                patchRequest.setName("Updated Name");

                when(playerService.patchPlayer(eq(999), any(PlayerPatchRequest.class)))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 999));

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should return 400 when team not found")
        void patchPlayer_TeamNotFound_Returns400() throws Exception {
                // Arrange
                patchRequest.setTeamName("Unknown Team");

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenThrow(new InvalidRequestException("teamName",
                                                "Team not found with name: Unknown Team"));

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should return 400 when duplicate name")
        void patchPlayer_DuplicateName_Returns400() throws Exception {
                // Arrange
                patchRequest.setName("Existing Player");

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenThrow(new InvalidRequestException("name",
                                                "Player with name already exists in team"));

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should return 400 when new team is full")
        void patchPlayer_TeamFull_Returns400() throws Exception {
                // Arrange
                patchRequest.setTeamName("Full Team");

                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenThrow(new InvalidRequestException("teamName",
                                                "Team has reached maximum player limit of 25"));

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        // ==================== DELETE PLAYER TESTS ====================

        @Test
        @DisplayName("DELETE /api/v1/players/{id} - Should delete player successfully")
        void deletePlayer_Success() throws Exception {
                // Arrange
                when(playerService.deletePlayer(1)).thenReturn(playerResponse);

                // Act & Assert
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
        @DisplayName("DELETE /api/v1/players/{id} - Should return 404 when player not found")
        void deletePlayer_NotFound_Returns404() throws Exception {
                // Arrange
                when(playerService.deletePlayer(999))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 999));

                // Act & Assert
                mockMvc.perform(delete(BASE_URL + "/999")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(playerService).deletePlayer(999);
        }

        @Test
        @DisplayName("DELETE /api/v1/players/{id} - Should return deleted player with all details")
        void deletePlayer_ReturnsFullDetails() throws Exception {
                // Arrange
                when(playerService.deletePlayer(1)).thenReturn(playerResponse);

                // Act & Assert
                mockMvc.perform(delete(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Virat Kohli"))
                                .andExpect(jsonPath("$.data.teamName").value("Mumbai Indians"))
                                .andExpect(jsonPath("$.data.role").value("Batsman"))
                                .andExpect(jsonPath("$.data.battingStyle").value("Right-handed"))
                                .andExpect(jsonPath("$.data.bowlingStyle").value("Right-arm medium"))
                                .andExpect(jsonPath("$.data.stats.matchesPlayed").value(50));
        }

        // ==================== EDGE CASE TESTS ====================

        @Test
        @DisplayName("POST /api/v1/players - Should handle empty request body")
        void createPlayer_EmptyBody_Returns400() throws Exception {
                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).create(any());
        }

        @Test
        @DisplayName("POST /api/v1/players - Should handle invalid JSON")
        void createPlayer_InvalidJson_Returns400() throws Exception {
                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("invalid json"))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).create(any());
        }

        @Test
        @DisplayName("GET /api/v1/players/{id} - Should handle string ID")
        void getPlayerById_StringId_Returns400() throws Exception {
                // Act & Assert
                mockMvc.perform(get(BASE_URL + "/abc")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).getPlayerById(any());
        }

        @Test
        @DisplayName("PUT /api/v1/players/{id} - Should handle empty request body")
        void updatePlayer_EmptyBody_Returns400() throws Exception {
                // Act & Assert
                mockMvc.perform(put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(playerService, never()).updatePlayer(any(), any());
        }

        @Test
        @DisplayName("PATCH /api/v1/players/{id} - Should handle empty request body")
        void patchPlayer_EmptyBody_Success() throws Exception {
                // Arrange - empty patch should still work (no changes)
                when(playerService.patchPlayer(eq(1), any(PlayerPatchRequest.class)))
                                .thenReturn(playerResponse);

                // Act & Assert
                mockMvc.perform(patch(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle request without content type")
        void request_NoContentType_Returns415() throws Exception {
                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("GET /api/v1/players - Should return players in correct order")
        void getAllPlayers_CorrectOrder() throws Exception {
                // Arrange
                PlayerResponse player1 = PlayerResponse.builder().id(1).name("First Player").teamName("Team A").build();
                PlayerResponse player2 = PlayerResponse.builder().id(2).name("Second Player").teamName("Team A")
                                .build();
                PlayerResponse player3 = PlayerResponse.builder().id(3).name("Third Player").teamName("Team B").build();

                List<PlayerResponse> players = Arrays.asList(player1, player2, player3);
                when(playerService.getAllPlayers()).thenReturn(players);

                // Act & Assert
                mockMvc.perform(get(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].id").value(1))
                                .andExpect(jsonPath("$.data[1].id").value(2))
                                .andExpect(jsonPath("$.data[2].id").value(3));
        }

        @Test
        @DisplayName("POST /api/v1/players - Should handle very long name")
        void createPlayer_VeryLongName() throws Exception {
                // Arrange
                String longName = "A".repeat(500);
                createRequest.setName(longName);

                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenReturn(playerResponse);

                // Act & Assert - behavior depends on validation rules
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print());
                // Status depends on your validation annotations
        }

        @Test
        @DisplayName("POST /api/v1/players - Should handle special characters in name")
        void createPlayer_SpecialCharactersInName() throws Exception {
                // Arrange
                createRequest.setName("O'Brien Jr.");

                PlayerResponse responseWithSpecialName = PlayerResponse.builder()
                                .id(1)
                                .name("O'Brien Jr.")
                                .teamName("Mumbai Indians")
                                .build();

                when(playerService.create(any(PlayerCreateRequest.class)))
                                .thenReturn(responseWithSpecialName);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.name").value("O'Brien Jr."));
        }

        @Test
        @DisplayName("DELETE /api/v1/players/{id} - Should handle negative ID")
        void deletePlayer_NegativeId() throws Exception {
                // Arrange
                when(playerService.deletePlayer(-1))
                                .thenThrow(new ResourceNotFoundException("Player", "id", -1));

                // Act & Assert
                mockMvc.perform(delete(BASE_URL + "/-1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/v1/players/{id} - Should handle zero ID")
        void deletePlayer_ZeroId() throws Exception {
                // Arrange
                when(playerService.deletePlayer(0))
                                .thenThrow(new ResourceNotFoundException("Player", "id", 0));

                // Act & Assert
                mockMvc.perform(delete(BASE_URL + "/0")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }
}
