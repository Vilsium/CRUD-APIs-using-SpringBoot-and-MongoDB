package com.example.tournament_data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

import com.example.tournament_data.dto.PlayerCreateRequest;
import com.example.tournament_data.dto.PlayerPatchRequest;
import com.example.tournament_data.dto.PlayerResponse;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Stats;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.repository.PlayerRepository;
import com.example.tournament_data.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService Tests")
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @InjectMocks
    private PlayerService playerService;

    @Captor
    private ArgumentCaptor<Player> playerCaptor;

    @Captor
    private ArgumentCaptor<Team> teamCaptor;

    // Test data
    private Team testTeam;
    private Player testPlayer;
    private PlayerCreateRequest createRequest;
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

        // Initialize test team
        testTeam = Team.builder()
                .id(1)
                .teamName("Mumbai Indians")
                .playerIds(new ArrayList<>(Arrays.asList(10, 11, 12)))
                .captainId(10)
                .build();

        // Initialize test player
        testPlayer = Player.builder()
                .id(100)
                .name("Virat Kohli")
                .teamId(1)
                .role("Batsman")
                .battingStyle("Right-handed")
                .bowlingStyle("Right-arm medium")
                .stats(testStats)
                .build();

        // Initialize create request
        createRequest = new PlayerCreateRequest();
        createRequest.setName("New Player");
        createRequest.setTeamName("Mumbai Indians");
        createRequest.setRole("All-rounder");
        createRequest.setBattingStyle("Left-handed");
        createRequest.setBowlingStyle("Left-arm spin");
        createRequest.setStats(testStats);
    }

    @Nested
    @DisplayName("Create Player Tests")
    class CreatePlayerTests {

        @Test
        @DisplayName("Should create player successfully with all fields")
        void shouldCreatePlayerSuccessfully() {
            // Arrange
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam));
            when(sequenceGeneratorService.generateSequence(Player.SEQUENCE_NAME))
                    .thenReturn(101);
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Mock existing players in team (none with same name)
            when(playerRepository.findById(10)).thenReturn(Optional.of(
                    Player.builder().id(10).name("Player One").build()));
            when(playerRepository.findById(11)).thenReturn(Optional.of(
                    Player.builder().id(11).name("Player Two").build()));
            when(playerRepository.findById(12)).thenReturn(Optional.of(
                    Player.builder().id(12).name("Player Three").build()));

            // Mock for convertToResponse() method
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

            // Act
            PlayerResponse response = playerService.create(createRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(101);
            assertThat(response.getName()).isEqualTo("New Player");
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
            assertThat(response.getRole()).isEqualTo("All-rounder");
            assertThat(response.getBattingStyle()).isEqualTo("Left-handed");
            assertThat(response.getBowlingStyle()).isEqualTo("Left-arm spin");

            // Verify player was saved
            verify(playerRepository).save(playerCaptor.capture());
            Player savedPlayer = playerCaptor.getValue();
            assertThat(savedPlayer.getTeamId()).isEqualTo(1);

            // Verify team was updated with new player ID
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getPlayerIds()).contains(101);
        }

        @Test
        @DisplayName("Should create player with default stats when stats not provided")
        void shouldCreatePlayerWithDefaultStats() {
            // Arrange
            createRequest.setStats(null);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam));
            when(sequenceGeneratorService.generateSequence(Player.SEQUENCE_NAME))
                    .thenReturn(101);
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Mock existing players
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(
                    Player.builder().id(1).name("Other Player").build()));

            // Mock for convertToResponse()
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

            // Act
            PlayerResponse response = playerService.create(createRequest);

            // Assert
            assertThat(response.getStats()).isNotNull();
            assertThat(response.getStats().getMatchesPlayed()).isEqualTo(0);
            assertThat(response.getStats().getRunsScored()).isEqualTo(0);
            assertThat(response.getStats().getWicketsTaken()).isEqualTo(0);
            assertThat(response.getStats().getCatchesTaken()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw exception when team not found")
        void shouldThrowExceptionWhenTeamNotFound() {
            // Arrange
            when(teamRepository.findByTeamNameIgnoreCase("Non-existent Team"))
                    .thenReturn(Optional.empty());

            createRequest.setTeamName("Non-existent Team");

            // Act & Assert
            assertThatThrownBy(() -> playerService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Team not found");

            verify(playerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when player with same name exists in team")
        void shouldThrowExceptionWhenDuplicatePlayerName() {
            // Arrange
            createRequest.setName("Player One"); // Same name as existing player

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(10)).thenReturn(Optional.of(
                    Player.builder().id(10).name("Player One").build()));

            // Act & Assert
            assertThatThrownBy(() -> playerService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("already exists in team");

            verify(playerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when team has reached maximum player limit")
        void shouldThrowExceptionWhenTeamFull() {
            // Arrange
            List<Integer> fullPlayerList = new ArrayList<>();
            for (int i = 1; i <= 25; i++) {
                fullPlayerList.add(i);
            }
            testTeam.setPlayerIds(fullPlayerList);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam));

            // Mock existing players (none with same name)
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(
                    Player.builder().id(1).name("Different Name").build()));

            // Act & Assert
            assertThatThrownBy(() -> playerService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("maximum player limit of 25");

            verify(playerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle partial stats with null values")
        void shouldHandlePartialStats() {
            // Arrange
            Stats partialStats = Stats.builder()
                    .matchesPlayed(10)
                    .runsScored(null)
                    .wicketsTaken(5)
                    .catchesTaken(null)
                    .build();
            createRequest.setStats(partialStats);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam));
            when(sequenceGeneratorService.generateSequence(Player.SEQUENCE_NAME))
                    .thenReturn(101);
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(
                    Player.builder().id(1).name("Other").build()));

            // Mock for convertToResponse()
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

            // Act
            PlayerResponse response = playerService.create(createRequest);

            // Assert
            assertThat(response.getStats().getMatchesPlayed()).isEqualTo(10);
            assertThat(response.getStats().getRunsScored()).isEqualTo(0);
            assertThat(response.getStats().getWicketsTaken()).isEqualTo(5);
            assertThat(response.getStats().getCatchesTaken()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Get All Players Tests")
    class GetAllPlayersTests {

        @Test
        @DisplayName("Should return all players")
        void shouldReturnAllPlayers() {
            // Arrange
            Player player1 = Player.builder()
                    .id(1)
                    .name("Player One")
                    .teamId(1)
                    .role("Batsman")
                    .build();

            Player player2 = Player.builder()
                    .id(2)
                    .name("Player Two")
                    .teamId(1)
                    .role("Bowler")
                    .build();

            when(playerRepository.findAll()).thenReturn(Arrays.asList(player1, player2));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

            // Act
            List<PlayerResponse> responses = playerService.getAllPlayers();

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo("Player One");
            assertThat(responses.get(1).getName()).isEqualTo("Player Two");
            assertThat(responses.get(0).getTeamName()).isEqualTo("Mumbai Indians");
        }

        @Test
        @DisplayName("Should return empty list when no players exist")
        void shouldReturnEmptyListWhenNoPlayers() {
            // Arrange
            when(playerRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<PlayerResponse> responses = playerService.getAllPlayers();

            // Assert
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should handle player with null teamId")
        void shouldHandlePlayerWithNullTeamId() {
            // Arrange
            Player playerWithoutTeam = Player.builder()
                    .id(1)
                    .name("Free Agent")
                    .teamId(null)
                    .role("Batsman")
                    .build();

            when(playerRepository.findAll()).thenReturn(Collections.singletonList(playerWithoutTeam));

            // Act
            List<PlayerResponse> responses = playerService.getAllPlayers();

            // Assert
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getTeamName()).isNull();
        }
    }

    @Nested
    @DisplayName("Get Player By ID Tests")
    class GetPlayerByIdTests {

        @Test
        @DisplayName("Should return player when found")
        void shouldReturnPlayerWhenFound() {
            // Arrange
            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

            // Act
            PlayerResponse response = playerService.getPlayerById(100);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100);
            assertThat(response.getName()).isEqualTo("Virat Kohli");
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
            assertThat(response.getRole()).isEqualTo("Batsman");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when player not found")
        void shouldThrowExceptionWhenPlayerNotFound() {
            // Arrange
            when(playerRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> playerService.getPlayerById(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Player");
        }
    }

    @Nested
    @DisplayName("Update Player Tests")
    class UpdatePlayerTests {

        @Test
        @DisplayName("Should update player successfully without team change")
        void shouldUpdatePlayerSuccessfully() {
            // Arrange
            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

            createRequest.setName("Updated Name");
            createRequest.setRole("Wicket-keeper");

            // Act
            PlayerResponse response = playerService.updatePlayer(100, createRequest);

            // Assert
            assertThat(response.getName()).isEqualTo("Updated Name");
            assertThat(response.getRole()).isEqualTo("Wicket-keeper");
            verify(playerRepository).save(any(Player.class));
        }

        @Test
        @DisplayName("Should handle team transfer on update")
        void shouldHandleTeamTransfer() {
            // Arrange
            Team newTeam = Team.builder()
                    .id(2)
                    .teamName("Chennai Super Kings")
                    .playerIds(new ArrayList<>())
                    .captainId(null)
                    .build();

            // Player currently in team 1, moving to team 2
            testPlayer.setTeamId(1);
            testTeam.getPlayerIds().add(100); // Player is in old team

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(newTeam));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findById(2)).thenReturn(Optional.of(newTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            createRequest.setTeamName("Chennai Super Kings");

            // Act
            PlayerResponse response = playerService.updatePlayer(100, createRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("Chennai Super Kings");

            // Verify old team was updated (player removed)
            verify(teamRepository, times(2)).save(teamCaptor.capture());
            List<Team> savedTeams = teamCaptor.getAllValues();

            // Find the old team save
            boolean oldTeamUpdated = savedTeams.stream()
                    .anyMatch(t -> t.getId().equals(1) && !t.getPlayerIds().contains(100));
            assertThat(oldTeamUpdated).isTrue();
        }

        @Test
        @DisplayName("Should clear captain when transferred player was captain")
        void shouldClearCaptainOnTransfer() {
            // Arrange
            testTeam.setCaptainId(100); // Player is captain
            testTeam.getPlayerIds().add(100);

            Team newTeam = Team.builder()
                    .id(2)
                    .teamName("Chennai Super Kings")
                    .playerIds(new ArrayList<>())
                    .build();

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(newTeam));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findById(2)).thenReturn(Optional.of(newTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            createRequest.setTeamName("Chennai Super Kings");

            // Act
            playerService.updatePlayer(100, createRequest);

            // Assert - verify captain was cleared
            verify(teamRepository, times(2)).save(teamCaptor.capture());
            Team oldTeamSaved = teamCaptor.getAllValues().stream()
                    .filter(t -> t.getId().equals(1))
                    .findFirst()
                    .orElse(null);

            assertThat(oldTeamSaved).isNotNull();
            assertThat(oldTeamSaved.getCaptainId()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when new team not found")
        void shouldThrowExceptionWhenNewTeamNotFound() {
            // Arrange
            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findByTeamNameIgnoreCase("Non-existent Team"))
                    .thenReturn(Optional.empty());

            createRequest.setTeamName("Non-existent Team");

            // Act & Assert
            assertThatThrownBy(() -> playerService.updatePlayer(100, createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Team not found");
        }

        @Test
        @DisplayName("Should throw exception on duplicate name in new team")
        void shouldThrowExceptionOnDuplicateNameInNewTeam() {
            // Arrange
            Team newTeam = Team.builder()
                    .id(2)
                    .teamName("Chennai Super Kings")
                    .playerIds(new ArrayList<>(Arrays.asList(200)))
                    .build();

            Player existingPlayerInNewTeam = Player.builder()
                    .id(200)
                    .name("Updated Name")
                    .teamId(2)
                    .build();

            // Only mock what's actually needed before exception is thrown
            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(newTeam));
            when(playerRepository.findById(200)).thenReturn(Optional.of(existingPlayerInNewTeam));

            createRequest.setTeamName("Chennai Super Kings");
            createRequest.setName("Updated Name");

            // Act & Assert
            assertThatThrownBy(() -> playerService.updatePlayer(100, createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("already exists in team");
        }

        @Test
        @DisplayName("Should throw exception when player not found for update")
        void shouldThrowExceptionWhenPlayerNotFoundForUpdate() {
            // Arrange
            when(playerRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> playerService.updatePlayer(999, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should handle player with null teamId on update")
        void shouldHandlePlayerWithNullTeamIdOnUpdate() {
            // Arrange
            testPlayer.setTeamId(null);

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.updatePlayer(100, createRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
            verify(teamRepository).save(any(Team.class));
        }
    }

    @Nested
    @DisplayName("Patch Player Tests")
    class PatchPlayerTests {

        private PlayerPatchRequest patchRequest;

        @BeforeEach
        void setUpPatchRequest() {
            patchRequest = new PlayerPatchRequest();
        }

        @Test
        @DisplayName("Should patch only name")
        void shouldPatchOnlyName() {
            // Arrange
            patchRequest.setName("New Name");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getName()).isEqualTo("New Name");
            assertThat(response.getRole()).isEqualTo("Batsman"); // Unchanged
        }

        @Test
        @DisplayName("Should patch only role")
        void shouldPatchOnlyRole() {
            // Arrange
            patchRequest.setRole("All-rounder");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getRole()).isEqualTo("All-rounder");
            assertThat(response.getName()).isEqualTo("Virat Kohli"); // Unchanged
        }

        @Test
        @DisplayName("Should patch batting and bowling style")
        void shouldPatchBattingAndBowlingStyle() {
            // Arrange
            patchRequest.setBattingStyle("Left-handed");
            patchRequest.setBowlingStyle("Left-arm spin");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getBattingStyle()).isEqualTo("Left-handed");
            assertThat(response.getBowlingStyle()).isEqualTo("Left-arm spin");
        }

        @Test
        @DisplayName("Should patch team (transfer player)")
        void shouldPatchTeam() {
            // Arrange
            Team newTeam = Team.builder()
                    .id(2)
                    .teamName("Chennai Super Kings")
                    .playerIds(new ArrayList<>())
                    .build();

            testTeam.getPlayerIds().add(100);
            patchRequest.setTeamName("Chennai Super Kings");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(newTeam));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(teamRepository.findById(2)).thenReturn(Optional.of(newTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("Chennai Super Kings");
        }

        @Test
        @DisplayName("Should patch stats partially")
        void shouldPatchStatsPartially() {
            // Arrange
            Stats patchStats = Stats.builder()
                    .matchesPlayed(100)
                    .runsScored(null) // Don't update
                    .wicketsTaken(50)
                    .catchesTaken(null) // Don't update
                    .build();
            patchRequest.setStats(patchStats);

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getStats().getMatchesPlayed()).isEqualTo(100);
            assertThat(response.getStats().getRunsScored()).isEqualTo(2000); // Original value
            assertThat(response.getStats().getWicketsTaken()).isEqualTo(50);
            assertThat(response.getStats().getCatchesTaken()).isEqualTo(25); // Original value
        }

        @Test
        @DisplayName("Should create stats when patching player with null stats")
        void shouldCreateStatsWhenNull() {
            // Arrange
            testPlayer.setStats(null);
            Stats patchStats = Stats.builder()
                    .matchesPlayed(10)
                    .build();
            patchRequest.setStats(patchStats);

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getStats()).isNotNull();
            assertThat(response.getStats().getMatchesPlayed()).isEqualTo(10);
            assertThat(response.getStats().getRunsScored()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw exception when patching name with duplicate")
        void shouldThrowExceptionOnDuplicateNamePatch() {
            // Arrange
            Player existingPlayerInTeam = Player.builder()
                    .id(10)
                    .name("Duplicate Name")
                    .teamId(1)
                    .build();

            testTeam.getPlayerIds().add(100);
            patchRequest.setName("Duplicate Name");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(10)).thenReturn(Optional.of(existingPlayerInTeam));

            // Act & Assert
            assertThatThrownBy(() -> playerService.patchPlayer(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("already exists in team");
        }

        @Test
        @DisplayName("Should throw exception when new team is full on patch")
        void shouldThrowExceptionWhenNewTeamFullOnPatch() {
            // Arrange
            List<Integer> fullPlayerList = new ArrayList<>();
            for (int i = 1; i <= 25; i++) {
                fullPlayerList.add(i);
            }

            Team fullTeam = Team.builder()
                    .id(2)
                    .teamName("Full Team")
                    .playerIds(fullPlayerList)
                    .build();

            patchRequest.setTeamName("Full Team");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findByTeamNameIgnoreCase("Full Team"))
                    .thenReturn(Optional.of(fullTeam));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act & Assert
            assertThatThrownBy(() -> playerService.patchPlayer(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("maximum player limit of 25");
        }

        @Test
        @DisplayName("Should throw exception when player not found for patch")
        void shouldThrowExceptionWhenPlayerNotFoundForPatch() {
            // Arrange
            when(playerRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> playerService.patchPlayer(999, patchRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should ignore blank name on patch")
        void shouldIgnoreBlankNameOnPatch() {
            // Arrange
            patchRequest.setName("   ");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getName()).isEqualTo("Virat Kohli"); // Unchanged
        }

        @Test
        @DisplayName("Should ignore blank role on patch")
        void shouldIgnoreBlankRoleOnPatch() {
            // Arrange
            patchRequest.setRole("");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getRole()).isEqualTo("Batsman"); // Unchanged
        }

        @Test
        @DisplayName("Should handle player without team on name patch")
        void shouldHandlePlayerWithoutTeamOnNamePatch() {
            // Arrange
            testPlayer.setTeamId(null);
            patchRequest.setName("New Name");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getName()).isEqualTo("New Name");
            assertThat(response.getTeamName()).isNull();
        }
    }

    @Nested
    @DisplayName("Delete Player Tests")
    class DeletePlayerTests {

        @Test
        @DisplayName("Should delete player successfully")
        void shouldDeletePlayerSuccessfully() {
            // Arrange
            testTeam.getPlayerIds().add(100);

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.deletePlayer(100);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100);
            assertThat(response.getName()).isEqualTo("Virat Kohli");

            verify(playerRepository).deleteById(100);

            // Verify player was removed from team
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getPlayerIds()).doesNotContain(100);
        }

        @Test
        @DisplayName("Should clear captain when deleting captain")
        void shouldClearCaptainWhenDeletingCaptain() {
            // Arrange
            testTeam.setCaptainId(100);
            testTeam.getPlayerIds().add(100);

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            playerService.deletePlayer(100);

            // Assert
            verify(teamRepository).save(teamCaptor.capture());
            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getCaptainId()).isNull();
        }

        @Test
        @DisplayName("Should delete player without team")
        void shouldDeletePlayerWithoutTeam() {
            // Arrange
            testPlayer.setTeamId(null);

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));

            // Act
            PlayerResponse response = playerService.deletePlayer(100);

            // Assert
            assertThat(response).isNotNull();
            verify(playerRepository).deleteById(100);
            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when player not found for delete")
        void shouldThrowExceptionWhenPlayerNotFoundForDelete() {
            // Arrange
            when(playerRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> playerService.deletePlayer(999))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(playerRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("Should handle team not found during delete gracefully")
        void shouldHandleTeamNotFoundDuringDelete() {
            // Arrange
            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.empty());

            // Act
            PlayerResponse response = playerService.deletePlayer(100);

            // Assert
            assertThat(response).isNotNull();
            verify(playerRepository).deleteById(100);
            verify(teamRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("Should convert player to response with team name")
        void shouldConvertPlayerToResponseWithTeamName() {
            // Arrange
            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

            // Act
            PlayerResponse response = playerService.getPlayerById(100);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
        }

        @Test
        @DisplayName("Should handle team not found when converting to response")
        void shouldHandleTeamNotFoundInConversion() {
            // Arrange
            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.empty());

            // Act
            PlayerResponse response = playerService.getPlayerById(100);

            // Assert
            assertThat(response.getTeamName()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle case-insensitive team name search")
        void shouldHandleCaseInsensitiveTeamName() {
            // Arrange
            createRequest.setTeamName("MUMBAI INDIANS"); // Different case

            when(teamRepository.findByTeamNameIgnoreCase("MUMBAI INDIANS"))
                    .thenReturn(Optional.of(testTeam));
            when(sequenceGeneratorService.generateSequence(Player.SEQUENCE_NAME))
                    .thenReturn(101);
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(teamRepository.save(any(Team.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(playerRepository.findById(anyInt())).thenReturn(Optional.of(
                    Player.builder().id(1).name("Other").build()));

            // *** KEY FIX: Mock for convertToResponse() ***
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

            // Act
            PlayerResponse response = playerService.create(createRequest);

            // Assert
            assertThat(response.getTeamName()).isEqualTo("Mumbai Indians");
        }

        @Test
        @DisplayName("Should handle case-insensitive player name duplicate check")
        void shouldHandleCaseInsensitivePlayerNameCheck() {
            // Arrange
            createRequest.setName("PLAYER ONE"); // Same name different case

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(testTeam));
            when(playerRepository.findById(10)).thenReturn(Optional.of(
                    Player.builder().id(10).name("Player One").build()));

            // Act & Assert
            assertThatThrownBy(() -> playerService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should handle multiple fields update simultaneously")
        void shouldHandleMultipleFieldsUpdateSimultaneously() {
            // Arrange
            PlayerPatchRequest patchRequest = new PlayerPatchRequest();
            patchRequest.setName("Updated Name");
            patchRequest.setRole("Updated Role");
            patchRequest.setBattingStyle("Left-handed");
            patchRequest.setBowlingStyle("Left-arm spin");

            when(playerRepository.findById(100)).thenReturn(Optional.of(testPlayer));
            when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
            when(playerRepository.save(any(Player.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlayerResponse response = playerService.patchPlayer(100, patchRequest);

            // Assert
            assertThat(response.getName()).isEqualTo("Updated Name");
            assertThat(response.getRole()).isEqualTo("Updated Role");
            assertThat(response.getBattingStyle()).isEqualTo("Left-handed");
            assertThat(response.getBowlingStyle()).isEqualTo("Left-arm spin");
        }
    }
}
