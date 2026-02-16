package com.example.tournament_data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.example.tournament_data.dto.TeamCreateRequest;
import com.example.tournament_data.dto.TeamDetailsResponse;
import com.example.tournament_data.dto.TeamPatchRequest;
import com.example.tournament_data.dto.TeamResponse;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.repository.PlayerRepository;
import com.example.tournament_data.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService Tests")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TeamService teamService;

    @Captor
    private ArgumentCaptor<Team> teamCaptor;

    @Captor
    private ArgumentCaptor<Player> playerCaptor;

    // Test data
    private Team testTeam;
    private Player testPlayer1;
    private Player testPlayer2;
    private Player testPlayer3;
    private TeamCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // Initialize test players
        testPlayer1 = Player.builder()
                .id(1)
                .name("Virat Kohli")
                .teamId(null)
                .role("Batsman")
                .build();

        testPlayer2 = Player.builder()
                .id(2)
                .name("Rohit Sharma")
                .teamId(null)
                .role("Batsman")
                .build();

        testPlayer3 = Player.builder()
                .id(3)
                .name("Jasprit Bumrah")
                .teamId(null)
                .role("Bowler")
                .build();

        // Initialize test team
        testTeam = Team.builder()
                .id(100)
                .teamName("Mumbai Indians")
                .homeGround("Wankhede Stadium")
                .coach("Mahela Jayawardene")
                .captainId(1)
                .playerIds(new ArrayList<>(Arrays.asList(1, 2, 3)))
                .build();

        // Initialize create request
        createRequest = new TeamCreateRequest();
        createRequest.setTeamName("Chennai Super Kings");
        createRequest.setHomeGround("MA Chidambaram Stadium");
        createRequest.setCoach("Stephen Fleming");
        createRequest.setPlayerNames(Arrays.asList("Virat Kohli", "Rohit Sharma"));
        createRequest.setCaptainName("Virat Kohli");
    }

    // ==================== CREATE TESTS ====================
    @Nested
    @DisplayName("Create Team Tests")
    class CreateTeamTests {

        @Test
        @DisplayName("Should create team successfully with all fields")
        void shouldCreateTeamSuccessfully() {
            // Arrange
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME))
                    .thenReturn(101);
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(2)).thenReturn(Optional.of(testPlayer2));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.create(createRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(101);
            assertThat(response.getTeamName()).isEqualTo("Chennai Super Kings");
            assertThat(response.getHomeGround()).isEqualTo("MA Chidambaram Stadium");
            assertThat(response.getCoach()).isEqualTo("Stephen Fleming");
            assertThat(response.getCaptainName()).isEqualTo("Virat Kohli");
            assertThat(response.getPlayerNames()).containsExactly("Virat Kohli", "Rohit Sharma");

            // Verify team was saved
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getCaptainId()).isEqualTo(1);
            assertThat(savedTeam.getPlayerIds()).contains(1, 2);

            // Verify players' teamId was updated
            verify(playerRepository, times(2)).save(any(Player.class));
        }

        @Test
        @DisplayName("Should create team without players")
        void shouldCreateTeamWithoutPlayers() {
            // Arrange
            createRequest.setPlayerNames(null);
            createRequest.setCaptainName(null);

            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME))
                    .thenReturn(101);
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.create(createRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getPlayerNames()).isEmpty();
            assertThat(response.getCaptainName()).isNull();
        }

        @Test
        @DisplayName("Should create team with empty player list")
        void shouldCreateTeamWithEmptyPlayerList() {
            // Arrange
            createRequest.setPlayerNames(Collections.emptyList());
            createRequest.setCaptainName(null);

            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME))
                    .thenReturn(101);
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.create(createRequest);

            // Assert
            assertThat(response.getPlayerNames()).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when team name already exists")
        void shouldThrowExceptionWhenTeamNameExists() {
            // Arrange
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(testTeam));

            // Act & Assert
            assertThatThrownBy(() -> teamService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("already exists");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when player not found")
        void shouldThrowExceptionWhenPlayerNotFound() {
            // Arrange
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Player not found");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when captain not found")
        void shouldThrowExceptionWhenCaptainNotFound() {
            // Arrange
            createRequest.setPlayerNames(Arrays.asList("Rohit Sharma"));
            createRequest.setCaptainName("Non-existent Player");

            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findByNameIgnoreCase("Non-existent Player"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Captain not found");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when captain is not in team")
        void shouldThrowExceptionWhenCaptainNotInTeam() {
            // Arrange
            createRequest.setPlayerNames(Arrays.asList("Rohit Sharma"));
            createRequest.setCaptainName("Virat Kohli");

            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));

            // Act & Assert
            assertThatThrownBy(() -> teamService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Captain must be a player in the team");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create team with empty captain name")
        void shouldCreateTeamWithEmptyCaptainName() {
            // Arrange
            createRequest.setCaptainName("");

            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME))
                    .thenReturn(101);
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(2)).thenReturn(Optional.of(testPlayer2));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.create(createRequest);

            // Assert
            assertThat(response.getCaptainName()).isNull();
        }
    }

    // ==================== GET ALL TESTS ====================
    @Nested
    @DisplayName("Get All Teams Tests")
    class GetAllTeamsTests {

        @Test
        @DisplayName("Should return all teams")
        void shouldReturnAllTeams() {
            // Arrange
            Team team1 = Team.builder()
                    .id(1)
                    .teamName("Team One")
                    .playerIds(new ArrayList<>())
                    .build();

            Team team2 = Team.builder()
                    .id(2)
                    .teamName("Team Two")
                    .playerIds(new ArrayList<>())
                    .build();

            when(teamRepository.findAll()).thenReturn(Arrays.asList(team1, team2));

            // Act
            List<TeamResponse> responses = teamService.getAllTeams();

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getTeamName()).isEqualTo("Team One");
            assertThat(responses.get(1).getTeamName()).isEqualTo("Team Two");
        }

        @Test
        @DisplayName("Should return empty list when no teams exist")
        void shouldReturnEmptyListWhenNoTeams() {
            // Arrange
            when(teamRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<TeamResponse> responses = teamService.getAllTeams();

            // Assert
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should return teams with player names resolved")
        void shouldReturnTeamsWithPlayerNames() {
            // Arrange
            testTeam.setPlayerIds(new ArrayList<>(Arrays.asList(1, 2)));
            testTeam.setCaptainId(1);

            when(teamRepository.findAll()).thenReturn(Collections.singletonList(testTeam));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(2)).thenReturn(Optional.of(testPlayer2));

            // Act
            List<TeamResponse> responses = teamService.getAllTeams();

            // Assert
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getCaptainName()).isEqualTo("Virat Kohli");
            assertThat(responses.get(0).getPlayerNames()).containsExactly("Virat Kohli", "Rohit Sharma");
        }
    }

    // ==================== GET BY ID TESTS ====================
    @Nested
    @DisplayName("Get Team By ID Tests")
    class GetTeamByIdTests {

        @Test
        @DisplayName("Should return team when found")
        void shouldReturnTeamWhenFound() {
            // Arrange
            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(2)).thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(3)).thenReturn(Optional.of(testPlayer3));

            // Act
            TeamResponse response = teamService.getTeamById(100);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100);
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
            assertThat(response.getCaptainName()).isEqualTo("Virat Kohli");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when team not found")
        void shouldThrowExceptionWhenTeamNotFound() {
            // Arrange
            when(teamRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamById(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Team");
        }

        @Test
        @DisplayName("Should handle team with null captain")
        void shouldHandleTeamWithNullCaptain() {
            // Arrange
            testTeam.setCaptainId(null);

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.getTeamById(100);

            // Assert
            assertThat(response.getCaptainName()).isNull();
        }

        @Test
        @DisplayName("Should handle missing player in team")
        void shouldHandleMissingPlayerInTeam() {
            // Arrange
            testTeam.setPlayerIds(new ArrayList<>(Arrays.asList(1, 999)));
            testTeam.setCaptainId(null);

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            TeamResponse response = teamService.getTeamById(100);

            // Assert
            assertThat(response.getPlayerNames()).containsExactly("Virat Kohli");
        }
    }

    // ==================== UPDATE TESTS ====================
    @Nested
    @DisplayName("Update Team Tests")
    class UpdateTeamTests {

        @Test
        @DisplayName("Should update team successfully")
        void shouldUpdateTeamSuccessfully() {
            // Arrange
            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(anyInt())).thenAnswer(invocation -> {
                Integer id = invocation.getArgument(0);
                if (id == 1)
                    return Optional.of(testPlayer1);
                if (id == 2)
                    return Optional.of(testPlayer2);
                if (id == 3)
                    return Optional.of(testPlayer3);
                return Optional.empty();
            });
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.updateTeam(100, createRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("Chennai Super Kings");
            assertThat(response.getHomeGround()).isEqualTo("MA Chidambaram Stadium");
            assertThat(response.getCoach()).isEqualTo("Stephen Fleming");

            verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should update team with same name")
        void shouldUpdateTeamWithSameName() {
            // Arrange
            createRequest.setTeamName("Mumbai Indians");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam)); // Same team
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.updateTeam(100, createRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should throw exception when new team name exists on different team")
        void shouldThrowExceptionWhenNewNameExistsOnDifferentTeam() {
            // Arrange
            Team anotherTeam = Team.builder()
                    .id(200)
                    .teamName("Chennai Super Kings")
                    .build();

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(anotherTeam));

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(100, createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should clear teamId for removed players")
        void shouldClearTeamIdForRemovedPlayers() {
            // Arrange
            // Original team has players 1, 2, 3
            // New request has only players 1, 2
            // Player 3 should have teamId cleared

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(2)).thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(3)).thenReturn(Optional.of(testPlayer3));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            teamService.updateTeam(100, createRequest);

            // Assert - verify player 3's teamId was cleared
            verify(playerRepository, times(3)).save(playerCaptor.capture());
            List<Player> savedPlayers = playerCaptor.getAllValues();

            Player player3Saved = savedPlayers.stream()
                    .filter(p -> p.getId().equals(3))
                    .findFirst()
                    .orElse(null);

            assertThat(player3Saved).isNotNull();
            assertThat(player3Saved.getTeamId()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when team not found for update")
        void shouldThrowExceptionWhenTeamNotFoundForUpdate() {
            // Arrange
            when(teamRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(999, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when captain not in updated player list")
        void shouldThrowExceptionWhenCaptainNotInUpdatedPlayerList() {
            // Arrange
            createRequest.setPlayerNames(Arrays.asList("Rohit Sharma"));
            createRequest.setCaptainName("Virat Kohli");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));

            // Act & Assert
            assertThatThrownBy(() -> teamService.updateTeam(100, createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Captain must be a player in the team");
        }

        @Test
        @DisplayName("Should update team with no captain")
        void shouldUpdateTeamWithNoCaptain() {
            // Arrange
            createRequest.setCaptainName(null);

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.updateTeam(100, createRequest);

            // Assert
            assertThat(response.getCaptainName()).isNull();
        }
    }

    // ==================== PATCH TESTS ====================
    @Nested
    @DisplayName("Patch Team Tests")
    class PatchTeamTests {

        private TeamPatchRequest patchRequest;

        @BeforeEach
        void setUpPatchRequest() {
            patchRequest = new TeamPatchRequest();
        }

        @Test
        @DisplayName("Should patch only team name")
        void shouldPatchOnlyTeamName() {
            // Arrange
            patchRequest.setTeamName("New Team Name");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("New Team Name"))
                    .thenReturn(Optional.empty());
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("New Team Name");
            assertThat(response.getHomeGround()).isEqualTo("Wankhede Stadium"); // Unchanged
        }

        @Test
        @DisplayName("Should patch only home ground")
        void shouldPatchOnlyHomeGround() {
            // Arrange
            patchRequest.setHomeGround("New Stadium");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getHomeGround()).isEqualTo("New Stadium");
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians"); // Unchanged
        }

        @Test
        @DisplayName("Should patch only coach")
        void shouldPatchOnlyCoach() {
            // Arrange
            patchRequest.setCoach("New Coach");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getCoach()).isEqualTo("New Coach");
        }

        @Test
        @DisplayName("Should patch captain")
        void shouldPatchCaptain() {
            // Arrange
            patchRequest.setCaptainName("Rohit Sharma");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenAnswer(invocation -> {
                Integer id = invocation.getArgument(0);
                if (id == 2)
                    return Optional.of(testPlayer2);
                return Optional.of(testPlayer1);
            });

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getCaptainName()).isEqualTo("Rohit Sharma");
        }

        @Test
        @DisplayName("Should clear captain when blank name provided")
        void shouldClearCaptainWhenBlankNameProvided() {
            // Arrange
            patchRequest = new TeamPatchRequest(); // Fresh request
            patchRequest.setCaptainName(""); // Only setting captain to blank

            // Setup existing team with captain
            testTeam.setCaptainId(1);
            testTeam.setPlayerIds(new ArrayList<>(Arrays.asList(1, 2, 3)));

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Mock for convertToResponse() - needed to return player names
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(2)).thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(3)).thenReturn(Optional.of(testPlayer3));

            teamService.patchTeam(100, patchRequest);

            // Assert - verify save was called
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getCaptainId()).isNull();
        }

        @Test
        @DisplayName("Should patch players list")
        void shouldPatchPlayersList() {
            // Arrange
            patchRequest.setPlayerNames(Arrays.asList("Virat Kohli"));

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getPlayerNames()).containsExactly("Virat Kohli");
        }

        @Test
        @DisplayName("Should clear captain when captain removed from players list")
        void shouldClearCaptainWhenRemovedFromPlayersList() {
            // Arrange
            testTeam.setCaptainId(3); // Captain is player 3
            patchRequest.setPlayerNames(Arrays.asList("Virat Kohli", "Rohit Sharma")); // No player 3

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(anyInt())).thenAnswer(invocation -> {
                Integer id = invocation.getArgument(0);
                if (id == 1)
                    return Optional.of(testPlayer1);
                if (id == 2)
                    return Optional.of(testPlayer2);
                if (id == 3)
                    return Optional.of(testPlayer3);
                return Optional.empty();
            });
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            teamService.patchTeam(100, patchRequest);

            // Assert
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getCaptainId()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when captain not in team on patch")
        void shouldThrowExceptionWhenCaptainNotInTeamOnPatch() {
            // Arrange
            Player outsidePlayer = Player.builder()
                    .id(999)
                    .name("Outside Player")
                    .build();

            patchRequest.setCaptainName("Outside Player");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByNameIgnoreCase("Outside Player"))
                    .thenReturn(Optional.of(outsidePlayer));

            // Act & Assert
            assertThatThrownBy(() -> teamService.patchTeam(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Captain must be a player in the team");
        }

        @Test
        @DisplayName("Should throw exception when team name exists on different team")
        void shouldThrowExceptionWhenTeamNameExistsOnPatch() {
            // Arrange
            Team anotherTeam = Team.builder()
                    .id(200)
                    .teamName("Existing Team")
                    .build();

            patchRequest.setTeamName("Existing Team");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("Existing Team"))
                    .thenReturn(Optional.of(anotherTeam));

            // Act & Assert
            assertThatThrownBy(() -> teamService.patchTeam(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when team not found for patch")
        void shouldThrowExceptionWhenTeamNotFoundForPatch() {
            // Arrange
            when(teamRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.patchTeam(999, patchRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when player not found on patch")
        void shouldThrowExceptionWhenPlayerNotFoundOnPatch() {
            // Arrange
            patchRequest.setPlayerNames(Arrays.asList("Non-existent Player"));

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByNameIgnoreCase("Non-existent Player"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.patchTeam(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Player not found");
        }

        @Test
        @DisplayName("Should ignore blank team name on patch")
        void shouldIgnoreBlankTeamNameOnPatch() {
            // Arrange
            patchRequest.setTeamName("   ");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians"); // Unchanged
        }

        @Test
        @DisplayName("Should ignore blank home ground on patch")
        void shouldIgnoreBlankHomeGroundOnPatch() {
            // Arrange
            patchRequest.setHomeGround("");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getHomeGround()).isEqualTo("Wankhede Stadium"); // Unchanged
        }

        @Test
        @DisplayName("Should ignore blank coach on patch")
        void shouldIgnoreBlankCoachOnPatch() {
            // Arrange
            patchRequest.setCoach("  ");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getCoach()).isEqualTo("Mahela Jayawardene"); // Unchanged
        }

        @Test
        @DisplayName("Should update players' teamId on patch")
        void shouldUpdatePlayersTeamIdOnPatch() {
            // Arrange
            Player newPlayer = Player.builder()
                    .id(4)
                    .name("New Player")
                    .teamId(null)
                    .build();

            patchRequest.setPlayerNames(Arrays.asList("New Player"));
            testTeam.setPlayerIds(new ArrayList<>(Arrays.asList(1))); // Original has player 1
            testTeam.setCaptainId(null);

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByNameIgnoreCase("New Player"))
                    .thenReturn(Optional.of(newPlayer));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(4)).thenReturn(Optional.of(newPlayer));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            teamService.patchTeam(100, patchRequest);

            // Assert - verify player 1's teamId was cleared and player 4's was set
            verify(playerRepository, times(2)).save(playerCaptor.capture());
            List<Player> savedPlayers = playerCaptor.getAllValues();

            Player player1Saved = savedPlayers.stream()
                    .filter(p -> p.getId().equals(1))
                    .findFirst()
                    .orElse(null);

            Player newPlayerSaved = savedPlayers.stream()
                    .filter(p -> p.getId().equals(4))
                    .findFirst()
                    .orElse(null);

            assertThat(player1Saved).isNotNull();
            assertThat(player1Saved.getTeamId()).isNull();

            assertThat(newPlayerSaved).isNotNull();
            assertThat(newPlayerSaved.getTeamId()).isEqualTo(100);
        }
    }

    // ==================== DELETE TESTS ====================
    @Nested
    @DisplayName("Delete Team Tests")
    class DeleteTeamTests {

        @Test
        @DisplayName("Should delete team successfully")
        void shouldDeleteTeamSuccessfully() {
            // Arrange
            testPlayer1.setTeamId(100);
            testPlayer2.setTeamId(100);

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByTeamId(100))
                    .thenReturn(Arrays.asList(testPlayer1, testPlayer2));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.deleteTeam(100);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100);
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");

            verify(teamRepository).deleteById(100);
            verify(playerRepository, times(2)).save(any(Player.class));
        }

        @Test
        @DisplayName("Should clear players' teamId on delete")
        void shouldClearPlayersTeamIdOnDelete() {
            // Arrange
            testPlayer1.setTeamId(100);
            testPlayer2.setTeamId(100);

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByTeamId(100))
                    .thenReturn(Arrays.asList(testPlayer1, testPlayer2));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            teamService.deleteTeam(100);

            // Assert
            verify(playerRepository, times(2)).save(playerCaptor.capture());
            List<Player> savedPlayers = playerCaptor.getAllValues();

            for (Player player : savedPlayers) {
                assertThat(player.getTeamId()).isNull();
            }
        }

        @Test
        @DisplayName("Should delete team with no players")
        void shouldDeleteTeamWithNoPlayers() {
            // Arrange
            testTeam.setPlayerIds(new ArrayList<>());
            testTeam.setCaptainId(null);

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByTeamId(100)).thenReturn(Collections.emptyList());

            // Act
            TeamResponse response = teamService.deleteTeam(100);

            // Assert
            assertThat(response).isNotNull();
            verify(teamRepository).deleteById(100);
            verify(playerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when team not found for delete")
        void shouldThrowExceptionWhenTeamNotFoundForDelete() {
            // Arrange
            when(teamRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> teamService.deleteTeam(999))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(teamRepository, never()).deleteById(anyInt());
        }
    }

    // ==================== GET TEAM DETAILS (AGGREGATION) TESTS
    // ====================
    @Nested
    @DisplayName("Get Team Details Tests")
    class GetTeamDetailsTests {

        @Test
        @DisplayName("Should return team details with aggregation")
        void shouldReturnTeamDetailsWithAggregation() {
            // Arrange
            TeamDetailsResponse expectedResponse = TeamDetailsResponse.builder()
                    .id(100)
                    .teamName("Mumbai Indians")
                    .homeGround("Wankhede Stadium")
                    .coach("Mahela Jayawardene")
                    .captainId(1)
                    .build();

            @SuppressWarnings("unchecked")
            AggregationResults<TeamDetailsResponse> aggregationResults = org.mockito.Mockito
                    .mock(AggregationResults.class);

            when(teamRepository.existsById(100)).thenReturn(true);
            when(aggregationResults.getUniqueMappedResult()).thenReturn(expectedResponse);
            when(mongoTemplate.aggregate(any(Aggregation.class), eq("teams"), eq(TeamDetailsResponse.class)))
                    .thenReturn(aggregationResults);

            // Act
            TeamDetailsResponse response = teamService.getTeamDetails(100);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100);
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
        }

        @Test
        @DisplayName("Should throw exception when team not found for details")
        void shouldThrowExceptionWhenTeamNotFoundForDetails() {
            // Arrange
            when(teamRepository.existsById(999)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamDetails(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Team");

            verify(mongoTemplate, never()).aggregate(any(), anyString(), any());
        }

        @Test
        @DisplayName("Should throw exception when aggregation returns null")
        void shouldThrowExceptionWhenAggregationReturnsNull() {
            // Arrange
            @SuppressWarnings("unchecked")
            AggregationResults<TeamDetailsResponse> aggregationResults = org.mockito.Mockito
                    .mock(AggregationResults.class);

            when(teamRepository.existsById(100)).thenReturn(true);
            when(aggregationResults.getUniqueMappedResult()).thenReturn(null);
            when(mongoTemplate.aggregate(any(Aggregation.class), eq("teams"), eq(TeamDetailsResponse.class)))
                    .thenReturn(aggregationResults);

            // Act & Assert
            assertThatThrownBy(() -> teamService.getTeamDetails(100))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== HELPER METHOD TESTS ====================
    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("Should convert team to response with all fields")
        void shouldConvertTeamToResponseWithAllFields() {
            // Arrange
            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findById(2)).thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(3)).thenReturn(Optional.of(testPlayer3));

            // Act
            TeamResponse response = teamService.getTeamById(100);

            // Assert
            assertThat(response.getId()).isEqualTo(100);
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
            assertThat(response.getHomeGround()).isEqualTo("Wankhede Stadium");
            assertThat(response.getCoach()).isEqualTo("Mahela Jayawardene");
            assertThat(response.getCaptainName()).isEqualTo("Virat Kohli");
            assertThat(response.getPlayerNames()).hasSize(3);
        }

        @Test
        @DisplayName("Should handle captain not found in conversion")
        void shouldHandleCaptainNotFoundInConversion() {
            // Arrange
            testTeam.setCaptainId(999);
            testTeam.setPlayerIds(new ArrayList<>());

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            TeamResponse response = teamService.getTeamById(100);

            // Assert
            assertThat(response.getCaptainName()).isNull();
        }
    }

    // ==================== EDGE CASE TESTS ====================
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle case-insensitive team name search")
        void shouldHandleCaseInsensitiveTeamName() {
            // Arrange
            createRequest.setTeamName("CHENNAI SUPER KINGS");

            when(teamRepository.findByTeamNameIgnoreCase("CHENNAI SUPER KINGS"))
                    .thenReturn(Optional.empty());
            when(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME))
                    .thenReturn(101);
            when(playerRepository.findByNameIgnoreCase(anyString()))
                    .thenReturn(Optional.of(testPlayer1));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.create(createRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("CHENNAI SUPER KINGS");
        }

        @Test
        @DisplayName("Should handle case-insensitive player name search")
        void shouldHandleCaseInsensitivePlayerName() {
            // Arrange
            createRequest.setPlayerNames(Arrays.asList("VIRAT KOHLI"));
            createRequest.setCaptainName("VIRAT KOHLI");

            when(teamRepository.findByTeamNameIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME))
                    .thenReturn(101);
            when(playerRepository.findByNameIgnoreCase("VIRAT KOHLI"))
                    .thenReturn(Optional.of(testPlayer1));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(1)).thenReturn(Optional.of(testPlayer1));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.create(createRequest);

            // Assert
            assertThat(response).isNotNull();

            // FIX: Service calls findByNameIgnoreCase twice:
            // 1. Once when processing playerNames list
            // 2. Once when processing captainName
            verify(playerRepository, times(2)).findByNameIgnoreCase("VIRAT KOHLI");
        }

        @Test
        @DisplayName("Should handle multiple simultaneous updates on patch")
        void shouldHandleMultipleSimultaneousUpdatesOnPatch() {
            // Arrange
            TeamPatchRequest patchRequest = new TeamPatchRequest();
            patchRequest.setTeamName("New Name");
            patchRequest.setHomeGround("New Ground");
            patchRequest.setCoach("New Coach");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("New Name"))
                    .thenReturn(Optional.empty());
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("New Name");
            assertThat(response.getHomeGround()).isEqualTo("New Ground");
            assertThat(response.getCoach()).isEqualTo("New Coach");
        }

        @Test
        @DisplayName("Should allow patching team name to same value")
        void shouldAllowPatchingTeamNameToSameValue() {
            // Arrange
            TeamPatchRequest patchRequest = new TeamPatchRequest();
            patchRequest.setTeamName("Mumbai Indians");

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam)); // Same team
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(testPlayer1));

            // Act
            TeamResponse response = teamService.patchTeam(100, patchRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
            verify(teamRepository).save(any(Team.class));
        }

        @Test
        @DisplayName("Should handle empty player names list on create")
        void shouldHandleEmptyPlayerNamesListOnCreate() {
            // Arrange
            createRequest.setPlayerNames(new ArrayList<>());
            createRequest.setCaptainName(null);

            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());
            when(sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME))
                    .thenReturn(101);
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            TeamResponse response = teamService.create(createRequest);

            // Assert
            assertThat(response.getPlayerNames()).isEmpty();
            assertThat(response.getCaptainName()).isNull();
        }

        @Test
        @DisplayName("Should preserve captain when updating players list with captain included")
        void shouldPreserveCaptainWhenUpdatingPlayersListWithCaptainIncluded() {
            // Arrange
            testTeam.setCaptainId(1);
            TeamPatchRequest patchRequest = new TeamPatchRequest();
            patchRequest.setPlayerNames(Arrays.asList("Virat Kohli", "Rohit Sharma"));

            when(teamRepository.findById(100)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findByNameIgnoreCase("Virat Kohli"))
                    .thenReturn(Optional.of(testPlayer1));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(testPlayer2));
            when(playerRepository.findById(anyInt())).thenAnswer(invocation -> {
                Integer id = invocation.getArgument(0);
                if (id == 1)
                    return Optional.of(testPlayer1);
                if (id == 2)
                    return Optional.of(testPlayer2);
                if (id == 3)
                    return Optional.of(testPlayer3);
                return Optional.empty();
            });
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            teamService.patchTeam(100, patchRequest);

            // Assert
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getCaptainId()).isEqualTo(1); // Captain preserved
        }
    }
}
