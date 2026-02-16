package com.example.tournament_data.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Stats;
import com.example.tournament_data.service.PlayerService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PlayerController Unit Tests")
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests

    @Mock
    private PlayerService playerService;

    private Player testPlayer;
    private Player testPlayer2;
    private Stats testStats;

    @BeforeEach
    void setUp() {
        testStats = new Stats();
        testStats.setMatchesPlayed(100);
        testStats.setRunsScored(5000);

        testPlayer = new Player("Virat Kohli", "Batsman", "Right-Handed", "Right-Arm Medium");
        testPlayer.setId("player123");
        testPlayer.setStats(testStats);

        testPlayer2 = new Player("Rohit Sharma", "Batsman", "Right-Handed", "Right-Arm Medium");
        testPlayer2.setId("player456");
    }

    @Nested
    @DisplayName("GET /api/players")
    class GetAllPlayersEndpointTests {

        @Test
        @DisplayName("Should return all players with 200 OK")
        void getAllPlayers_Success_Returns200() throws Exception {
            // Arrange
            List<Player> players = Arrays.asList(testPlayer, testPlayer2);
            when(playerService.getAllPlayers()).thenReturn(players);

            // Act & Assert
            mockMvc.perform(get("/api/players")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print()) // Print request/response for debugging
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Players retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value("player123"))
                    .andExpect(jsonPath("$.data[0].name").value("Virat Kohli"))
                    .andExpect(jsonPath("$.data[1].id").value("player456"))
                    .andExpect(jsonPath("$.data[1].name").value("Rohit Sharma"));

            // Verify service was called
            verify(playerService, times(1)).getAllPlayers();
        }

        @Test
        @DisplayName("Should return empty array when no players exist")
        void getAllPlayers_Empty_ReturnsEmptyArray() throws Exception {
            // Arrange
            when(playerService.getAllPlayers()).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/players"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("Should return player with all fields")
        void getAllPlayers_ReturnsCompletePlayerData() throws Exception {
            // Arrange
            when(playerService.getAllPlayers()).thenReturn(Arrays.asList(testPlayer));

            // Act & Assert
            mockMvc.perform(get("/api/players"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value("player123"))
                    .andExpect(jsonPath("$.data[0].name").value("Virat Kohli"))
                    .andExpect(jsonPath("$.data[0].role").value("Batsman"))
                    .andExpect(jsonPath("$.data[0].battingStyle").value("Right-Handed"))
                    .andExpect(jsonPath("$.data[0].bowlingStyle").value("Right-Arm Medium"))
                    .andExpect(jsonPath("$.data[0].stats.matches").value(100))
                    .andExpect(jsonPath("$.data[0].stats.runs").value(5000));
        }
    }

    @Nested
    @DisplayName("GET /api/players/{id}")
    class GetPlayerByIdEndpointTests {

        @Test
        @DisplayName("Should return player when found")
        void getPlayerById_Found_Returns200() throws Exception {
            // Arrange
            when(playerService.getPlayerById("player123")).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(get("/api/players/player123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("player123"))
                    .andExpect(jsonPath("$.data.name").value("Virat Kohli"));

            verify(playerService, times(1)).getPlayerById("player123");
        }

        @Test
        @DisplayName("Should return 404 when player not found")
        void getPlayerById_NotFound_Returns404() throws Exception {
            // Arrange
            when(playerService.getPlayerById("nonExistentId"))
                    .thenThrow(new ResourceNotFoundException("Player", "id", "nonExistentId"));

            // Act & Assert
            mockMvc.perform(get("/api/players/nonExistentId"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should handle various ID formats")
        void getPlayerById_VariousIdFormats_HandledCorrectly() throws Exception {
            // Arrange
            when(playerService.getPlayerById("64a1b2c3d4e5f6g7h8i9j0k1")).thenReturn(testPlayer);

            // Act & Assert - MongoDB-like ID
            mockMvc.perform(get("/api/players/64a1b2c3d4e5f6g7h8i9j0k1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/players")
    class CreatePlayerEndpointTests {

        @Test
        @DisplayName("Should create player and return 201")
        void createPlayer_ValidData_Returns201() throws Exception {
            // Arrange
            when(playerService.create(any(Player.class))).thenReturn(testPlayer);

            String requestBody = """
                    {
                        "name": "Virat Kohli",
                        "role": "Batsman",
                        "battingStyle": "Right-Handed",
                        "bowlingStyle": "Right-Arm Medium"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player created successfully"))
                    .andExpect(jsonPath("$.data.id").value("player123"))
                    .andExpect(jsonPath("$.data.name").value("Virat Kohli"));

            verify(playerService, times(1)).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void createPlayer_BlankName_Returns400() throws Exception {
            String requestBody = """
                    {
                        "name": "",
                        "role": "Batsman",
                        "battingStyle": "Right-Handed",
                        "bowlingStyle": "Right-Arm Medium"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            // Service should not be called for invalid data
            verify(playerService, never()).create(any(Player.class));
        }

        @Test
        @DisplayName("Should return 400 when name is missing")
        void createPlayer_MissingName_Returns400() throws Exception {
            String requestBody = """
                    {
                        "role": "Batsman",
                        "battingStyle": "Right-Handed"
                    }
                    """;

            mockMvc.perform(post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when role is invalid")
        void createPlayer_InvalidRole_Returns400() throws Exception {
            String requestBody = """
                    {
                        "name": "Virat Kohli",
                        "role": "InvalidRole",
                        "battingStyle": "Right-Handed",
                        "bowlingStyle": "Right-Arm Medium"
                    }
                    """;

            mockMvc.perform(post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when battingStyle is invalid")
        void createPlayer_InvalidBattingStyle_Returns400() throws Exception {
            String requestBody = """
                    {
                        "name": "Virat Kohli",
                        "role": "Batsman",
                        "battingStyle": "InvalidStyle",
                        "bowlingStyle": "Right-Arm Medium"
                    }
                    """;

            mockMvc.perform(post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should create player with all valid fields")
        void createPlayer_AllValidFields_Returns201() throws Exception {
            // Arrange
            when(playerService.create(any(Player.class))).thenReturn(testPlayer);

            String requestBody = """
                    {
                        "name": "Virat Kohli",
                        "role": "Batsman",
                        "battingStyle": "Right-Handed",
                        "bowlingStyle": "Right-Arm Medium",
                        "teamId": null,
                        "stats": {
                            "matches": 100,
                            "runs": 5000,
                            "wickets": 10
                        }
                    }
                    """;

            mockMvc.perform(post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 400 when invalid teamId provided")
        void createPlayer_InvalidTeamId_Returns400() throws Exception {
            // Arrange
            when(playerService.create(any(Player.class)))
                    .thenThrow(new InvalidRequestException("teamId", "Team not found"));

            String requestBody = """
                    {
                        "name": "Virat Kohli",
                        "role": "Batsman",
                        "battingStyle": "Right-Handed",
                        "bowlingStyle": "Right-Arm Medium",
                        "teamId": "invalidTeamId"
                    }
                    """;

            mockMvc.perform(post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("DELETE /api/players/{id}")
    class DeletePlayerEndpointTests {

        @Test
        @DisplayName("Should delete player and return 200")
        void deletePlayer_Success_Returns200() throws Exception {
            // Arrange
            when(playerService.deletePlayer("player123")).thenReturn(testPlayer);

            // Act & Assert
            mockMvc.perform(delete("/api/players/player123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player deleted successfully"))
                    .andExpect(jsonPath("$.data.id").value("player123"));

            verify(playerService, times(1)).deletePlayer("player123");
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent player")
        void deletePlayer_NotFound_Returns404() throws Exception {
            // Arrange
            when(playerService.deletePlayer("nonExistentId"))
                    .thenThrow(new ResourceNotFoundException("Player", "id", "nonExistentId"));

            // Act & Assert
            mockMvc.perform(delete("/api/players/nonExistentId"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("PUT /api/players/update/{id}")
    class UpdatePlayerEndpointTests {

        @Test
        @DisplayName("Should update player and return 200")
        void updatePlayer_ValidData_Returns200() throws Exception {
            // Arrange
            Player updatedPlayer = new Player("King Kohli", "All-Rounder", "Right-Handed", "Right-Arm Fast");
            updatedPlayer.setId("player123");

            when(playerService.updatePlayer(anyString(), any(Player.class))).thenReturn(updatedPlayer);

            String requestBody = """
                    {
                        "name": "King Kohli",
                        "role": "All-Rounder",
                        "battingStyle": "Right-Handed",
                        "bowlingStyle": "Right-Arm Fast"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(put("/api/players/update/player123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player updated successfully"))
                    .andExpect(jsonPath("$.data.name").value("King Kohli"))
                    .andExpect(jsonPath("$.data.role").value("All-Rounder"));

            verify(playerService, times(1)).updatePlayer(anyString(), any(Player.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent player")
        void updatePlayer_NotFound_Returns404() throws Exception {
            // Arrange
            when(playerService.updatePlayer(anyString(), any(Player.class)))
                    .thenThrow(new ResourceNotFoundException("Player", "id", "nonExistentId"));

            String requestBody = """
                    {
                        "name": "King Kohli",
                        "role": "Batsman",
                        "battingStyle": "Right-Handed",
                        "bowlingStyle": "Right-Arm Medium"
                    }
                    """;

            mockMvc.perform(put("/api/players/update/nonExistentId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when update data is invalid")
        void updatePlayer_InvalidData_Returns400() throws Exception {
            String requestBody = """
                    {
                        "name": "",
                        "role": "InvalidRole",
                        "battingStyle": "Right-Handed"
                    }
                    """;

            mockMvc.perform(put("/api/players/update/player123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/players/update/{id}")
    class PatchPlayerEndpointTests {

        @Test
        @DisplayName("Should partially update player and return 200")
        void patchPlayer_ValidData_Returns200() throws Exception {
            // Arrange
            Player patchedPlayer = new Player("King Kohli", "Batsman", "Right-Handed", "Right-Arm Medium");
            patchedPlayer.setId("player123");

            when(playerService.patchPlayer(anyString(), any(Player.class))).thenReturn(patchedPlayer);

            String requestBody = """
                    {
                        "name": "King Kohli"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/players/update/player123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Player updated successfully"))
                    .andExpect(jsonPath("$.data.name").value("King Kohli"));

            verify(playerService, times(1)).patchPlayer(anyString(), any(Player.class));
        }

        @Test
        @DisplayName("Should return 404 when patching non-existent player")
        void patchPlayer_NotFound_Returns404() throws Exception {
            // Arrange
            when(playerService.patchPlayer(anyString(), any(Player.class)))
                    .thenThrow(new ResourceNotFoundException("Player", "id", "nonExistentId"));

            String requestBody = """
                    {
                        "name": "King Kohli"
                    }
                    """;

            mockMvc.perform(patch("/api/players/update/nonExistentId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should update only role when only role provided")
        void patchPlayer_OnlyRole_Returns200() throws Exception {
            // Arrange
            Player patchedPlayer = new Player("Virat Kohli", "All-Rounder", "Right-Handed", "Right-Arm Medium");
            patchedPlayer.setId("player123");

            when(playerService.patchPlayer(anyString(), any(Player.class))).thenReturn(patchedPlayer);

            String requestBody = """
                    {
                        "role": "All-Rounder"
                    }
                    """;

            mockMvc.perform(patch("/api/players/update/player123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.role").value("All-Rounder"));
        }

        @Test
        @DisplayName("Should handle empty JSON body")
        void patchPlayer_EmptyBody_Returns200() throws Exception {
            // Arrange
            when(playerService.patchPlayer(anyString(), any(Player.class))).thenReturn(testPlayer);

            mockMvc.perform(patch("/api/players/update/player123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk());
        }
    }
}
