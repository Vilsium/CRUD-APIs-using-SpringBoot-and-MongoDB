package com.example.tournament_data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime; // CHANGED from LocalDate  
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.tournament_data.dto.MatchCreateRequest;
import com.example.tournament_data.dto.MatchPatchRequest;
import com.example.tournament_data.dto.MatchResponse;
import com.example.tournament_data.dto.ResultCreateRequest;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Match;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Result;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.repository.MatchRepository;
import com.example.tournament_data.repository.PlayerRepository;
import com.example.tournament_data.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MatchService Tests")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @InjectMocks
    private MatchService matchService;

    @Captor
    private ArgumentCaptor<Match> matchCaptor;

    // Test data
    private Team team1;
    private Team team2;
    private Player player1;
    private Player player2;
    private Match testMatch;
    private MatchCreateRequest createRequest;
    private ResultCreateRequest resultRequest;
    private Result testResult;

    @BeforeEach
    void setUp() {
        // Initialize test teams
        team1 = Team.builder()
                .id(1)
                .teamName("Mumbai Indians")
                .homeGround("Wankhede Stadium")
                .playerIds(new ArrayList<>(Arrays.asList(10, 11, 12)))
                .build();

        team2 = Team.builder()
                .id(2)
                .teamName("Chennai Super Kings")
                .homeGround("MA Chidambaram Stadium")
                .playerIds(new ArrayList<>(Arrays.asList(20, 21, 22)))
                .build();

        // Initialize test players
        player1 = Player.builder()
                .id(10)
                .name("Rohit Sharma")
                .teamId(1)
                .role("Batsman")
                .build();

        player2 = Player.builder()
                .id(20)
                .name("MS Dhoni")
                .teamId(2)
                .role("Wicket-keeper")
                .build();

        // Initialize test result
        testResult = Result.builder()
                .winner(1)
                .margin("5 wickets")
                .manOfTheMatchId(10)
                .build();

        // Initialize test match - CHANGED to LocalDateTime
        testMatch = Match.builder()
                .id(100)
                .venue("Wankhede Stadium")
                .date(LocalDateTime.of(2024, 4, 15, 19, 30)) // CHANGED
                .firstTeam(1)
                .secondTeam(2)
                .status("SCHEDULED")
                .result(null)
                .build();

        // Initialize result request
        resultRequest = new ResultCreateRequest();
        resultRequest.setWinner("Mumbai Indians");
        resultRequest.setMargin("5 wickets");
        resultRequest.setManOfTheMatchName("Rohit Sharma");

        // Initialize create request - CHANGED to LocalDateTime
        createRequest = new MatchCreateRequest();
        createRequest.setVenue("Wankhede Stadium");
        createRequest.setDate(LocalDateTime.of(2024, 4, 15, 19, 30)); // CHANGED
        createRequest.setFirstTeamName("Mumbai Indians");
        createRequest.setSecondTeamName("Chennai Super Kings");
        createRequest.setStatus("SCHEDULED");
        createRequest.setResult(null);

        // Setup common mocks for convertToResponse
        lenient().when(teamRepository.findById(1)).thenReturn(Optional.of(team1));
        lenient().when(teamRepository.findById(2)).thenReturn(Optional.of(team2));
        lenient().when(playerRepository.findById(10)).thenReturn(Optional.of(player1));
        lenient().when(playerRepository.findById(20)).thenReturn(Optional.of(player2));
    }

    // ==================== CREATE TESTS ====================
    @Nested
    @DisplayName("Create Match Tests")
    class CreateMatchTests {

        @Test
        @DisplayName("Should create match successfully with SCHEDULED status")
        void shouldCreateMatchSuccessfully() {
            // Arrange
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(sequenceGeneratorService.generateSequence(Match.SEQUENCE_NAME))
                    .thenReturn(101);
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.create(createRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(101);
            assertThat(response.getVenue()).isEqualTo("Wankhede Stadium");
            assertThat(response.getFirstTeamName()).isEqualTo("Mumbai Indians");
            assertThat(response.getSecondTeamName()).isEqualTo("Chennai Super Kings");
            assertThat(response.getStatus()).isEqualTo("SCHEDULED");
            assertThat(response.getResult()).isNull();

            // Verify match was saved
            verify(matchRepository).save(matchCaptor.capture());
            Match savedMatch = matchCaptor.getValue();
            assertThat(savedMatch.getFirstTeam()).isEqualTo(1);
            assertThat(savedMatch.getSecondTeam()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should create match with COMPLETED status and result")
        void shouldCreateMatchWithCompletedStatusAndResult() {
            // Arrange
            createRequest.setStatus("COMPLETED");
            createRequest.setResult(resultRequest);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(player1));
            when(sequenceGeneratorService.generateSequence(Match.SEQUENCE_NAME))
                    .thenReturn(101);
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.create(createRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getWinner()).isEqualTo("Mumbai Indians");
            assertThat(response.getResult().getMargin()).isEqualTo("5 wickets");
            assertThat(response.getResult().getManOfTheMatch()).isEqualTo("Rohit Sharma");
        }

        @Test
        @DisplayName("Should throw exception when first team not found")
        void shouldThrowExceptionWhenFirstTeamNotFound() {
            // Arrange
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> matchService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Team not found with name: Mumbai Indians");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when second team not found")
        void shouldThrowExceptionWhenSecondTeamNotFound() {
            // Arrange
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> matchService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Team not found with name: Chennai Super Kings");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when both teams are same")
        void shouldThrowExceptionWhenBothTeamsAreSame() {
            // Arrange
            createRequest.setSecondTeamName("Mumbai Indians");

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));

            // Act & Assert
            assertThatThrownBy(() -> matchService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("First team and second team cannot be the same");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when COMPLETED status without result")
        void shouldThrowExceptionWhenCompletedWithoutResult() {
            // Arrange
            createRequest.setStatus("COMPLETED");
            createRequest.setResult(null);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));

            // Act & Assert
            assertThatThrownBy(() -> matchService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Result is required when match status is COMPLETED");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when SCHEDULED status with result")
        void shouldThrowExceptionWhenScheduledWithResult() {
            // Arrange
            createRequest.setStatus("SCHEDULED");
            createRequest.setResult(resultRequest);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));

            // Act & Assert
            assertThatThrownBy(() -> matchService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Result should not be provided when match status is SCHEDULED");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when winner is not a playing team")
        void shouldThrowExceptionWhenWinnerNotPlayingTeam() {
            // Arrange
            createRequest.setStatus("COMPLETED");
            resultRequest.setWinner("Royal Challengers Bangalore");
            createRequest.setResult(resultRequest);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));

            // Act & Assert
            assertThatThrownBy(() -> matchService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Winner must be one of the playing teams");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when man of the match not found")
        void shouldThrowExceptionWhenManOfTheMatchNotFound() {
            // Arrange
            createRequest.setStatus("COMPLETED");
            resultRequest.setManOfTheMatchName("Unknown Player");
            createRequest.setResult(resultRequest);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(playerRepository.findByNameIgnoreCase("Unknown Player"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> matchService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Player not found with name: Unknown Player");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when man of the match not from playing teams")
        void shouldThrowExceptionWhenManOfTheMatchNotFromPlayingTeams() {
            // Arrange
            Player outsidePlayer = Player.builder()
                    .id(99)
                    .name("Outside Player")
                    .teamId(99)
                    .build();

            createRequest.setStatus("COMPLETED");
            resultRequest.setManOfTheMatchName("Outside Player");
            createRequest.setResult(resultRequest);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(playerRepository.findByNameIgnoreCase("Outside Player"))
                    .thenReturn(Optional.of(outsidePlayer));

            // Act & Assert
            assertThatThrownBy(() -> matchService.create(createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Man of the match must belong to one of the playing teams");

            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create match with second team as winner")
        void shouldCreateMatchWithSecondTeamAsWinner() {
            // Arrange
            createRequest.setStatus("COMPLETED");
            resultRequest.setWinner("Chennai Super Kings");
            resultRequest.setManOfTheMatchName("MS Dhoni");
            createRequest.setResult(resultRequest);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(playerRepository.findByNameIgnoreCase("MS Dhoni"))
                    .thenReturn(Optional.of(player2));
            when(sequenceGeneratorService.generateSequence(Match.SEQUENCE_NAME))
                    .thenReturn(101);
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.create(createRequest);

            // Assert
            assertThat(response.getResult().getWinner()).isEqualTo("Chennai Super Kings");
            assertThat(response.getResult().getManOfTheMatch()).isEqualTo("MS Dhoni");
        }
    }

    // ==================== GET ALL TESTS ====================
    @Nested
    @DisplayName("Get All Matches Tests")
    class GetAllMatchesTests {

        @Test
        @DisplayName("Should return all matches")
        void shouldReturnAllMatches() {
            // Arrange
            Match match1 = Match.builder()
                    .id(1)
                    .venue("Venue 1")
                    .date(LocalDateTime.of(2024, 4, 15, 19, 30)) // CHANGED
                    .firstTeam(1)
                    .secondTeam(2)
                    .status("SCHEDULED")
                    .build();

            Match match2 = Match.builder()
                    .id(2)
                    .venue("Venue 2")
                    .date(LocalDateTime.of(2024, 4, 16, 19, 30)) // CHANGED
                    .firstTeam(1)
                    .secondTeam(2)
                    .status("COMPLETED")
                    .result(testResult)
                    .build();

            when(matchRepository.findAll()).thenReturn(Arrays.asList(match1, match2));

            // Act
            List<MatchResponse> responses = matchService.getAllMatches();

            // Assert
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getVenue()).isEqualTo("Venue 1");
            assertThat(responses.get(1).getVenue()).isEqualTo("Venue 2");
        }

        @Test
        @DisplayName("Should return empty list when no matches exist")
        void shouldReturnEmptyListWhenNoMatches() {
            // Arrange
            when(matchRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<MatchResponse> responses = matchService.getAllMatches();

            // Assert
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should return matches with resolved team names")
        void shouldReturnMatchesWithResolvedTeamNames() {
            // Arrange
            when(matchRepository.findAll()).thenReturn(Collections.singletonList(testMatch));

            // Act
            List<MatchResponse> responses = matchService.getAllMatches();

            // Assert
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getFirstTeamName()).isEqualTo("Mumbai Indians");
            assertThat(responses.get(0).getSecondTeamName()).isEqualTo("Chennai Super Kings");
        }
    }

    // ==================== GET BY ID TESTS ====================
    @Nested
    @DisplayName("Get Match By ID Tests")
    class GetMatchByIdTests {

        @Test
        @DisplayName("Should return match when found")
        void shouldReturnMatchWhenFound() {
            // Arrange
            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));

            // Act
            MatchResponse response = matchService.getMatchById(100);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100);
            assertThat(response.getVenue()).isEqualTo("Wankhede Stadium");
            assertThat(response.getFirstTeamName()).isEqualTo("Mumbai Indians");
            assertThat(response.getSecondTeamName()).isEqualTo("Chennai Super Kings");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when match not found")
        void shouldThrowExceptionWhenMatchNotFound() {
            // Arrange
            when(matchRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> matchService.getMatchById(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Match");
        }

        @Test
        @DisplayName("Should return match with result details")
        void shouldReturnMatchWithResultDetails() {
            // Arrange
            testMatch.setStatus("COMPLETED");
            testMatch.setResult(testResult);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));

            // Act
            MatchResponse response = matchService.getMatchById(100);

            // Assert
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getWinner()).isEqualTo("Mumbai Indians");
            assertThat(response.getResult().getMargin()).isEqualTo("5 wickets");
            assertThat(response.getResult().getManOfTheMatch()).isEqualTo("Rohit Sharma");
        }
    }

    // ==================== UPDATE TESTS ====================
    @Nested
    @DisplayName("Update Match Tests")
    class UpdateMatchTests {

        @Test
        @DisplayName("Should update match successfully")
        void shouldUpdateMatchSuccessfully() {
            // Arrange
            createRequest.setVenue("New Venue");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.updateMatch(100, createRequest);

            // Assert
            assertThat(response.getVenue()).isEqualTo("New Venue");
            verify(matchRepository).save(any(Match.class));
        }

        @Test
        @DisplayName("Should update match status to COMPLETED with result")
        void shouldUpdateMatchStatusToCompletedWithResult() {
            // Arrange
            createRequest.setStatus("COMPLETED");
            createRequest.setResult(resultRequest);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(player1));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.updateMatch(100, createRequest);

            // Assert
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getResult()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when match not found for update")
        void shouldThrowExceptionWhenMatchNotFoundForUpdate() {
            // Arrange
            when(matchRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> matchService.updateMatch(999, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when updating with same teams")
        void shouldThrowExceptionWhenUpdatingWithSameTeams() {
            // Arrange
            createRequest.setSecondTeamName("Mumbai Indians");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));

            // Act & Assert
            assertThatThrownBy(() -> matchService.updateMatch(100, createRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("First team and second team cannot be the same");
        }

        @Test
        @DisplayName("Should swap teams on update")
        void shouldSwapTeamsOnUpdate() {
            // Arrange
            createRequest.setFirstTeamName("Chennai Super Kings");
            createRequest.setSecondTeamName("Mumbai Indians");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.updateMatch(100, createRequest);

            // Assert
            assertThat(response.getFirstTeamName()).isEqualTo("Chennai Super Kings");
            assertThat(response.getSecondTeamName()).isEqualTo("Mumbai Indians");
        }
    }

    // ==================== PATCH TESTS ====================
    @Nested
    @DisplayName("Patch Match Tests")
    class PatchMatchTests {

        private MatchPatchRequest patchRequest;

        @BeforeEach
        void setUpPatchRequest() {
            patchRequest = new MatchPatchRequest();
        }

        @Test
        @DisplayName("Should patch only venue")
        void shouldPatchOnlyVenue() {
            // Arrange
            patchRequest.setVenue("New Venue");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getVenue()).isEqualTo("New Venue");
            assertThat(response.getStatus()).isEqualTo("SCHEDULED");
        }

        @Test
        @DisplayName("Should patch only date")
        void shouldPatchOnlyDate() {
            // Arrange
            LocalDateTime newDate = LocalDateTime.of(2024, 5, 20, 15, 0); // CHANGED
            patchRequest.setDate(newDate);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getDate()).isEqualTo(newDate);
        }

        @Test
        @DisplayName("Should patch first team")
        void shouldPatchFirstTeam() {
            // Arrange
            Team team3 = Team.builder()
                    .id(3)
                    .teamName("Royal Challengers Bangalore")
                    .build();

            patchRequest.setFirstTeamName("Royal Challengers Bangalore");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findByTeamNameIgnoreCase("Royal Challengers Bangalore"))
                    .thenReturn(Optional.of(team3));
            when(teamRepository.findById(3)).thenReturn(Optional.of(team3));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getFirstTeamName()).isEqualTo("Royal Challengers Bangalore");
            assertThat(response.getSecondTeamName()).isEqualTo("Chennai Super Kings");
        }

        @Test
        @DisplayName("Should patch second team")
        void shouldPatchSecondTeam() {
            // Arrange
            Team team3 = Team.builder()
                    .id(3)
                    .teamName("Royal Challengers Bangalore")
                    .build();

            patchRequest.setSecondTeamName("Royal Challengers Bangalore");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findByTeamNameIgnoreCase("Royal Challengers Bangalore"))
                    .thenReturn(Optional.of(team3));
            when(teamRepository.findById(3)).thenReturn(Optional.of(team3));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getFirstTeamName()).isEqualTo("Mumbai Indians");
            assertThat(response.getSecondTeamName()).isEqualTo("Royal Challengers Bangalore");
        }

        @Test
        @DisplayName("Should throw exception when patching first team to same as second")
        void shouldThrowExceptionWhenPatchingFirstTeamToSameAsSecond() {
            // Arrange
            patchRequest.setFirstTeamName("Chennai Super Kings");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));

            // Act & Assert
            assertThatThrownBy(() -> matchService.patchMatch(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("First team and second team cannot be the same");
        }

        @Test
        @DisplayName("Should throw exception when patching second team to same as first")
        void shouldThrowExceptionWhenPatchingSecondTeamToSameAsFirst() {
            // Arrange
            patchRequest.setSecondTeamName("Mumbai Indians");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));

            // Act & Assert
            assertThatThrownBy(() -> matchService.patchMatch(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("First team and second team cannot be the same");
        }

        @Test
        @DisplayName("Should patch status to COMPLETED with result")
        void shouldPatchStatusToCompletedWithResult() {
            // Arrange
            patchRequest.setStatus("COMPLETED");
            patchRequest.setResult(resultRequest);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(player1));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getWinner()).isEqualTo("Mumbai Indians");
        }

        @Test
        @DisplayName("Should throw exception when changing to COMPLETED without result")
        void shouldThrowExceptionWhenChangingToCompletedWithoutResult() {
            // Arrange
            patchRequest.setStatus("COMPLETED");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));

            // Act & Assert
            assertThatThrownBy(() -> matchService.patchMatch(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Result is required when changing match status to COMPLETED");
        }

        @Test
        @DisplayName("Should clear result when changing status to SCHEDULED")
        void shouldClearResultWhenChangingToScheduled() {
            // Arrange
            testMatch.setStatus("COMPLETED");
            testMatch.setResult(testResult);
            patchRequest.setStatus("SCHEDULED");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getStatus()).isEqualTo("SCHEDULED");
            assertThat(response.getResult()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when setting result for non-COMPLETED match")
        void shouldThrowExceptionWhenSettingResultForNonCompletedMatch() {
            // Arrange
            patchRequest.setResult(resultRequest);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));

            // Act & Assert
            assertThatThrownBy(() -> matchService.patchMatch(100, patchRequest))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Result can only be set for COMPLETED matches");
        }

        @Test
        @DisplayName("Should throw exception when match not found for patch")
        void shouldThrowExceptionWhenMatchNotFoundForPatch() {
            // Arrange
            when(matchRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> matchService.patchMatch(999, patchRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should ignore blank venue on patch")
        void shouldIgnoreBlankVenueOnPatch() {
            // Arrange
            patchRequest.setVenue("   ");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getVenue()).isEqualTo("Wankhede Stadium");
        }

        @Test
        @DisplayName("Should ignore blank status on patch")
        void shouldIgnoreBlankStatusOnPatch() {
            // Arrange
            patchRequest.setStatus("");

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getStatus()).isEqualTo("SCHEDULED");
        }

        @Test
        @DisplayName("Should update result for already COMPLETED match")
        void shouldUpdateResultForAlreadyCompletedMatch() {
            // Arrange
            testMatch.setStatus("COMPLETED");
            testMatch.setResult(testResult);

            ResultCreateRequest newResult = new ResultCreateRequest();
            newResult.setWinner("Chennai Super Kings");
            newResult.setMargin("10 runs");
            newResult.setManOfTheMatchName("MS Dhoni");

            patchRequest.setResult(newResult);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(playerRepository.findByNameIgnoreCase("MS Dhoni"))
                    .thenReturn(Optional.of(player2));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getResult().getWinner()).isEqualTo("Chennai Super Kings");
            assertThat(response.getResult().getMargin()).isEqualTo("10 runs");
            assertThat(response.getResult().getManOfTheMatch()).isEqualTo("MS Dhoni");
        }
    }

    // ==================== DELETE TESTS ====================
    @Nested
    @DisplayName("Delete Match Tests")
    class DeleteMatchTests {

        @Test
        @DisplayName("Should delete match successfully")
        void shouldDeleteMatchSuccessfully() {
            // Arrange
            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));

            // Act
            MatchResponse response = matchService.deleteMatch(100);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100);
            assertThat(response.getVenue()).isEqualTo("Wankhede Stadium");

            verify(matchRepository).deleteById(100);
        }

        @Test
        @DisplayName("Should delete match with result")
        void shouldDeleteMatchWithResult() {
            // Arrange
            testMatch.setStatus("COMPLETED");
            testMatch.setResult(testResult);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));

            // Act
            MatchResponse response = matchService.deleteMatch(100);

            // Assert
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getWinner()).isEqualTo("Mumbai Indians");

            verify(matchRepository).deleteById(100);
        }

        @Test
        @DisplayName("Should throw exception when match not found for delete")
        void shouldThrowExceptionWhenMatchNotFoundForDelete() {
            // Arrange
            when(matchRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> matchService.deleteMatch(999))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(matchRepository, never()).deleteById(anyInt());
        }
    }

    // ==================== HELPER METHOD TESTS ====================
    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("Should handle null team ID in getTeamName")
        void shouldHandleNullTeamIdInGetTeamName() {
            // Arrange
            testMatch.setFirstTeam(null);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));

            // Act
            MatchResponse response = matchService.getMatchById(100);

            // Assert
            assertThat(response.getFirstTeamName()).isNull();
        }

        @Test
        @DisplayName("Should handle team not found in getTeamName")
        void shouldHandleTeamNotFoundInGetTeamName() {
            // Arrange
            testMatch.setFirstTeam(999);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(teamRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            MatchResponse response = matchService.getMatchById(100);

            // Assert
            assertThat(response.getFirstTeamName()).isNull();
        }

        @Test
        @DisplayName("Should handle null player ID in getPlayerName")
        void shouldHandleNullPlayerIdInGetPlayerName() {
            // Arrange
            Result resultWithNullPlayer = Result.builder()
                    .winner(1)
                    .margin("5 wickets")
                    .manOfTheMatchId(null)
                    .build();

            testMatch.setStatus("COMPLETED");
            testMatch.setResult(resultWithNullPlayer);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));

            // Act
            MatchResponse response = matchService.getMatchById(100);

            // Assert
            assertThat(response.getResult().getManOfTheMatch()).isNull();
        }

        @Test
        @DisplayName("Should handle player not found in getPlayerName")
        void shouldHandlePlayerNotFoundInGetPlayerName() {
            // Arrange
            Result resultWithUnknownPlayer = Result.builder()
                    .winner(1)
                    .margin("5 wickets")
                    .manOfTheMatchId(999)
                    .build();

            testMatch.setStatus("COMPLETED");
            testMatch.setResult(resultWithUnknownPlayer);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(playerRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            MatchResponse response = matchService.getMatchById(100);

            // Assert
            assertThat(response.getResult().getManOfTheMatch()).isNull();
        }
    }

    // ==================== EDGE CASE TESTS ====================
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle case-insensitive team name in create")
        void shouldHandleCaseInsensitiveTeamNameInCreate() {
            // Arrange
            createRequest.setFirstTeamName("MUMBAI INDIANS");
            createRequest.setSecondTeamName("chennai super kings");

            when(teamRepository.findByTeamNameIgnoreCase("MUMBAI INDIANS"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("chennai super kings"))
                    .thenReturn(Optional.of(team2));
            when(sequenceGeneratorService.generateSequence(Match.SEQUENCE_NAME))
                    .thenReturn(101);
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.create(createRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getFirstTeamName()).isEqualTo("Mumbai Indians");
            assertThat(response.getSecondTeamName()).isEqualTo("Chennai Super Kings");
        }

        @Test
        @DisplayName("Should handle case-insensitive winner name in result")
        void shouldHandleCaseInsensitiveWinnerNameInResult() {
            // Arrange
            createRequest.setStatus("COMPLETED");
            resultRequest.setWinner("MUMBAI INDIANS");
            createRequest.setResult(resultRequest);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(player1));
            when(sequenceGeneratorService.generateSequence(Match.SEQUENCE_NAME))
                    .thenReturn(101);
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.create(createRequest);

            // Assert
            assertThat(response.getResult().getWinner()).isEqualTo("Mumbai Indians");
        }

        @Test
        @DisplayName("Should handle multiple patches simultaneously")
        void shouldHandleMultiplePatchesSimultaneously() {
            // Arrange
            MatchPatchRequest patchRequest = new MatchPatchRequest();
            patchRequest.setVenue("New Venue");
            patchRequest.setDate(LocalDateTime.of(2024, 6, 15, 20, 0)); // CHANGED
            patchRequest.setStatus("COMPLETED");
            patchRequest.setResult(resultRequest);

            when(matchRepository.findById(100)).thenReturn(Optional.of(testMatch));
            when(playerRepository.findByNameIgnoreCase("Rohit Sharma"))
                    .thenReturn(Optional.of(player1));
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.patchMatch(100, patchRequest);

            // Assert
            assertThat(response.getVenue()).isEqualTo("New Venue");
            assertThat(response.getDate()).isEqualTo(LocalDateTime.of(2024, 6, 15, 20, 0));
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getResult()).isNotNull();
        }

        @Test
        @DisplayName("Should handle man of match from second team")
        void shouldHandleManOfMatchFromSecondTeam() {
            // Arrange
            createRequest.setStatus("COMPLETED");
            resultRequest.setWinner("Mumbai Indians");
            resultRequest.setManOfTheMatchName("MS Dhoni");
            createRequest.setResult(resultRequest);

            when(teamRepository.findByTeamNameIgnoreCase("Mumbai Indians"))
                    .thenReturn(Optional.of(team1));
            when(teamRepository.findByTeamNameIgnoreCase("Chennai Super Kings"))
                    .thenReturn(Optional.of(team2));
            when(playerRepository.findByNameIgnoreCase("MS Dhoni"))
                    .thenReturn(Optional.of(player2));
            when(sequenceGeneratorService.generateSequence(Match.SEQUENCE_NAME))
                    .thenReturn(101);
            when(matchRepository.save(any(Match.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            MatchResponse response = matchService.create(createRequest);

            // Assert
            assertThat(response.getResult().getManOfTheMatch()).isEqualTo("MS Dhoni");
        }
    }
}
