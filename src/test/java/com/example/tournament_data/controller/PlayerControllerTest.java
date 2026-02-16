package com.example.tournament_data.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Stats;
import com.example.tournament_data.service.PlayerService;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(PlayerController.class)
@DisplayName("PlayerController Unit Tests")
@ExtendWith(SpringExtension.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PlayerService playerService;

    private Player testPlayer;
    private Stats testStats;

    private static final String BASE_URL = "/api/players";
    private static final String PLAYER_ID = "64a1b2c3d4e5f6g7h8i9j0k1";
    private static final String TEAM_ID = "64a1b2c3d4e5f6g7h8i9j0k2";

    @BeforeEach
    void setUp() {
        // Initialize test stats
        testStats = Stats.builder()
                .matchesPlayed(150)
                .runsScored(12000)
                .wicketsTaken(45)
                .catchesTaken(80)
                .build();

        // Initialize test player
        testPlayer = new Player();
        testPlayer.setId(PLAYER_ID);
        testPlayer.setName("Virat Kohli");
        testPlayer.setRole("Batsman");
        testPlayer.setBattingStyle("Right-Handed");
        testPlayer.setBowlingStyle("Right-Arm Medium");
        testPlayer.setTeamId(null);
        testPlayer.setStats(testStats);
    }

    private Player createPlayer(String id, String name, String role, String battingStyle, String bowlingStyle) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setRole(role);
        player.setBattingStyle(battingStyle);
        player.setBowlingStyle(bowlingStyle);
        return player;
    }

    private String createPlayerJson(String name, String role, String battingStyle, String bowlingStyle) {
        return String.format("""
                {
                    "name": "%s",
                    "role": "%s",
                    "battingStyle": "%s",
                    "bowlingStyle": "%s"
                }
                """, name, role, battingStyle, bowlingStyle);
    }

    private String createPlayerJsonWithStats(String name, String role, String battingStyle, String bowlingStyle,
            int matches, int runs, int wickets, int catches) {
        return String.format("""
                {
                    "name": "%s",
                    "role": "%s",
                    "battingStyle": "%s",
                    "bowlingStyle": "%s",
                    "stats": {
                        "matchesPlayed": %d,
                        "runsScored": %d,
                        "wicketsTaken": %d,
                        "catchesTaken": %d
                    }
                }
                """, name, role, battingStyle, bowlingStyle, matches, runs, wickets, catches);
    }

    private String createPlayerJsonWithTeam(String name, String role, String battingStyle,
            String bowlingStyle, String teamId) {
        return String.format("""
                {
                    "name": "%s",
                    "role": "%s",
                    "battingStyle": "%s",
                    "bowlingStyle": "%s",
                    "teamId": "%s"
                }
                """, name, role, battingStyle, bowlingStyle, teamId);
    }

    @Nested
    @DisplayName("GET /api/players - Get All Players")
    class GetAllPlayersTests {

        @Test
        @DisplayName("Should return all players with 200 OK")
        void getAllPlayers_ShouldReturnAllPlayers() throws Exception {
            // Arrange
            Player player2 = createPlayer("player2", "MS Dhoni", "Wicket-Keeper", "Right-Handed", "Right-Arm Medium");
            List<Player> players = Arrays.asList(testPlayer, player2);

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
                    .andExpect(jsonPath("$.data[0].name").value("Virat Kohli"))
                    .andExpect(jsonPath("$.data[0].role").value("Batsman"))
                    .andExpect(jsonPath("$.data[1].name").value("MS Dhoni"))
                    .andExpect(jsonPath("$.data[1].role").value("Wicket-Keeper"));

            verify(playerService, times(1)).getAllPlayers();
        }

        @Test
        @DisplayName("Should return empty list when no players exist")
        void getAllPlayers_WhenNoPlayers_ShouldReturnEmptyList() throws Exception {
            // Arrange
            when(playerService.getAllPlayers()).thenReturn(new ArrayList<>());

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));

            verify(playerService, times(1)).getAllPlayers();
        }

        @Test
        @DisplayName("Should return players with stats")
        void getAllPlayers_ShouldReturnPlayersWithStats() throws Exception {
            // Arrange
            when(playerService.getAllPlayers()).thenReturn(Arrays.asList(testPlayer));

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].stats.matchesPlayed").value(150))
                    .andExpect(jsonPath("$.data[0].stats.runsScored").value(12000))
                    .andExpect(jsonPath("$.data[0].stats.wicketsTaken").value(45))
                    .andExpect(jsonPath("$.data[0].stats.catchesTaken").value(80));
        }

        @Test
        @DisplayName("Should handle service exception")
        void getAllPlayers_WhenServiceThrowsException_ShouldReturn500() throws Exception {
            // Arrange
            when(playerService.getAllPlayers()).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/players/{id} - Get Player By ID")
    class GetPlayerByIdTests {

        @Test
        @DisplayName("Should return player when found")
        void getPlayerById_WhenExists_ShouldReturnPlayer() throws Exception {
            // Arrange
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(PLAYER_ID))
                    .andExpect(jsonPath("$.data.name").value("Virat Kohli"))
                    .andExpect(jsonPath("$.data.role").value("Batsman"))
                    .andExpect(jsonPath("$.data.battingStyle").value("Right-Handed"))
                    .andExpect(jsonPath("$.data.bowlingStyle").value("Right-Arm Medium"));

            verify(playerService, times(1)).getPlayerById(PLAYER_ID);
        }

        @Test
        @DisplayName("Should return player with all stats fields")
        void getPlayerById_ShouldReturnPlayerWithStats() throws Exception {
            // Arrange
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats.matchesPlayed").value(150))
                    .andExpect(jsonPath("$.data.stats.runsScored").value(12000))
                    .andExpect(jsonPath("$.data.stats.wicketsTaken").value(45))
                    .andExpect(jsonPath("$.data.stats.catchesTaken").value(80));
        }

        @Test
        @DisplayName("Should return 404 when player not found")
        void getPlayerById_WhenNotExists_ShouldReturn404() throws Exception {
            // Arrange
            when(playerService.getPlayerById(PLAYER_ID))
                    .thenThrow(new ResourceNotFoundException("Player", "id", PLAYER_ID));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(playerService, times(1)).getPlayerById(PLAYER_ID);
        }

        @Test
        @DisplayName("Should return player with teamId")
        void getPlayerById_WithTeamId_ShouldReturnPlayerWithTeam() throws Exception {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.teamId").value(TEAM_ID));
        }
    }

    @Nested
    @DisplayName("POST /api/players - Create Player")
    class CreatePlayerTests {

        @Test
        @DisplayName("Should create player and return 201 Created")
        void createPlayer_WithValidData_ShouldReturn201() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Virat Kohli", "Batsman", "Right-Handed", "Right-Arm Medium");
            when(playerService.create(any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player created successfully"))
                    .andExpect(jsonPath("$.data.id").value(PLAYER_ID))
                    .andExpect(jsonPath("$.data.name").value("Virat Kohli"));

            verify(playerService, times(1)).create(any(Player.class));
        }

        @Test
        @DisplayName("Should create player with stats")
        void createPlayer_WithStats_ShouldReturn201() throws Exception {
            // Arrange
            String requestBody = createPlayerJsonWithStats("Virat Kohli", "Batsman",
                    "Right-Handed", "Right-Arm Medium", 150, 12000, 45, 80);
            when(playerService.create(any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.stats.matchesPlayed").value(150))
                    .andExpect(jsonPath("$.data.stats.runsScored").value(12000));
        }

        @Test
        @DisplayName("Should create player with teamId")
        void createPlayer_WithTeamId_ShouldReturn201() throws Exception {
            // Arrange
            String requestBody = createPlayerJsonWithTeam("Virat Kohli", "Batsman",
                    "Right-Handed", "Right-Arm Medium", TEAM_ID);
            testPlayer.setTeamId(TEAM_ID);
            when(playerService.create(any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.teamId").value(TEAM_ID));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void createPlayer_WithBlankName_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("", "Batsman", "Right-Handed", "Right-Arm Medium");

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when name is too short")
        void createPlayer_WithShortName_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("A", "Batsman", "Right-Handed", "Right-Arm Medium");

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when role is blank")
        void createPlayer_WithBlankRole_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Virat Kohli", "", "Right-Handed", "Right-Arm Medium");

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when role is invalid")
        void createPlayer_WithInvalidRole_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Virat Kohli", "InvalidRole", "Right-Handed", "Right-Arm Medium");

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when batting style is invalid")
        void createPlayer_WithInvalidBattingStyle_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Virat Kohli", "Batsman", "Invalid-Style", "Right-Arm Medium");

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when bowling style is invalid")
        void createPlayer_WithInvalidBowlingStyle_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Virat Kohli", "Batsman", "Right-Handed", "Invalid-Bowling");

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when team not found")
        void createPlayer_WithInvalidTeamId_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJsonWithTeam("Virat Kohli", "Batsman",
                    "Right-Handed", "Right-Arm Medium", "invalidTeamId");
            when(playerService.create(any(Player.class)))
                    .thenThrow(new InvalidRequestException("teamId", "Team not found"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when duplicate player in team")
        void createPlayer_WithDuplicateNameInTeam_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJsonWithTeam("Existing Player", "Batsman",
                    "Right-Handed", "Right-Arm Medium", TEAM_ID);
            when(playerService.create(any(Player.class)))
                    .thenThrow(new InvalidRequestException("player", "Player already exists in team"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should create player with all valid roles")
        void createPlayer_WithAllValidRoles_ShouldReturn201() throws Exception {
            String[] validRoles = { "Batsman", "Bowler", "All-Rounder", "Wicket-Keeper" };

            for (String role : validRoles) {
                // Arrange
                String requestBody = createPlayerJson("Test Player", role, "Right-Handed", "Right-Arm Medium");
                testPlayer.setRole(role);
                when(playerService.create(any(Player.class))).thenReturn(testPlayer);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isCreated());
            }
        }

        @Test
        @DisplayName("Should create player with all valid batting styles")
        void createPlayer_WithAllValidBattingStyles_ShouldReturn201() throws Exception {
            String[] validStyles = { "Right-Handed", "Left-Handed" };

            for (String style : validStyles) {
                // Arrange
                String requestBody = createPlayerJson("Test Player", "Batsman", style, "Right-Arm Medium");
                testPlayer.setBattingStyle(style);
                when(playerService.create(any(Player.class))).thenReturn(testPlayer);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isCreated());
            }
        }

        @Test
        @DisplayName("Should create player with all valid bowling styles")
        void createPlayer_WithAllValidBowlingStyles_ShouldReturn201() throws Exception {
            String[] validStyles = { "Right-Arm Fast", "Left-Arm Fast", "Right-Arm Medium",
                    "Left-Arm Medium", "Right-Arm Spin", "Left-Arm Spin", "None" };

            for (String style : validStyles) {
                // Arrange
                String requestBody = createPlayerJson("Test Player", "Batsman", "Right-Handed", style);
                testPlayer.setBowlingStyle(style);
                when(playerService.create(any(Player.class))).thenReturn(testPlayer);

                // Act & Assert
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isCreated());
            }
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void createPlayer_WithEmptyBody_ShouldReturn400() throws Exception {
            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is invalid JSON")
        void createPlayer_WithInvalidJson_ShouldReturn400() throws Exception {
            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/players/update/{id} - Update Player")
    class UpdatePlayerTests {

        @Test
        @DisplayName("Should update player and return 200 OK")
        void updatePlayer_WithValidData_ShouldReturn200() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Virat Kohli Updated", "All-Rounder",
                    "Right-Handed", "Right-Arm Spin");
            Player updatedPlayer = createPlayer(PLAYER_ID, "Virat Kohli Updated", "All-Rounder",
                    "Right-Handed", "Right-Arm Spin");
            when(playerService.updatePlayer(eq(PLAYER_ID), any(Player.class))).thenReturn(updatedPlayer);

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player updated successfully"))
                    .andExpect(jsonPath("$.data.name").value("Virat Kohli Updated"))
                    .andExpect(jsonPath("$.data.role").value("All-Rounder"));

            verify(playerService, times(1)).updatePlayer(eq(PLAYER_ID), any(Player.class));
        }

        @Test
        @DisplayName("Should update player with new stats")
        void updatePlayer_WithNewStats_ShouldReturn200() throws Exception {
            // Arrange
            String requestBody = createPlayerJsonWithStats("Virat Kohli", "Batsman",
                    "Right-Handed", "Right-Arm Medium", 200, 15000, 50, 100);
            Stats newStats = Stats.builder()
                    .matchesPlayed(200)
                    .runsScored(15000)
                    .wicketsTaken(50)
                    .catchesTaken(100)
                    .build();
            testPlayer.setStats(newStats);
            when(playerService.updatePlayer(eq(PLAYER_ID), any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats.matchesPlayed").value(200))
                    .andExpect(jsonPath("$.data.stats.runsScored").value(15000));
        }

        @Test
        @DisplayName("Should return 404 when player not found")
        void updatePlayer_WhenNotFound_ShouldReturn404() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Virat Kohli", "Batsman", "Right-Handed", "Right-Arm Medium");
            when(playerService.updatePlayer(eq(PLAYER_ID), any(Player.class)))
                    .thenThrow(new ResourceNotFoundException("Player", "id", PLAYER_ID));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when validation fails")
        void updatePlayer_WithInvalidData_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("", "Batsman", "Right-Handed", "Right-Arm Medium");

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).updatePlayer(anyString(), any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when role is invalid during update")
        void updatePlayer_WithInvalidRole_ShouldReturn400() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Virat Kohli", "InvalidRole", "Right-Handed", "Right-Arm Medium");

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).updatePlayer(anyString(), any(Player.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/players/update/{id} - Partial Update Player")
    class PatchPlayerTests {

        @Test
        @DisplayName("Should partially update player name")
        void patchPlayer_WithOnlyName_ShouldReturn200() throws Exception {
            // Arrange
            String requestBody = """
                    {
                        "name": "King Kohli"
                    }
                    """;
            testPlayer.setName("King Kohli");
            when(playerService.patchPlayer(eq(PLAYER_ID), any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player updated successfully"))
                    .andExpect(jsonPath("$.data.name").value("King Kohli"));

            verify(playerService, times(1)).patchPlayer(eq(PLAYER_ID), any(Player.class));
        }

        @Test
        @DisplayName("Should partially update player role")
        void patchPlayer_WithOnlyRole_ShouldReturn200() throws Exception {
            // Arrange
            String requestBody = """
                    {
                        "role": "All-Rounder"
                    }
                    """;
            testPlayer.setRole("All-Rounder");
            when(playerService.patchPlayer(eq(PLAYER_ID), any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.role").value("All-Rounder"));
        }

        @Test
        @DisplayName("Should partially update player stats")
        void patchPlayer_WithOnlyStats_ShouldReturn200() throws Exception {
            // Arrange
            String requestBody = """
                    {
                        "stats": {
                            "matchesPlayed": 200,
                            "runsScored": 15000,
                            "wicketsTaken": 60,
                            "catchesTaken": 100
                        }
                    }
                    """;
            Stats newStats = Stats.builder()
                    .matchesPlayed(200)
                    .runsScored(15000)
                    .wicketsTaken(60)
                    .catchesTaken(100)
                    .build();
            testPlayer.setStats(newStats);
            when(playerService.patchPlayer(eq(PLAYER_ID), any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats.matchesPlayed").value(200))
                    .andExpect(jsonPath("$.data.stats.runsScored").value(15000));
        }

        @Test
        @DisplayName("Should partially update multiple fields")
        void patchPlayer_WithMultipleFields_ShouldReturn200() throws Exception {
            // Arrange
            String requestBody = """
                    {
                        "name": "Updated Name",
                        "role": "Bowler",
                        "bowlingStyle": "Right-Arm Fast"
                    }
                    """;
            testPlayer.setName("Updated Name");
            testPlayer.setRole("Bowler");
            testPlayer.setBowlingStyle("Right-Arm Fast");
            when(playerService.patchPlayer(eq(PLAYER_ID), any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Updated Name"))
                    .andExpect(jsonPath("$.data.role").value("Bowler"))
                    .andExpect(jsonPath("$.data.bowlingStyle").value("Right-Arm Fast"));
        }

        @Test
        @DisplayName("Should return 404 when player not found")
        void patchPlayer_WhenNotFound_ShouldReturn404() throws Exception {
            // Arrange
            String requestBody = """
                    {
                        "name": "New Name"
                    }
                    """;
            when(playerService.patchPlayer(eq(PLAYER_ID), any(Player.class)))
                    .thenThrow(new ResourceNotFoundException("Player", "id", PLAYER_ID));

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should partially update teamId")
        void patchPlayer_WithTeamId_ShouldReturn200() throws Exception {
            // Arrange
            String requestBody = String.format("""
                    {
                        "teamId": "%s"
                    }
                    """, TEAM_ID);
            testPlayer.setTeamId(TEAM_ID);
            when(playerService.patchPlayer(eq(PLAYER_ID), any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.teamId").value(TEAM_ID));
        }

        @Test
        @DisplayName("Should handle empty patch request")
        void patchPlayer_WithEmptyBody_ShouldReturn200() throws Exception {
            // Arrange
            String requestBody = "{}";
            when(playerService.patchPlayer(eq(PLAYER_ID), any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(patch(BASE_URL + "/update/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/players/{id} - Delete Player")
    class DeletePlayerTests {

        @Test
        @DisplayName("Should delete player and return 200 OK")
        void deletePlayer_WhenExists_ShouldReturn200() throws Exception {
            // Arrange
            when(playerService.deletePlayer(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player deleted successfully"))
                    .andExpect(jsonPath("$.data.id").value(PLAYER_ID))
                    .andExpect(jsonPath("$.data.name").value("Virat Kohli"));

            verify(playerService, times(1)).deletePlayer(PLAYER_ID);
        }

        @Test
        @DisplayName("Should return deleted player with stats")
        void deletePlayer_ShouldReturnPlayerWithStats() throws Exception {
            // Arrange
            when(playerService.deletePlayer(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats.matchesPlayed").value(150))
                    .andExpect(jsonPath("$.data.stats.runsScored").value(12000));
        }

        @Test
        @DisplayName("Should return 404 when player not found")
        void deletePlayer_WhenNotFound_ShouldReturn404() throws Exception {
            // Arrange
            when(playerService.deletePlayer(PLAYER_ID))
                    .thenThrow(new ResourceNotFoundException("Player", "id", PLAYER_ID));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(playerService, times(1)).deletePlayer(PLAYER_ID);
        }

        @Test
        @DisplayName("Should delete player with teamId")
        void deletePlayer_WithTeamId_ShouldReturn200() throws Exception {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            when(playerService.deletePlayer(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.teamId").value(TEAM_ID));
        }
    }

    @Nested
    @DisplayName("Content Type Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("Should accept application/json content type")
        void request_WithJsonContentType_ShouldSucceed() throws Exception {
            // Arrange
            when(playerService.getAllPlayers()).thenReturn(Arrays.asList(testPlayer));

            // Act & Assert
            mockMvc.perform(get(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should return JSON response")
        void response_ShouldBeJson() throws Exception {
            // Arrange
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("API Response Structure Tests")
    class ApiResponseStructureTests {

        @Test
        @DisplayName("Should have correct response structure for success")
        void response_OnSuccess_ShouldHaveCorrectStructure() throws Exception {
            // Arrange
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(jsonPath("$.success").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.success").isBoolean())
                    .andExpect(jsonPath("$.message").isString());
        }

        @Test
        @DisplayName("Should have correct player data structure")
        void response_PlayerData_ShouldHaveCorrectStructure() throws Exception {
            // Arrange
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.name").exists())
                    .andExpect(jsonPath("$.data.role").exists())
                    .andExpect(jsonPath("$.data.battingStyle").exists())
                    .andExpect(jsonPath("$.data.bowlingStyle").exists())
                    .andExpect(jsonPath("$.data.stats").exists());
        }

        @Test
        @DisplayName("Should have correct stats structure")
        void response_Stats_ShouldHaveCorrectStructure() throws Exception {
            // Arrange
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(jsonPath("$.data.stats.matchesPlayed").exists())
                    .andExpect(jsonPath("$.data.stats.runsScored").exists())
                    .andExpect(jsonPath("$.data.stats.wicketsTaken").exists())
                    .andExpect(jsonPath("$.data.stats.catchesTaken").exists());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle special characters in player name")
        void createPlayer_WithSpecialCharacters_ShouldReturn201() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("M.S. Dhoni Jr.", "Wicket-Keeper",
                    "Right-Handed", "Right-Arm Medium");
            testPlayer.setName("M.S. Dhoni Jr.");
            when(playerService.create(any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("M.S. Dhoni Jr."));
        }

        @Test
        @DisplayName("Should handle player with null stats")
        void getPlayer_WithNullStats_ShouldReturn200() throws Exception {
            // Arrange
            testPlayer.setStats(null);
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats").doesNotExist());
        }

        @Test
        @DisplayName("Should handle player with zero stats")
        void getPlayer_WithZeroStats_ShouldReturn200() throws Exception {
            // Arrange
            Stats zeroStats = Stats.builder()
                    .matchesPlayed(0)
                    .runsScored(0)
                    .wicketsTaken(0)
                    .catchesTaken(0)
                    .build();
            testPlayer.setStats(zeroStats);
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stats.matchesPlayed").value(0))
                    .andExpect(jsonPath("$.data.stats.runsScored").value(0));
        }

        @Test
        @DisplayName("Should handle long player name at max length")
        void createPlayer_WithMaxLengthName_ShouldReturn201() throws Exception {
            // Arrange
            String longName = "A".repeat(100); // Max length is 100
            String requestBody = createPlayerJson(longName, "Batsman", "Right-Handed", "Right-Arm Medium");
            testPlayer.setName(longName);
            when(playerService.create(any(Player.class))).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 400 for name exceeding max length")
        void createPlayer_WithNameExceedingMaxLength_ShouldReturn400() throws Exception {
            // Arrange
            String tooLongName = "A".repeat(101); // Exceeds max length of 100
            String requestBody = createPlayerJson(tooLongName, "Batsman", "Right-Handed", "Right-Arm Medium");

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());

            verify(playerService, never()).create(any(Player.class));
        }
    }

    @Nested
    @DisplayName("Service Interaction Verification Tests")
    class VerificationTests {

        @Test
        @DisplayName("Should call service method exactly once for GET all")
        void getAllPlayers_ShouldCallServiceOnce() throws Exception {
            // Arrange
            when(playerService.getAllPlayers()).thenReturn(new ArrayList<>());

            // Act
            mockMvc.perform(get(BASE_URL));

            // Assert
            verify(playerService, times(1)).getAllPlayers();
            verifyNoMoreInteractions(playerService);
        }

        @Test
        @DisplayName("Should call service method exactly once for GET by ID")
        void getPlayerById_ShouldCallServiceOnce() throws Exception {
            // Arrange
            when(playerService.getPlayerById(PLAYER_ID)).thenReturn(testPlayer);

            // Act
            mockMvc.perform(get(BASE_URL + "/{id}", PLAYER_ID));

            // Assert
            verify(playerService, times(1)).getPlayerById(PLAYER_ID);
            verifyNoMoreInteractions(playerService);
        }

        @Test
        @DisplayName("Should call service method exactly once for POST")
        void createPlayer_ShouldCallServiceOnce() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("Test Player", "Batsman", "Right-Handed", "Right-Arm Medium");
            when(playerService.create(any(Player.class))).thenReturn(testPlayer);

            // Act
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));

            // Assert
            verify(playerService, times(1)).create(any(Player.class));
            verifyNoMoreInteractions(playerService);
        }

        @Test
        @DisplayName("Should call service method exactly once for DELETE")
        void deletePlayer_ShouldCallServiceOnce() throws Exception {
            // Arrange
            when(playerService.deletePlayer(PLAYER_ID)).thenReturn(testPlayer);

            // Act
            mockMvc.perform(delete(BASE_URL + "/{id}", PLAYER_ID));

            // Assert
            verify(playerService, times(1)).deletePlayer(PLAYER_ID);
            verifyNoMoreInteractions(playerService);
        }

        @Test
        @DisplayName("Should not call service when validation fails")
        void createPlayer_WhenValidationFails_ShouldNotCallService() throws Exception {
            // Arrange
            String requestBody = createPlayerJson("", "InvalidRole", "InvalidStyle", "InvalidBowling");

            // Act
            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));

            // Assert
            verifyNoInteractions(playerService);
        }
    }
}
