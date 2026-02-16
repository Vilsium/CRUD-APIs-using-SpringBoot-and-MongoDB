package com.example.tournament_data.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Stats;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.repository.PlayerRepository;
import com.example.tournament_data.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService Unit Tests")
public class PlayerServiceTest {

    // mocks for accessing a fake database
    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    // injecting the mocks to service
    @InjectMocks
    private PlayerService playerService;

    // making reusable test objects
    private Player testPlayer;
    private Player testPlayer2;
    private Team testTeam;
    private Stats testStats;

    @BeforeEach // hr test se phle run hoga, will use it to reset the test data
    void setUp() {
        testStats = new Stats();
        testStats.setMatchesPlayed(100);
        testStats.setRunsScored(5000);
        testStats.setWicketsTaken(10);

        testPlayer = new Player("Virat Kohli", "Batsman", "Right-Handed", "Right-Arm Medium");
        testPlayer.setId("player123");
        testPlayer.setTeamId(null);
        testPlayer.setStats(testStats);

        testPlayer2 = new Player("Rohit Sharma", "Batsman", "Right-Handed", "Right-Arm Medium");
        testPlayer2.setId("player456");
        testPlayer2.setTeamId(null);

        testTeam = new Team();
        testTeam.setId("team123");
        testTeam.setTeamName("Mumbai Indians");
        testTeam.setPlayerIds(new ArrayList<>());
        testTeam.setCaptainId(null);
    }

    @Nested
    @DisplayName("Creation method tests")
    class CreatePlayerTests {
        @Test
        @DisplayName("Should create player successfully without team")
        void create_WithoutTeam_Success() {
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            Player result = playerService.create(testPlayer);

            // Assert - Verify results
            assertNotNull(result, "Result should not be null");
            assertEquals("player123", result.getId());
            assertEquals("Virat Kohli", result.getName());
            assertEquals("Batsman", result.getRole());

            // Verify - Check that mocks were called correctly
            verify(playerRepository, times(1)).save(testPlayer);

            // Team repository should NOT be called since no teamId
            verify(teamRepository, never()).findById(anyString());
            verify(teamRepository, never()).save(any(Team.class));
        }

        @Test
        @DisplayName("Should create player and add to existing team")
        void create_WithValidTeam_AddsPlayerToTeam() {
            // Arrange
            testPlayer.setTeamId("team123");

            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
            when(teamRepository.findById("team123")).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result);
            assertEquals("team123", result.getTeamId());
            assertTrue(testTeam.getPlayerIds().contains("player123"),
                    "Team should contain the new player's ID");

            // Verify
            verify(playerRepository, times(1)).save(testPlayer);
            verify(teamRepository, times(1)).findById("team123");
            verify(teamRepository, times(1)).save(testTeam);
        }

        @Test
        @DisplayName("Should throw InvalidRequestException when team not found")
        void create_WithInvalidTeam_ThrowsException() {
            // Arrange
            testPlayer.setTeamId("bhbuh"); // koi invalid id daalkr check krna hai, isiliye fail ho rha hai

            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
            when(teamRepository.findById("invalidTeamId")).thenReturn(Optional.empty());

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> playerService.create(testPlayer),
                    "Should throw InvalidRequestException when team not found");

            // Verify exception message
            assertTrue(exception.getMessage().contains("Team not found"));

