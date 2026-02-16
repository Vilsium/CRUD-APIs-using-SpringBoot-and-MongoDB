package com.example.tournament_data.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer;
    private Team testTeam;
    private Stats testStats;

    private static final String PLAYER_ID = "64a1b2c3d4e5f6g7h8i9j0k1";
    private static final String TEAM_ID = "64a1b2c3d4e5f6g7h8i9j0k2";
    private static final String CAPTAIN_ID = "64a1b2c3d4e5f6g7h8i9j0k3";

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

        // Initialize test team
        testTeam = new Team();
        testTeam.setId(TEAM_ID);
        testTeam.setTeamName("Mumbai Indians");
        testTeam.setHomeGround("Wankhede Stadium");
        testTeam.setCoach("Mahela Jayawardene");
        testTeam.setCaptainId(CAPTAIN_ID);
        testTeam.setPlayerIds(new ArrayList<>(Arrays.asList("existingPlayer1", "existingPlayer2")));
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

    private Stats createStats(int matches, int runs, int wickets, int catches) {
        return Stats.builder()
                .matchesPlayed(matches)
                .runsScored(runs)
                .wicketsTaken(wickets)
                .catchesTaken(catches)
                .build();
    }

    @Nested
    @DisplayName("Create Player Tests")
    class CreatePlayerTests {

        @Test
        @DisplayName("Should create player without team successfully")
        void create_WithoutTeam_ShouldReturnSavedPlayer() {
            // Arrange
            testPlayer.setTeamId(null);
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result);
            assertEquals(PLAYER_ID, result.getId());
            assertEquals("Virat Kohli", result.getName());
            assertEquals("Batsman", result.getRole());
            assertEquals("Right-Handed", result.getBattingStyle());
            assertNull(result.getTeamId());
            verify(playerRepository, times(1)).save(testPlayer);
            verify(teamRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Should create player with empty teamId successfully")
        void create_WithEmptyTeamId_ShouldReturnSavedPlayer() {
            // Arrange
            testPlayer.setTeamId("");
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result);
            verify(teamRepository, never()).findById(anyString());
            verify(playerRepository, times(1)).save(testPlayer);
        }

        @Test
        @DisplayName("Should create player with valid team and add to team's playerIds")
        void create_WithValidTeam_ShouldAddPlayerToTeam() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            Player savedPlayer = new Player();
            savedPlayer.setId(PLAYER_ID);
            savedPlayer.setName("Virat Kohli");
            savedPlayer.setTeamId(TEAM_ID);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById("existingPlayer1")).thenReturn(Optional.of(
                    createPlayer("existingPlayer1", "Rohit Sharma", "Batsman", "Right-Handed", "Right-Arm Medium")));
            when(playerRepository.findById("existingPlayer2")).thenReturn(Optional.of(
                    createPlayer("existingPlayer2", "Jasprit Bumrah", "Bowler", "Right-Handed", "Right-Arm Fast")));
            when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result);
            assertEquals(PLAYER_ID, result.getId());
            verify(teamRepository, times(1)).findById(TEAM_ID);
            verify(teamRepository, times(1)).save(argThat(team -> team.getPlayerIds().contains(PLAYER_ID)));
        }

        @Test
        @DisplayName("Should throw exception when team not found")
        void create_WithInvalidTeamId_ShouldThrowException() {
            // Arrange
            testPlayer.setTeamId("invalidTeamId");
            when(teamRepository.findById("invalidTeamId")).thenReturn(Optional.empty());

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> playerService.create(testPlayer));

            assertTrue(exception.getMessage().contains("Team not found") ||
                    exception.getMessage().contains("invalidTeamId"));
            verify(playerRepository, never()).save(any(Player.class));
        }

        @Test
        @DisplayName("Should throw exception when player with same name exists in team")
        void create_WithDuplicateNameInTeam_ShouldThrowException() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            testPlayer.setName("Rohit Sharma"); // Same name as existing player

            Player existingPlayer = createPlayer("existingPlayer1", "Rohit Sharma", "Batsman", "Right-Handed",
                    "Right-Arm Medium");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById("existingPlayer1")).thenReturn(Optional.of(existingPlayer));

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> playerService.create(testPlayer));

            assertTrue(exception.getMessage().contains("already exists"));
            verify(playerRepository, never()).save(any(Player.class));
        }

        @Test
        @DisplayName("Should allow player with same name in different case (case-insensitive check)")
        void create_WithSameNameDifferentCase_ShouldThrowException() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            testPlayer.setName("rohit sharma"); // Same name but different case

            Player existingPlayer = createPlayer("existingPlayer1", "Rohit Sharma", "Batsman", "Right-Handed",
                    "Right-Arm Medium");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById("existingPlayer1")).thenReturn(Optional.of(existingPlayer));

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> playerService.create(testPlayer));

            assertTrue(exception.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("Should create player with stats")
        void create_WithStats_ShouldReturnPlayerWithStats() {
            // Arrange
            testPlayer.setTeamId(null);
            testPlayer.setStats(testStats);
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result.getStats());
            assertEquals(150, result.getStats().getMatchesPlayed());
            assertEquals(12000, result.getStats().getRunsScored());
            assertEquals(45, result.getStats().getWicketsTaken());
            assertEquals(80, result.getStats().getCatchesTaken());
        }

        @Test
        @DisplayName("Should create player when existing player in team not found")
        void create_WhenExistingPlayerNotFound_ShouldContinue() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            testPlayer.setName("New Player");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById("existingPlayer1")).thenReturn(Optional.empty());
            when(playerRepository.findById("existingPlayer2")).thenReturn(Optional.empty());
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result);
            verify(playerRepository, times(1)).save(testPlayer);
        }

        @Test
        @DisplayName("Should create player with all roles")
        void create_WithDifferentRoles_ShouldCreateSuccessfully() {
            // Arrange
            testPlayer.setTeamId(null);
            String[] roles = { "Batsman", "Bowler", "All-Rounder", "Wicket-Keeper" };

            for (String role : roles) {
                testPlayer.setRole(role);
                when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

                // Act
                Player result = playerService.create(testPlayer);

                // Assert
                assertNotNull(result);
                assertEquals(role, result.getRole());
            }
        }
    }

    @Nested
    @DisplayName("Get All Players Tests")
    class GetAllPlayersTests {

        @Test
        @DisplayName("Should return all players")
        void getAllPlayers_ShouldReturnAllPlayers() {
            // Arrange
            Player player2 = createPlayer("player2", "MS Dhoni", "Wicket-Keeper", "Right-Handed", "Right-Arm Medium");
            player2.setStats(createStats(350, 10000, 0, 150));

            List<Player> players = Arrays.asList(testPlayer, player2);
            when(playerRepository.findAll()).thenReturn(players);

            // Act
            List<Player> result = playerService.getAllPlayers();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Virat Kohli", result.get(0).getName());
            assertEquals("MS Dhoni", result.get(1).getName());
            verify(playerRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no players exist")
        void getAllPlayers_WhenNoPlayers_ShouldReturnEmptyList() {
            // Arrange
            when(playerRepository.findAll()).thenReturn(new ArrayList<>());

            // Act
            List<Player> result = playerService.getAllPlayers();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(playerRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return players with their stats")
        void getAllPlayers_ShouldReturnPlayersWithStats() {
            // Arrange
            when(playerRepository.findAll()).thenReturn(Arrays.asList(testPlayer));

            // Act
            List<Player> result = playerService.getAllPlayers();

            // Assert
            assertEquals(1, result.size());
            assertNotNull(result.get(0).getStats());
            assertEquals(150, result.get(0).getStats().getMatchesPlayed());
        }
    }

    @Nested
    @DisplayName("Get Player By ID Tests")
    class GetPlayerByIdTests {

        @Test
        @DisplayName("Should return player when found")
        void getPlayerById_WhenExists_ShouldReturnPlayer() {
            // Arrange
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));

            // Act
            Player result = playerService.getPlayerById(PLAYER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(PLAYER_ID, result.getId());
            assertEquals("Virat Kohli", result.getName());
            assertEquals("Batsman", result.getRole());
            assertEquals("Right-Handed", result.getBattingStyle());
            assertEquals("Right-Arm Medium", result.getBowlingStyle());
            assertNotNull(result.getStats());
            verify(playerRepository, times(1)).findById(PLAYER_ID);
        }

        @Test
        @DisplayName("Should throw exception when player not found")
        void getPlayerById_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> playerService.getPlayerById(PLAYER_ID));

            assertTrue(exception.getMessage().contains("Player") ||
                    exception.getMessage().contains(PLAYER_ID));
            verify(playerRepository, times(1)).findById(PLAYER_ID);
        }

        @Test
        @DisplayName("Should return player with all stats fields")
        void getPlayerById_ShouldReturnPlayerWithAllStatsFields() {
            // Arrange
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));

            // Act
            Player result = playerService.getPlayerById(PLAYER_ID);

            // Assert
            assertNotNull(result.getStats());
            assertEquals(150, result.getStats().getMatchesPlayed());
            assertEquals(12000, result.getStats().getRunsScored());
            assertEquals(45, result.getStats().getWicketsTaken());
            assertEquals(80, result.getStats().getCatchesTaken());
        }
    }

    @Nested
    @DisplayName("Update Player Tests")
    class UpdatePlayerTests {

        @Test
        @DisplayName("Should update player successfully with all fields")
        void updatePlayer_WithAllFields_ShouldReturnUpdatedPlayer() {
            // Arrange
            Player updatedDetails = new Player();
            updatedDetails.setName("Virat Kohli Updated");
            updatedDetails.setRole("All-Rounder");
            updatedDetails.setBattingStyle("Left-Handed");
            updatedDetails.setBowlingStyle("Right-Arm Spin");
            updatedDetails.setTeamId(TEAM_ID);
            updatedDetails.setStats(createStats(200, 15000, 50, 100));

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.updatePlayer(PLAYER_ID, updatedDetails);

            // Assert
            assertNotNull(result);
            assertEquals("Virat Kohli Updated", result.getName());
            assertEquals("All-Rounder", result.getRole());
            assertEquals("Left-Handed", result.getBattingStyle());
            assertEquals("Right-Arm Spin", result.getBowlingStyle());
            assertEquals(TEAM_ID, result.getTeamId());
            assertEquals(200, result.getStats().getMatchesPlayed());
            assertEquals(15000, result.getStats().getRunsScored());
            verify(playerRepository, times(1)).save(any(Player.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent player")
        void updatePlayer_WhenPlayerNotExists_ShouldThrowException() {
            // Arrange
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> playerService.updatePlayer(PLAYER_ID, testPlayer));

            assertTrue(exception.getMessage().contains("Player"));
            verify(playerRepository, never()).save(any(Player.class));
        }

        @Test
        @DisplayName("Should update player role from Batsman to Bowler")
        void updatePlayer_ChangeRole_ShouldUpdateSuccessfully() {
            // Arrange
            Player updatedDetails = new Player();
            updatedDetails.setName("Jasprit Bumrah");
            updatedDetails.setRole("Bowler");
            updatedDetails.setBattingStyle("Right-Handed");
            updatedDetails.setBowlingStyle("Right-Arm Fast");
            updatedDetails.setTeamId(null);
            updatedDetails.setStats(createStats(100, 50, 150, 20));

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.updatePlayer(PLAYER_ID, updatedDetails);

            // Assert
            assertEquals("Bowler", result.getRole());
            assertEquals("Right-Arm Fast", result.getBowlingStyle());
            assertEquals(150, result.getStats().getWicketsTaken());
        }

        @Test
        @DisplayName("Should update player stats")
        void updatePlayer_UpdateStats_ShouldUpdateAllStatsFields() {
            // Arrange
            Stats newStats = createStats(300, 20000, 100, 150);
            Player updatedDetails = new Player();
            updatedDetails.setName(testPlayer.getName());
            updatedDetails.setRole(testPlayer.getRole());
            updatedDetails.setBattingStyle(testPlayer.getBattingStyle());
            updatedDetails.setBowlingStyle(testPlayer.getBowlingStyle());
            updatedDetails.setTeamId(testPlayer.getTeamId());
            updatedDetails.setStats(newStats);

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.updatePlayer(PLAYER_ID, updatedDetails);

            // Assert
            assertNotNull(result.getStats());
            assertEquals(300, result.getStats().getMatchesPlayed());
            assertEquals(20000, result.getStats().getRunsScored());
            assertEquals(100, result.getStats().getWicketsTaken());
            assertEquals(150, result.getStats().getCatchesTaken());
        }

        @Test
        @DisplayName("Should clear stats when updated with null")
        void updatePlayer_WithNullStats_ShouldClearStats() {
            // Arrange
            Player updatedDetails = new Player();
            updatedDetails.setName(testPlayer.getName());
            updatedDetails.setRole(testPlayer.getRole());
            updatedDetails.setBattingStyle(testPlayer.getBattingStyle());
            updatedDetails.setBowlingStyle(testPlayer.getBowlingStyle());
            updatedDetails.setTeamId(testPlayer.getTeamId());
            updatedDetails.setStats(null);

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.updatePlayer(PLAYER_ID, updatedDetails);

            // Assert
            assertNull(result.getStats());
        }
    }

    @Nested
    @DisplayName("Patch Player Tests")
    class PatchPlayerTests {

        @Test
        @DisplayName("Should patch only name field")
        void patchPlayer_WithOnlyName_ShouldUpdateOnlyName() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setName("King Kohli");

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            assertEquals("King Kohli", result.getName());
            assertEquals("Batsman", result.getRole()); // Unchanged
            assertEquals("Right-Handed", result.getBattingStyle()); // Unchanged
            assertEquals("Right-Arm Medium", result.getBowlingStyle()); // Unchanged
        }

        @Test
        @DisplayName("Should patch only role field")
        void patchPlayer_WithOnlyRole_ShouldUpdateOnlyRole() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setRole("All-Rounder");

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            assertEquals("Virat Kohli", result.getName()); // Unchanged
            assertEquals("All-Rounder", result.getRole());
        }

        @Test
        @DisplayName("Should patch only batting style")
        void patchPlayer_WithOnlyBattingStyle_ShouldUpdateOnlyBattingStyle() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setBattingStyle("Left-Handed");

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            assertEquals("Left-Handed", result.getBattingStyle());
            assertEquals("Right-Arm Medium", result.getBowlingStyle()); // Unchanged
        }

        @Test
        @DisplayName("Should patch only bowling style")
        void patchPlayer_WithOnlyBowlingStyle_ShouldUpdateOnlyBowlingStyle() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setBowlingStyle("Left-Arm Spin");

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            assertEquals("Right-Handed", result.getBattingStyle()); // Unchanged
            assertEquals("Left-Arm Spin", result.getBowlingStyle());
        }

        @Test
        @DisplayName("Should patch only teamId")
        void patchPlayer_WithOnlyTeamId_ShouldUpdateOnlyTeamId() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setTeamId(TEAM_ID);

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            assertEquals(TEAM_ID, result.getTeamId());
            assertEquals("Virat Kohli", result.getName()); // Unchanged
        }

        @Test
        @DisplayName("Should patch only stats")
        void patchPlayer_WithOnlyStats_ShouldUpdateOnlyStats() {
            // Arrange
            Stats newStats = createStats(500, 25000, 200, 250);
            Player patchDetails = new Player();
            patchDetails.setStats(newStats);

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            assertNotNull(result.getStats());
            assertEquals(500, result.getStats().getMatchesPlayed());
            assertEquals(25000, result.getStats().getRunsScored());
            assertEquals(200, result.getStats().getWicketsTaken());
            assertEquals(250, result.getStats().getCatchesTaken());
            assertEquals("Virat Kohli", result.getName()); // Unchanged
        }

        @Test
        @DisplayName("Should patch multiple fields at once")
        void patchPlayer_WithMultipleFields_ShouldUpdateAllProvidedFields() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setName("Updated Name");
            patchDetails.setRole("Bowler");
            patchDetails.setBowlingStyle("Right-Arm Fast");

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            assertEquals("Updated Name", result.getName());
            assertEquals("Bowler", result.getRole());
            assertEquals("Right-Arm Fast", result.getBowlingStyle());
            assertEquals("Right-Handed", result.getBattingStyle()); // Unchanged
        }

        @Test
        @DisplayName("Should throw exception when player not found")
        void patchPlayer_WhenPlayerNotExists_ShouldThrowException() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setName("New Name");

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> playerService.patchPlayer(PLAYER_ID, patchDetails));

            assertTrue(exception.getMessage().contains("Player"));
            verify(playerRepository, never()).save(any(Player.class));
        }

        @Test
        @DisplayName("Should handle empty patch - no changes")
        void patchPlayer_WithNoFields_ShouldReturnUnchangedPlayer() {
            // Arrange
            Player emptyPatch = new Player();

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, emptyPatch);

            // Assert
            assertEquals("Virat Kohli", result.getName());
            assertEquals("Batsman", result.getRole());
            assertEquals("Right-Handed", result.getBattingStyle());
            assertEquals("Right-Arm Medium", result.getBowlingStyle());
        }
    }

    @Nested
    @DisplayName("Delete Player Tests")
    class DeletePlayerTests {

        @Test
        @DisplayName("Should delete player without team")
        void deletePlayer_WithoutTeam_ShouldDeleteSuccessfully() {
            // Arrange
            testPlayer.setTeamId(null);
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            doNothing().when(playerRepository).deleteById(PLAYER_ID);

            // Act
            Player result = playerService.deletePlayer(PLAYER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(PLAYER_ID, result.getId());
            assertEquals("Virat Kohli", result.getName());
            verify(playerRepository, times(1)).deleteById(PLAYER_ID);
            verify(teamRepository, never()).findById(anyString());
        }

        @Test
        @DisplayName("Should delete player with empty teamId")
        void deletePlayer_WithEmptyTeamId_ShouldDeleteSuccessfully() {
            // Arrange
            testPlayer.setTeamId("");
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            doNothing().when(playerRepository).deleteById(PLAYER_ID);

            // Act
            Player result = playerService.deletePlayer(PLAYER_ID);

            // Assert
            assertNotNull(result);
            verify(teamRepository, never()).findById(anyString());
            verify(playerRepository, times(1)).deleteById(PLAYER_ID);
        }

        @Test
        @DisplayName("Should delete player and remove from team's playerIds")
        void deletePlayer_WithTeam_ShouldRemoveFromTeamPlayerIds() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            testTeam.setPlayerIds(new ArrayList<>(Arrays.asList(PLAYER_ID, "otherPlayer")));
            testTeam.setCaptainId("otherPlayer");

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);
            doNothing().when(playerRepository).deleteById(PLAYER_ID);

            // Act
            Player result = playerService.deletePlayer(PLAYER_ID);

            // Assert
            assertNotNull(result);
            verify(teamRepository, times(1)).save(argThat(team -> !team.getPlayerIds().contains(PLAYER_ID)));
            verify(playerRepository, times(1)).deleteById(PLAYER_ID);
        }

        @Test
        @DisplayName("Should delete captain and clear team's captainId")
        void deletePlayer_WhenCaptain_ShouldClearCaptainId() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            testTeam.setPlayerIds(new ArrayList<>(Arrays.asList(PLAYER_ID, "otherPlayer")));
            testTeam.setCaptainId(PLAYER_ID); // Player is captain

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(playerRepository).deleteById(PLAYER_ID);

            // Act
            Player result = playerService.deletePlayer(PLAYER_ID);

            // Assert
            assertNotNull(result);
            verify(teamRepository, times(1)).save(argThat(team -> team.getCaptainId() == null));
        }

        @Test
        @DisplayName("Should not clear captainId when deleting non-captain player")
        void deletePlayer_WhenNotCaptain_ShouldNotClearCaptainId() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            testTeam.setPlayerIds(new ArrayList<>(Arrays.asList(PLAYER_ID, CAPTAIN_ID)));
            testTeam.setCaptainId(CAPTAIN_ID); // Different player is captain

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(playerRepository).deleteById(PLAYER_ID);

            // Act
            playerService.deletePlayer(PLAYER_ID);

            // Assert
            verify(teamRepository, times(1)).save(argThat(team -> CAPTAIN_ID.equals(team.getCaptainId())));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent player")
        void deletePlayer_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> playerService.deletePlayer(PLAYER_ID));

            assertTrue(exception.getMessage().contains("Player"));
            verify(playerRepository, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("Should handle case when team not found during deletion")
        void deletePlayer_WhenTeamNotFound_ShouldStillDeletePlayer() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());
            doNothing().when(playerRepository).deleteById(PLAYER_ID);

            // Act
            Player result = playerService.deletePlayer(PLAYER_ID);

            // Assert
            assertNotNull(result);
            verify(teamRepository, never()).save(any(Team.class));
            verify(playerRepository, times(1)).deleteById(PLAYER_ID);
        }

        @Test
        @DisplayName("Should return complete player object with stats after deletion")
        void deletePlayer_ShouldReturnCompletePlayerObject() {
            // Arrange
            testPlayer.setTeamId(null);
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            doNothing().when(playerRepository).deleteById(PLAYER_ID);

            // Act
            Player result = playerService.deletePlayer(PLAYER_ID);

            // Assert
            assertEquals(PLAYER_ID, result.getId());
            assertEquals("Virat Kohli", result.getName());
            assertEquals("Batsman", result.getRole());
            assertEquals("Right-Handed", result.getBattingStyle());
            assertEquals("Right-Arm Medium", result.getBowlingStyle());
            assertNotNull(result.getStats());
            assertEquals(150, result.getStats().getMatchesPlayed());
        }
    }

    @Nested
    @DisplayName("Stats-Specific Tests")
    class StatsSpecificTests {

        @Test
        @DisplayName("Should handle player with zero stats")
        void stats_WithZeroValues_ShouldBeHandledCorrectly() {
            // Arrange
            Stats zeroStats = createStats(0, 0, 0, 0);
            testPlayer.setStats(zeroStats);
            testPlayer.setTeamId(null);
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result.getStats());
            assertEquals(0, result.getStats().getMatchesPlayed());
            assertEquals(0, result.getStats().getRunsScored());
            assertEquals(0, result.getStats().getWicketsTaken());
            assertEquals(0, result.getStats().getCatchesTaken());
        }

        @Test
        @DisplayName("Should handle player with high stats values")
        void stats_WithHighValues_ShouldBeHandledCorrectly() {
            // Arrange
            Stats highStats = createStats(500, 50000, 500, 300);
            testPlayer.setStats(highStats);
            testPlayer.setTeamId(null);
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result.getStats());
            assertEquals(500, result.getStats().getMatchesPlayed());
            assertEquals(50000, result.getStats().getRunsScored());
            assertEquals(500, result.getStats().getWicketsTaken());
            assertEquals(300, result.getStats().getCatchesTaken());
        }

        @Test
        @DisplayName("Should handle player with null stats")
        void stats_WithNullStats_ShouldBeHandledCorrectly() {
            // Arrange
            testPlayer.setStats(null);
            testPlayer.setTeamId(null);
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNull(result.getStats());
        }

        @Test
        @DisplayName("Should update individual stats field")
        void patchPlayer_UpdateStatsOnly_ShouldPreserveOtherFields() {
            // Arrange
            Stats updatedStats = Stats.builder()
                    .matchesPlayed(200)
                    .runsScored(15000)
                    .wicketsTaken(60)
                    .catchesTaken(100)
                    .build();

            Player patchDetails = new Player();
            patchDetails.setStats(updatedStats);

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Player result = playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            assertEquals("Virat Kohli", result.getName()); // Unchanged
            assertEquals("Batsman", result.getRole()); // Unchanged
            assertEquals(200, result.getStats().getMatchesPlayed());
            assertEquals(15000, result.getStats().getRunsScored());
            assertEquals(60, result.getStats().getWicketsTaken());
            assertEquals(100, result.getStats().getCatchesTaken());
        }

        @Test
        @DisplayName("Should handle bowler stats - high wickets, low runs")
        void stats_ForBowler_ShouldHandleCorrectly() {
            // Arrange
            Stats bowlerStats = createStats(200, 500, 300, 50);
            Player bowler = createPlayer("bowler1", "Jasprit Bumrah", "Bowler", "Right-Handed", "Right-Arm Fast");
            bowler.setStats(bowlerStats);

            when(playerRepository.save(any(Player.class))).thenReturn(bowler);

            // Act
            Player result = playerService.create(bowler);

            // Assert
            assertEquals(300, result.getStats().getWicketsTaken());
            assertEquals(500, result.getStats().getRunsScored());
        }

        @Test
        @DisplayName("Should handle wicket-keeper stats - high catches")
        void stats_ForWicketKeeper_ShouldHandleHighCatches() {
            // Arrange
            Stats keeperStats = createStats(350, 10000, 0, 500);
            Player keeper = createPlayer("keeper1", "MS Dhoni", "Wicket-Keeper", "Right-Handed", "None");
            keeper.setStats(keeperStats);

            when(playerRepository.save(any(Player.class))).thenReturn(keeper);

            // Act
            Player result = playerService.create(keeper);

            // Assert
            assertEquals(500, result.getStats().getCatchesTaken());
            assertEquals(0, result.getStats().getWicketsTaken());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle player with all bowling styles")
        void player_WithDifferentBowlingStyles_ShouldCreateSuccessfully() {
            // Arrange
            String[] bowlingStyles = {
                    "Right-Arm Fast", "Left-Arm Fast",
                    "Right-Arm Medium", "Left-Arm Medium",
                    "Right-Arm Spin", "Left-Arm Spin", "None"
            };

            for (String style : bowlingStyles) {
                testPlayer.setBowlingStyle(style);
                testPlayer.setTeamId(null);
                when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

                // Act
                Player result = playerService.create(testPlayer);

                // Assert
                assertNotNull(result);
                assertEquals(style, result.getBowlingStyle());
            }
        }

        @Test
        @DisplayName("Should handle player with both batting styles")
        void player_WithDifferentBattingStyles_ShouldCreateSuccessfully() {
            // Arrange
            String[] battingStyles = { "Right-Handed", "Left-Handed" };

            for (String style : battingStyles) {
                testPlayer.setBattingStyle(style);
                testPlayer.setTeamId(null);
                when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

                // Act
                Player result = playerService.create(testPlayer);

                // Assert
                assertNotNull(result);
                assertEquals(style, result.getBattingStyle());
            }
        }

        @Test
        @DisplayName("Should handle team with empty playerIds list during create")
        void create_TeamWithEmptyPlayerIds_ShouldAddPlayerSuccessfully() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            testTeam.setPlayerIds(new ArrayList<>());

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Player result = playerService.create(testPlayer);

            // Assert
            assertNotNull(result);
            verify(teamRepository, times(1)).save(argThat(team -> team.getPlayerIds().contains(PLAYER_ID)));
        }

        @Test
        @DisplayName("Should verify repository interaction order in create with team")
        void create_WithTeam_ShouldCallRepositoriesInCorrectOrder() {
            // Arrange
            testPlayer.setTeamId(TEAM_ID);
            testTeam.setPlayerIds(new ArrayList<>());

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            playerService.create(testPlayer);

            // Assert - Verify order
            var inOrder = inOrder(teamRepository, playerRepository);
            inOrder.verify(teamRepository).findById(TEAM_ID);
            inOrder.verify(playerRepository).save(any(Player.class));
            inOrder.verify(teamRepository).save(any(Team.class));
        }
    }

    @Nested
    @DisplayName("Repository Interaction Verification Tests")
    class VerificationTests {

        @Test
        @DisplayName("Should not interact with teamRepository in getAllPlayers")
        void getAllPlayers_ShouldNotInteractWithTeamRepository() {
            // Arrange
            when(playerRepository.findAll()).thenReturn(Arrays.asList(testPlayer));

            // Act
            playerService.getAllPlayers();

            // Assert
            verifyNoInteractions(teamRepository);
        }

        @Test
        @DisplayName("Should call findById exactly once in getPlayerById")
        void getPlayerById_ShouldCallFindByIdOnce() {
            // Arrange
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));

            // Act
            playerService.getPlayerById(PLAYER_ID);

            // Assert
            verify(playerRepository, times(1)).findById(PLAYER_ID);
            verifyNoMoreInteractions(playerRepository);
        }

        @Test
        @DisplayName("Should call save exactly once in updatePlayer")
        void updatePlayer_ShouldCallSaveOnce() {
            // Arrange
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            playerService.updatePlayer(PLAYER_ID, testPlayer);

            // Assert
            verify(playerRepository, times(1)).findById(PLAYER_ID);
            verify(playerRepository, times(1)).save(any(Player.class));
        }

        @Test
        @DisplayName("Should not interact with teamRepository in patchPlayer")
        void patchPlayer_ShouldNotInteractWithTeamRepository() {
            // Arrange
            Player patchDetails = new Player();
            patchDetails.setName("Updated Name");

            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            playerService.patchPlayer(PLAYER_ID, patchDetails);

            // Assert
            verifyNoInteractions(teamRepository);
        }
    }
}
