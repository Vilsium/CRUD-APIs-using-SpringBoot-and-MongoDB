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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.example.tournament_data.dto.TeamDetailsResponse;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.repository.PlayerRepository;
import com.example.tournament_data.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService Unit Tests")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TeamService teamService;

    private Team testTeam;
    private Player testPlayer;
    private static final String TEAM_ID = "team123";
    private static final String PLAYER_ID = "player123";
    private static final String CAPTAIN_ID = "captain123";

    @BeforeEach
    void setUp() {
        // Initialize test team
        testTeam = new Team();
        testTeam.setId(TEAM_ID);
        testTeam.setTeamName("Mumbai Indians");
        testTeam.setHomeGround("Wankhede Stadium");
        testTeam.setCoach("Mahela Jayawardene");
        testTeam.setCaptainId(CAPTAIN_ID);
        testTeam.setPlayerIds(new ArrayList<>(Arrays.asList(PLAYER_ID, CAPTAIN_ID)));

        // Initialize test player
        testPlayer = new Player();
        testPlayer.setId(PLAYER_ID);
        testPlayer.setName("Rohit Sharma");
        testPlayer.setRole("Batsman");
        testPlayer.setBattingStyle("Right-Handed");
        testPlayer.setTeamId(TEAM_ID);
    }

    @Nested
    @DisplayName("Create Team Tests")
    class CreateTeamTests {

        @Test
        @DisplayName("Should create team successfully with valid data")
        void create_WithValidData_ShouldReturnSavedTeam() {
            // Arrange
            when(playerRepository.existsById(CAPTAIN_ID)).thenReturn(true);
            when(playerRepository.existsById(PLAYER_ID)).thenReturn(true);
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Team result = teamService.create(testTeam);

            // Assert
            assertNotNull(result);
            assertEquals("Mumbai Indians", result.getTeamName());
            verify(teamRepository, times(1)).save(testTeam);
        }

        @Test
        @DisplayName("Should create team without captain")
        void create_WithoutCaptain_ShouldReturnSavedTeam() {
            // Arrange
            testTeam.setCaptainId(null);
            testTeam.setPlayerIds(new ArrayList<>());
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Team result = teamService.create(testTeam);

            // Assert
            assertNotNull(result);
            verify(playerRepository, never()).existsById(anyString());
            verify(teamRepository, times(1)).save(testTeam);
        }

        @Test
        @DisplayName("Should throw exception when captain not found")
        void create_WithInvalidCaptainId_ShouldThrowException() {
            // Arrange
            when(playerRepository.existsById(CAPTAIN_ID)).thenReturn(false);

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> teamService.create(testTeam));

            assertTrue(exception.getMessage().contains("Player not found"));
            verify(teamRepository, never()).save(any(Team.class));
        }

        @Test
        @DisplayName("Should throw exception when player in playerIds not found")
        void create_WithInvalidPlayerId_ShouldThrowException() {
            // Arrange
            when(playerRepository.existsById(CAPTAIN_ID)).thenReturn(true);
            when(playerRepository.existsById(PLAYER_ID)).thenReturn(false);

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> teamService.create(testTeam));

            assertTrue(exception.getMessage().contains("Player not found"));
            verify(teamRepository, never()).save(any(Team.class));
        }

        @Test
        @DisplayName("Should create team with empty captainId string")
        void create_WithEmptyCaptainId_ShouldSkipValidation() {
            // Arrange
            testTeam.setCaptainId("");
            testTeam.setPlayerIds(new ArrayList<>());
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Team result = teamService.create(testTeam);

            // Assert
            assertNotNull(result);
            verify(playerRepository, never()).existsById(anyString());
        }
    }

    @Nested
    @DisplayName("Get All Teams Tests")
    class GetAllTeamsTests {

        @Test
        @DisplayName("Should return all teams")
        void getAllTeams_ShouldReturnAllTeams() {
            // Arrange
            Team team2 = new Team();
            team2.setId("team456");
            team2.setTeamName("Chennai Super Kings");

            List<Team> teams = Arrays.asList(testTeam, team2);
            when(teamRepository.findAll()).thenReturn(teams);

            // Act
            List<Team> result = teamService.getAllTeams();

            // Assert
            assertEquals(2, result.size());
            verify(teamRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no teams exist")
        void getAllTeams_WhenNoTeams_ShouldReturnEmptyList() {
            // Arrange
            when(teamRepository.findAll()).thenReturn(new ArrayList<>());

            // Act
            List<Team> result = teamService.getAllTeams();

            // Assert
            assertTrue(result.isEmpty());
            verify(teamRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Get Team By ID Tests")
    class GetTeamByIdTests {

        @Test
        @DisplayName("Should return team when found")
        void getTeamById_WhenExists_ShouldReturnTeam() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));

            // Act
            Team result = teamService.getTeamById(TEAM_ID);

            // Assert
            assertNotNull(result);
            assertEquals(TEAM_ID, result.getId());
            assertEquals("Mumbai Indians", result.getTeamName());
            verify(teamRepository, times(1)).findById(TEAM_ID);
        }

        @Test
        @DisplayName("Should throw exception when team not found")
        void getTeamById_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> teamService.getTeamById(TEAM_ID));

            assertTrue(exception.getMessage().contains("Team"));
            verify(teamRepository, times(1)).findById(TEAM_ID);
        }
    }

    @Nested
    @DisplayName("Update Team Tests")
    class UpdateTeamTests {

        @Test
        @DisplayName("Should update team successfully")
        void updateTeam_WithValidData_ShouldReturnUpdatedTeam() {
            // Arrange
            Team updatedDetails = new Team();
            updatedDetails.setTeamName("Mumbai Indians Updated");
            updatedDetails.setHomeGround("New Stadium");
            updatedDetails.setCoach("New Coach");
            updatedDetails.setCaptainId("newCaptain");
            updatedDetails.setPlayerIds(new ArrayList<>(Arrays.asList("player1", "player2")));

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Team result = teamService.updateTeam(TEAM_ID, updatedDetails);

            // Assert
            assertEquals("Mumbai Indians Updated", result.getTeamName());
            assertEquals("New Stadium", result.getHomeGround());
            assertEquals("New Coach", result.getCoach());
            verify(teamRepository, times(1)).save(any(Team.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent team")
        void updateTeam_WhenTeamNotExists_ShouldThrowException() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teamService.updateTeam(TEAM_ID, testTeam));

            verify(teamRepository, never()).save(any(Team.class));
        }
    }

    @Nested
    @DisplayName("Patch Team Tests")
    class PatchTeamTests {

        @Test
        @DisplayName("Should patch only provided fields")
        void patchTeam_WithPartialData_ShouldUpdateOnlyProvidedFields() {
            // Arrange
            Team patchDetails = new Team();
            patchDetails.setTeamName("Patched Name");
            // Other fields are null

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Team result = teamService.patchTeam(TEAM_ID, patchDetails);

            // Assert
            assertEquals("Patched Name", result.getTeamName());
            assertEquals("Wankhede Stadium", result.getHomeGround()); // Unchanged
            assertEquals("Mahela Jayawardene", result.getCoach()); // Unchanged
            verify(teamRepository, times(1)).save(any(Team.class));
        }

        @Test
        @DisplayName("Should patch all fields when all provided")
        void patchTeam_WithAllFields_ShouldUpdateAllFields() {
            // Arrange
            Team patchDetails = new Team();
            patchDetails.setTeamName("New Name");
            patchDetails.setHomeGround("New Ground");
            patchDetails.setCoach("New Coach");
            patchDetails.setCaptainId("newCaptain");
            patchDetails.setPlayerIds(new ArrayList<>(Arrays.asList("p1", "p2")));

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Team result = teamService.patchTeam(TEAM_ID, patchDetails);

            // Assert
            assertEquals("New Name", result.getTeamName());
            assertEquals("New Ground", result.getHomeGround());
            assertEquals("New Coach", result.getCoach());
            assertEquals("newCaptain", result.getCaptainId());
        }

        @Test
        @DisplayName("Should throw exception when patching non-existent team")
        void patchTeam_WhenTeamNotExists_ShouldThrowException() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teamService.patchTeam(TEAM_ID, testTeam));

            verify(teamRepository, never()).save(any(Team.class));
        }
    }

    @Nested
    @DisplayName("Delete Team Tests")
    class DeleteTeamTests {

        @Test
        @DisplayName("Should delete team and update players")
        void deleteTeam_WhenExists_ShouldDeleteAndUpdatePlayers() {
            // Arrange
            List<Player> teamPlayers = Arrays.asList(testPlayer);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByTeamId(TEAM_ID)).thenReturn(teamPlayers);
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Team result = teamService.deleteTeam(TEAM_ID);

            // Assert
            assertNotNull(result);
            assertEquals(TEAM_ID, result.getId());
            verify(playerRepository, times(1)).findByTeamId(TEAM_ID);
            verify(playerRepository, times(1)).save(any(Player.class));
            verify(teamRepository, times(1)).deleteById(TEAM_ID);
        }

        @Test
        @DisplayName("Should delete team with no players")
        void deleteTeam_WithNoPlayers_ShouldDeleteSuccessfully() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByTeamId(TEAM_ID)).thenReturn(new ArrayList<>());

            // Act
            Team result = teamService.deleteTeam(TEAM_ID);

            // Assert
            assertNotNull(result);
            verify(playerRepository, never()).save(any(Player.class));
            verify(teamRepository, times(1)).deleteById(TEAM_ID);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent team")
        void deleteTeam_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teamService.deleteTeam(TEAM_ID));

            verify(teamRepository, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("Should set teamId to null for all players when team deleted")
        void deleteTeam_ShouldSetPlayerTeamIdToNull() {
            // Arrange
            Player player1 = new Player();
            player1.setId("p1");
            player1.setTeamId(TEAM_ID);

            Player player2 = new Player();
            player2.setId("p2");
            player2.setTeamId(TEAM_ID);

            List<Player> teamPlayers = Arrays.asList(player1, player2);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByTeamId(TEAM_ID)).thenReturn(teamPlayers);

            // Act
            teamService.deleteTeam(TEAM_ID);

            // Assert
            verify(playerRepository, times(2)).save(argThat(player -> player.getTeamId() == null));
        }
    }

    @Nested
    @DisplayName("Add Player To Team Tests")
    class AddPlayerToTeamTests {

        @Test
        @DisplayName("Should add player to team successfully")
        void addPlayerToTeam_WithValidIds_ShouldAddPlayer() {
            // Arrange
            String newPlayerId = "newPlayer123";
            Player newPlayer = new Player();
            newPlayer.setId(newPlayerId);
            newPlayer.setName("New Player");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(newPlayerId)).thenReturn(Optional.of(newPlayer));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class))).thenReturn(newPlayer);

            // Act
            Team result = teamService.addPlayerToTeam(TEAM_ID, newPlayerId);

            // Assert
            assertTrue(result.getPlayerIds().contains(newPlayerId));
            verify(playerRepository, times(1)).save(argThat(player -> TEAM_ID.equals(player.getTeamId())));
        }

        @Test
        @DisplayName("Should not add duplicate player to team")
        void addPlayerToTeam_WhenPlayerAlreadyInTeam_ShouldNotDuplicate() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            int initialSize = testTeam.getPlayerIds().size();

            // Act
            Team result = teamService.addPlayerToTeam(TEAM_ID, PLAYER_ID);

            // Assert
            assertEquals(initialSize, result.getPlayerIds().size());
        }

        @Test
        @DisplayName("Should throw exception when team not found")
        void addPlayerToTeam_WhenTeamNotExists_ShouldThrowException() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teamService.addPlayerToTeam(TEAM_ID, PLAYER_ID));
        }

        @Test
        @DisplayName("Should throw exception when player not found")
        void addPlayerToTeam_WhenPlayerNotExists_ShouldThrowException() {
            // Arrange
            testTeam.setPlayerIds(new ArrayList<>()); // Empty list so player will be added
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teamService.addPlayerToTeam(TEAM_ID, PLAYER_ID));
        }
    }

    @Nested
    @DisplayName("Remove Player From Team Tests")
    class RemovePlayerFromTeamTests {

        @Test
        @DisplayName("Should remove player from team successfully")
        void removePlayerFromTeam_WithValidIds_ShouldRemovePlayer() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Team result = teamService.removePlayerFromTeam(TEAM_ID, PLAYER_ID);

            // Assert
            assertFalse(result.getPlayerIds().contains(PLAYER_ID));
            verify(playerRepository, times(1)).save(argThat(player -> player.getTeamId() == null));
        }

        @Test
        @DisplayName("Should clear captainId when removing captain")
        void removePlayerFromTeam_WhenRemovingCaptain_ShouldClearCaptainId() {
            // Arrange
            testTeam.setCaptainId(PLAYER_ID); // Player is captain

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Team result = teamService.removePlayerFromTeam(TEAM_ID, PLAYER_ID);

            // Assert
            assertNull(result.getCaptainId());
        }

        @Test
        @DisplayName("Should not clear captainId when removing non-captain player")
        void removePlayerFromTeam_WhenRemovingNonCaptain_ShouldKeepCaptainId() {
            // Arrange
            testTeam.setCaptainId("differentCaptain");

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

            // Act
            Team result = teamService.removePlayerFromTeam(TEAM_ID, PLAYER_ID);

            // Assert
            assertEquals("differentCaptain", result.getCaptainId());
        }

        @Test
        @DisplayName("Should throw exception when team not found")
        void removePlayerFromTeam_WhenTeamNotExists_ShouldThrowException() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teamService.removePlayerFromTeam(TEAM_ID, PLAYER_ID));
        }

        @Test
        @DisplayName("Should throw exception when player not found")
        void removePlayerFromTeam_WhenPlayerNotExists_ShouldThrowException() {
            // Arrange
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(PLAYER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> teamService.removePlayerFromTeam(TEAM_ID, PLAYER_ID));
        }
    }

    @Nested
    @DisplayName("Get Team Details Tests")
    class GetTeamDetailsTests {

        @Test
        @DisplayName("Should return team details with aggregation")
        void getTeamDetails_WhenTeamExists_ShouldReturnDetails() {
            // Arrange
            TeamDetailsResponse expectedResponse = new TeamDetailsResponse();
            // Set expected response fields as per your DTO

            @SuppressWarnings("unchecked")
            AggregationResults<TeamDetailsResponse> aggregationResults = mock(AggregationResults.class);

            when(aggregationResults.getUniqueMappedResult()).thenReturn(expectedResponse);
            when(mongoTemplate.aggregate(
                    any(Aggregation.class),
                    eq("teams"),
                    eq(TeamDetailsResponse.class))).thenReturn(aggregationResults);

            // Act
            TeamDetailsResponse result = teamService.getTeamDetails(TEAM_ID);

            // Assert
            assertNotNull(result);
            verify(mongoTemplate, times(1)).aggregate(
                    any(Aggregation.class),
                    eq("teams"),
                    eq(TeamDetailsResponse.class));
        }

        @Test
        @DisplayName("Should return null when team not found in aggregation")
        void getTeamDetails_WhenTeamNotExists_ShouldReturnNull() {
            // Arrange
            @SuppressWarnings("unchecked")
            AggregationResults<TeamDetailsResponse> aggregationResults = mock(AggregationResults.class);

            when(aggregationResults.getUniqueMappedResult()).thenReturn(null);
            when(mongoTemplate.aggregate(
                    any(Aggregation.class),
                    eq("teams"),
                    eq(TeamDetailsResponse.class))).thenReturn(aggregationResults);

            // Act
            TeamDetailsResponse result = teamService.getTeamDetails(TEAM_ID);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle team with null playerIds list")
        void create_WithNullPlayerIds_ShouldNotThrowException() {
            // Arrange
            testTeam.setPlayerIds(null);
            testTeam.setCaptainId(null);
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Team result = teamService.create(testTeam);

            // Assert
            assertNotNull(result);
            verify(teamRepository, times(1)).save(testTeam);
        }

        @Test
        @DisplayName("Should handle team with empty playerIds list")
        void create_WithEmptyPlayerIds_ShouldNotThrowException() {
            // Arrange
            testTeam.setPlayerIds(new ArrayList<>());
            testTeam.setCaptainId(null);
            when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

            // Act
            Team result = teamService.create(testTeam);

            // Assert
            assertNotNull(result);
            verify(playerRepository, never()).existsById(anyString());
        }
    }
}