            // Verify
            verify(playerRepository, times(1)).save(testPlayer);
            verify(teamRepository, times(1)).findById("invalidTeamId");
            verify(teamRepository, never()).save(any(Team.class));
        }

        @Test
        @DisplayName("Should handle empty teamId string")
        void create_WithEmptyTeamId_TreatsAsNoTeam() {
            // Arrange
            testPlayer.setTeamId(""); // Empty string

            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result);

            // Verify - Team repository should NOT be called
            verify(teamRepository, never()).findById(anyString());
        }
    }

    @Nested
    @DisplayName("getAllPlayers() method tests")
    class GetAllPlayersTests {

        @Test
        @DisplayName("Should return all players")
        void getAllPlayers_ReturnsAllPlayers() {
            // Arrange
            List<Player> players = Arrays.asList(testPlayer, testPlayer2);
            when(playerRepository.findAll()).thenReturn(players);

            // Act
            List<Player> result = playerService.getAllPlayers();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Virat Kohli", result.get(0).getName());
            assertEquals("Rohit Sharma", result.get(1).getName());

            // Verify
            verify(playerRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no players exist")
        void getAllPlayers_NoPlayers_ReturnsEmptyList() {
            // Arrange
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            // Act
            List<Player> result = playerService.getAllPlayers();

            // Assert
            assertNotNull(result, "Result should not be null, should be empty list");
            assertTrue(result.isEmpty(), "Result should be empty");

            // Verify
            verify(playerRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return single player when only one exists")
        void getAllPlayers_SinglePlayer_ReturnsList() {
            // Arrange
            when(playerRepository.findAll()).thenReturn(Arrays.asList(testPlayer));

            // Act
            List<Player> result = playerService.getAllPlayers();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Virat Kohli", result.get(0).getName());
        }
    }

    @Nested
    @DisplayName("getPlayerById() method tests")
    class GetPlayerByIdTests {

        @Test
        @DisplayName("Should return player when found")
        void getPlayerById_PlayerExists_ReturnsPlayer() {
            // Arrange
            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));

            // Act
            Player result = playerService.getPlayerById("player123");

            // Assert
            assertNotNull(result);
            assertEquals("player123", result.getId());
            assertEquals("Virat Kohli", result.getName());
            assertEquals("Batsman", result.getRole());
            assertEquals("Right-Handed", result.getBattingStyle());

            // Verify
            verify(playerRepository, times(1)).findById("player123");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when player not found")
        void getPlayerById_PlayerNotFound_ThrowsException() {
            // Arrange
            when(playerRepository.findById("nonExistentId")).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> playerService.getPlayerById("nonExistentId"),
                    "Should throw ResourceNotFoundException");

            // Verify exception details
            assertTrue(exception.getMessage().contains("Player"));
            assertTrue(exception.getMessage().contains("nonExistentId"));

            // Verify
            verify(playerRepository, times(1)).findById("nonExistentId");
        }
    }

    @Nested
    @DisplayName("updatePlayer() method tests")
    class UpdatePlayerTests {

        @Test
        @DisplayName("Should update all player fields successfully")
        void updatePlayer_ValidData_UpdatesAllFields() {
            // Arrange
            Player updateDetails = new Player("King Kohli", "All-Rounder", "Right-Handed", "Right-Arm Fast");
            updateDetails.setTeamId("newTeam123");
            Stats newStats = new Stats();
            newStats.setMatchesPlayed(200);
            updateDetails.setStats(newStats);

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.updatePlayer("player123", updateDetails);

            // Assert
            assertEquals("King Kohli", result.getName());
            assertEquals("All-Rounder", result.getRole());
            assertEquals("Right-Handed", result.getBattingStyle());
            assertEquals("Right-Arm Fast", result.getBowlingStyle());
            assertEquals("newTeam123", result.getTeamId());
            assertEquals(200, result.getStats().getMatchesPlayed());

            // Verify
            verify(playerRepository, times(1)).findById("player123");
            verify(playerRepository, times(1)).save(any(Player.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent player")
        void updatePlayer_PlayerNotFound_ThrowsException() {
            // Arrange
            when(playerRepository.findById("nonExistentId")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> playerService.updatePlayer("nonExistentId", testPlayer));

            // Verify - save should never be called
            verify(playerRepository, never()).save(any(Player.class));
        }

        @Test
        @DisplayName("Should update player even with null fields in update details")
        void updatePlayer_NullFieldsInUpdate_SetsToNull() {
            // Arrange
            Player updateDetails = new Player();
            updateDetails.setName("New Name");
            // All other fields are null

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.updatePlayer("player123", updateDetails);

            // Assert - Full update means null fields become null
            assertEquals("New Name", result.getName());
            assertNull(result.getRole());
            assertNull(result.getBattingStyle());
            assertNull(result.getBowlingStyle());
        }
    }

    @Nested
    @DisplayName("patchPlayer() method tests")
    class PatchPlayerTests {

        @Test
        @DisplayName("Should update only name when only name provided")
        void patchPlayer_OnlyName_UpdatesOnlyName() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setName("King Kohli");
            // All other fields are null

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer("player123", patchDetails);

            // Assert
            assertEquals("King Kohli", result.getName());
            assertEquals("Batsman", result.getRole()); // Unchanged
            assertEquals("Right-Handed", result.getBattingStyle()); // Unchanged
            assertEquals("Right-Arm Medium", result.getBowlingStyle()); // Unchanged
        }

        @Test
        @DisplayName("Should update only role when only role provided")
        void patchPlayer_OnlyRole_UpdatesOnlyRole() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setRole("All-Rounder");

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer("player123", patchDetails);

            // Assert
            assertEquals("Virat Kohli", result.getName()); // Unchanged
            assertEquals("All-Rounder", result.getRole()); // Updated
        }

        @Test
        @DisplayName("Should update multiple fields when multiple provided")
        void patchPlayer_MultipleFields_UpdatesAllProvided() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setName("King Kohli");
            patchDetails.setRole("All-Rounder");
            patchDetails.setBowlingStyle("Right-Arm Fast");

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer("player123", patchDetails);

            // Assert
            assertEquals("King Kohli", result.getName()); // Updated
            assertEquals("All-Rounder", result.getRole()); // Updated
            assertEquals("Right-Handed", result.getBattingStyle()); // Unchanged
            assertEquals("Right-Arm Fast", result.getBowlingStyle()); // Updated
        }

        @Test
        @DisplayName("Should update teamId when provided")
        void patchPlayer_TeamId_UpdatesTeamId() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setTeamId("newTeam456");

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer("player123", patchDetails);

            // Assert
            assertEquals("newTeam456", result.getTeamId());
            assertEquals("Virat Kohli", result.getName()); // Unchanged
        }

        @Test
        @DisplayName("Should update stats when provided")
        void patchPlayer_Stats_UpdatesStats() {
            // Arrange
            Player patchDetails = new Player();
            Stats newStats = new Stats();
            newStats.setMatchesPlayed(200);
            newStats.setRunsScored(10000);
            patchDetails.setStats(newStats);

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer("player123", patchDetails);

            // Assert
            assertEquals(200, result.getStats().getMatchesPlayed());
            assertEquals(10000, result.getStats().getRunsScored());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when patching non-existent player")
        void patchPlayer_PlayerNotFound_ThrowsException() {
            // Arrange
            when(playerRepository.findById("nonExistentId")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> playerService.patchPlayer("nonExistentId", testPlayer));

            verify(playerRepository, never()).save(any(Player.class));
        }

        @Test
        @DisplayName("Should not change anything when empty patch provided")
        void patchPlayer_EmptyPatch_NoChanges() {
            // Arrange
            Player patchDetails = new Player(); // All fields null

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer("player123", patchDetails);

            // Assert - All original values preserved
            assertEquals("Virat Kohli", result.getName());
            assertEquals("Batsman", result.getRole());
            assertEquals("Right-Handed", result.getBattingStyle());
            assertEquals("Right-Arm Medium", result.getBowlingStyle());
        }
    }

    @Nested
    @DisplayName("deletePlayer() method tests")
    class DeletePlayerTests {
        @Test
        @DisplayName("Should delete player without team successfully")
        void deletePlayer_NoTeam_DeletesSuccessfully() {
            // Arrange
            testPlayer.setTeamId(null);

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            doNothing().when(playerRepository).deleteById("player123");

            // Act
            Player result = playerService.deletePlayer("player123");

            // Assert
            assertNotNull(result);
            assertEquals("player123", result.getId());
            assertEquals("Virat Kohli", result.getName());

            // Verify
            verify(playerRepository, times(1)).findById("player123");
            verify(playerRepository, times(1)).deleteById("player123");
            verify(teamRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Should delete player and remove from team")
        void deletePlayer_WithTeam_RemovesFromTeam() {
            // Arrange
            testPlayer.setTeamId("team123");
            testTeam.getPlayerIds().add("player123");

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById("team123")).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);
            doNothing().when(playerRepository).deleteById("player123");

            // Act
            Player result = playerService.deletePlayer("player123");

            // Assert
            assertNotNull(result);
            assertFalse(testTeam.getPlayerIds().contains("player123"),
                    "Player should be removed from team");

            // Verify
            verify(teamRepository, times(1)).findById("team123");
            verify(teamRepository, times(1)).save(testTeam);
            verify(playerRepository, times(1)).deleteById("player123");
        }

        @Test
        @DisplayName("Should clear captainId when deleting team captain")
        void deletePlayer_IsCaptain_ClearsCaptainId() {
            // Arrange
            testPlayer.setTeamId("team123");
            testTeam.getPlayerIds().add("player123");
            testTeam.setCaptainId("player123"); // Player is captain

            when(playerRepository.findById("player123")).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById("team123")).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);
            doNothing().when(playerRepository).deleteById("player123");

            // Act
            playerService.deletePlayer("player123");

            // Assert
            assertNull(testTeam.getCaptainId(), "Captain should be cleared");
            assertFalse(testTeam.getPlayerIds().contains("player123"));

            // Verify
            verify(teamRepository, times(1)).save(testTeam);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent player")
        void deletePlayer_PlayerNotFound_ThrowsException() {
            // Arrange
            when(playerRepository.findById("nonExistentId")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> playerService.deletePlayer("nonExistentId"));

            // Verify - delete should never be called
            verify(playerRepository, never()).deleteById(anyString());
        }
    }
}
