package com.example.tournament_data.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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
import com.example.tournament_data.model.Match;
import com.example.tournament_data.model.Result;
import com.example.tournament_data.repository.MatchRepository;
import com.example.tournament_data.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService Unit Tests")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private MatchService matchService;

    private Match testMatch;
    private Result testResult;

    private static final String MATCH_ID = "64a1b2c3d4e5f6g7h8i9j0k1";
    private static final String FIRST_TEAM_ID = "64a1b2c3d4e5f6g7h8i9j0k2";
    private static final String SECOND_TEAM_ID = "64a1b2c3d4e5f6g7h8i9j0k3";
    private static final String PLAYER_ID = "64a1b2c3d4e5f6g7h8i9j0k4";

    @BeforeEach
    void setUp() {
        // Initialize test result
        testResult = Result.builder()
                .winner(FIRST_TEAM_ID)
                .margin("45 runs")
                .manOfTheMatchId(PLAYER_ID)
                .build();

        // Initialize test match
        testMatch = new Match();
        testMatch.setId(MATCH_ID);
        testMatch.setVenue("Wankhede Stadium");
        testMatch.setDate(LocalDateTime.of(2024, 5, 15, 19, 30));
        testMatch.setFirstTeam(FIRST_TEAM_ID);
        testMatch.setSecondTeam(SECOND_TEAM_ID);
        testMatch.setStatus("Scheduled");
        testMatch.setResult(null);
    }

    // ==================== HELPER METHODS ====================
    private Result createResult(String winner, String margin, String manOfTheMatch) {
        return Result.builder()
                .winner(winner)
                .margin(margin)
                .manOfTheMatchId(manOfTheMatch)
                .build();
    }

    private Match createCompletedMatch() {
        Match completedMatch = new Match();
        completedMatch.setId(MATCH_ID);
        completedMatch.setVenue("Wankhede Stadium");
        completedMatch.setDate(LocalDateTime.of(2024, 5, 15, 19, 30));
        completedMatch.setFirstTeam(FIRST_TEAM_ID);
        completedMatch.setSecondTeam(SECOND_TEAM_ID);
        completedMatch.setStatus("Completed");
        completedMatch.setResult(testResult);
        return completedMatch;
    }

    // ==================== CREATE TESTS ====================
    @Nested
    @DisplayName("Create Match Tests")
    class CreateMatchTests {

        @Test
        @DisplayName("Should create match successfully with valid teams")
        void create_WithValidTeams_ShouldReturnSavedMatch() {
            // Arrange
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            Match result = matchService.create(testMatch);

            // Assert
            assertNotNull(result);
            assertEquals(MATCH_ID, result.getId());
            assertEquals("Wankhede Stadium", result.getVenue());
            assertEquals(FIRST_TEAM_ID, result.getFirstTeam());
            assertEquals(SECOND_TEAM_ID, result.getSecondTeam());
            assertNull(result.getResult());
            verify(teamRepository, times(1)).existsById(FIRST_TEAM_ID);
            verify(teamRepository, times(1)).existsById(SECOND_TEAM_ID);
            verify(matchRepository, times(1)).save(testMatch);
        }

        @Test
        @DisplayName("Should create completed match with result")
        void create_WithResult_ShouldReturnSavedMatchWithResult() {
            // Arrange
            Match completedMatch = createCompletedMatch();

            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(completedMatch);

            // Act
            Match result = matchService.create(completedMatch);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getResult());
            assertEquals(FIRST_TEAM_ID, result.getResult().getWinner());
            assertEquals("45 runs", result.getResult().getMargin());
            assertEquals(PLAYER_ID, result.getResult().getManOfTheMatchId());
            assertEquals("Completed", result.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when first team not found")
        void create_WithInvalidFirstTeam_ShouldThrowException() {
            // Arrange
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(false);

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> matchService.create(testMatch));

            assertTrue(exception.getMessage().contains("firstTeam") ||
                    exception.getMessage().contains(FIRST_TEAM_ID));
            verify(teamRepository, times(1)).existsById(FIRST_TEAM_ID);
            verify(teamRepository, never()).existsById(SECOND_TEAM_ID);
            verify(matchRepository, never()).save(any(Match.class));
        }

        @Test
        @DisplayName("Should throw exception when second team not found")
        void create_WithInvalidSecondTeam_ShouldThrowException() {
            // Arrange
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(false);

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> matchService.create(testMatch));

            assertTrue(exception.getMessage().contains("secondTeam") ||
                    exception.getMessage().contains(SECOND_TEAM_ID));
            verify(matchRepository, never()).save(any(Match.class));
        }

        @Test
        @DisplayName("Should create match with different margin types")
        void create_WithDifferentMarginTypes_ShouldCreateSuccessfully() {
            // Arrange
            Result wicketsResult = createResult(SECOND_TEAM_ID, "5 wickets", PLAYER_ID);
            testMatch.setResult(wicketsResult);
            testMatch.setStatus("Completed");

            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            Match result = matchService.create(testMatch);

            // Assert
            assertNotNull(result);
            assertEquals("5 wickets", result.getResult().getMargin());
        }
    }

    // ==================== GET ALL MATCHES TESTS ====================
    @Nested
    @DisplayName("Get All Matches Tests")
    class GetAllMatchesTests {

        @Test
        @DisplayName("Should return all matches including completed ones with results")
        void getAllMatches_ShouldReturnAllMatchesWithResults() {
            // Arrange
            Match completedMatch = createCompletedMatch();

            Match scheduledMatch = new Match();
            scheduledMatch.setId("match456");
            scheduledMatch.setVenue("Eden Gardens");
            scheduledMatch.setFirstTeam("team3");
            scheduledMatch.setSecondTeam("team4");
            scheduledMatch.setStatus("Scheduled");
            scheduledMatch.setResult(null);

            List<Match> matches = Arrays.asList(completedMatch, scheduledMatch);
            when(matchRepository.findAll()).thenReturn(matches);

            // Act
            List<Match> result = matchService.getAllMatches();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            // Verify completed match has result
            assertNotNull(result.get(0).getResult());
            assertEquals(FIRST_TEAM_ID, result.get(0).getResult().getWinner());

            // Verify scheduled match has no result
            assertNull(result.get(1).getResult());

            verify(matchRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no matches exist")
        void getAllMatches_WhenNoMatches_ShouldReturnEmptyList() {
            // Arrange
            when(matchRepository.findAll()).thenReturn(new ArrayList<>());

            // Act
            List<Match> result = matchService.getAllMatches();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(matchRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return matches with various result margins")
        void getAllMatches_WithVariousResults_ShouldReturnAll() {
            // Arrange
            Match match1 = createCompletedMatch();
            match1.getResult().setMargin("50 runs");

            Match match2 = createCompletedMatch();
            match2.setId("match2");
            match2.setResult(createResult(SECOND_TEAM_ID, "3 wickets", PLAYER_ID));

            Match match3 = createCompletedMatch();
            match3.setId("match3");
            match3.setResult(createResult(FIRST_TEAM_ID, "Super Over", PLAYER_ID));

            when(matchRepository.findAll()).thenReturn(Arrays.asList(match1, match2, match3));

            // Act
            List<Match> result = matchService.getAllMatches();

            // Assert
            assertEquals(3, result.size());
            assertEquals("50 runs", result.get(0).getResult().getMargin());
            assertEquals("3 wickets", result.get(1).getResult().getMargin());
            assertEquals("Super Over", result.get(2).getResult().getMargin());
        }
    }

    // ==================== GET MATCH BY ID TESTS ====================
    @Nested
    @DisplayName("Get Match By ID Tests")
    class GetMatchByIdTests {

        @Test
        @DisplayName("Should return match with result when found")
        void getMatchById_WhenExistsWithResult_ShouldReturnMatchWithResult() {
            // Arrange
            Match completedMatch = createCompletedMatch();
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(completedMatch));

            // Act
            Match result = matchService.getMatchById(MATCH_ID);

            // Assert
            assertNotNull(result);
            assertEquals(MATCH_ID, result.getId());
            assertNotNull(result.getResult());
            assertEquals(FIRST_TEAM_ID, result.getResult().getWinner());
            assertEquals("45 runs", result.getResult().getMargin());
            assertEquals(PLAYER_ID, result.getResult().getManOfTheMatchId());
            verify(matchRepository, times(1)).findById(MATCH_ID);
        }

        @Test
        @DisplayName("Should return match without result when scheduled")
        void getMatchById_WhenScheduled_ShouldReturnMatchWithoutResult() {
            // Arrange
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));

            // Act
            Match result = matchService.getMatchById(MATCH_ID);

            // Assert
            assertNotNull(result);
            assertEquals("Scheduled", result.getStatus());
            assertNull(result.getResult());
        }

        @Test
        @DisplayName("Should throw exception when match not found")
        void getMatchById_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> matchService.getMatchById(MATCH_ID));

            assertTrue(exception.getMessage().contains("Match") ||
                    exception.getMessage().contains(MATCH_ID));
        }
    }

    // ==================== UPDATE MATCH TESTS ====================
    @Nested
    @DisplayName("Update Match Tests")
    class UpdateMatchTests {

        @Test
        @DisplayName("Should update match with new result")
        void updateMatch_WithResult_ShouldReturnUpdatedMatchWithResult() {
            // Arrange
            Match updatedDetails = createCompletedMatch();
            updatedDetails.setVenue("Eden Gardens");
            updatedDetails.setResult(createResult(SECOND_TEAM_ID, "7 wickets", PLAYER_ID));

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.updateMatch(MATCH_ID, updatedDetails);

            // Assert
            assertNotNull(result);
            assertEquals("Eden Gardens", result.getVenue());
            assertEquals("Completed", result.getStatus());
            assertNotNull(result.getResult());
            assertEquals(SECOND_TEAM_ID, result.getResult().getWinner());
            assertEquals("7 wickets", result.getResult().getMargin());
        }

        @Test
        @DisplayName("Should update match from scheduled to completed")
        void updateMatch_FromScheduledToCompleted_ShouldUpdateWithResult() {
            // Arrange
            Match completedDetails = new Match();
            completedDetails.setVenue("Wankhede Stadium");
            completedDetails.setDate(testMatch.getDate());
            completedDetails.setFirstTeam(FIRST_TEAM_ID);
            completedDetails.setSecondTeam(SECOND_TEAM_ID);
            completedDetails.setStatus("Completed");
            completedDetails.setResult(testResult);

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.updateMatch(MATCH_ID, completedDetails);

            // Assert
            assertEquals("Completed", result.getStatus());
            assertNotNull(result.getResult());
            assertEquals(FIRST_TEAM_ID, result.getResult().getWinner());
        }

        @Test
        @DisplayName("Should update match and clear result")
        void updateMatch_ClearResult_ShouldReturnMatchWithoutResult() {
            // Arrange
            Match existingCompletedMatch = createCompletedMatch();

            Match updateDetails = new Match();
            updateDetails.setVenue("New Venue");
            updateDetails.setDate(LocalDateTime.now());
            updateDetails.setFirstTeam(FIRST_TEAM_ID);
            updateDetails.setSecondTeam(SECOND_TEAM_ID);
            updateDetails.setStatus("Cancelled");
            updateDetails.setResult(null); // Clear result

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(existingCompletedMatch));
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.updateMatch(MATCH_ID, updateDetails);

            // Assert
            assertEquals("Cancelled", result.getStatus());
            assertNull(result.getResult());
        }

        @Test
        @DisplayName("Should throw exception when match not found")
        void updateMatch_WhenMatchNotExists_ShouldThrowException() {
            // Arrange
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> matchService.updateMatch(MATCH_ID, testMatch));

            verify(matchRepository, never()).save(any(Match.class));
        }

        @Test
        @DisplayName("Should throw exception when first team not found during update")
        void updateMatch_WithInvalidFirstTeam_ShouldThrowException() {
            // Arrange
            Match updatedDetails = createCompletedMatch();
            updatedDetails.setFirstTeam("invalidTeamId123456789012");

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(teamRepository.existsById("invalidTeamId123456789012")).thenReturn(false);

            // Act & Assert
            assertThrows(
                    InvalidRequestException.class,
                    () -> matchService.updateMatch(MATCH_ID, updatedDetails));

            verify(matchRepository, never()).save(any(Match.class));
        }
    }

    // ==================== PATCH MATCH TESTS ====================
    @Nested
    @DisplayName("Patch Match Tests")
    class PatchMatchTests {

        @Test
        @DisplayName("Should patch only result field")
        void patchMatch_WithOnlyResult_ShouldUpdateOnlyResult() {
            // Arrange
            Match patchDetails = new Match();
            patchDetails.setResult(testResult);

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.patchMatch(MATCH_ID, patchDetails);

            // Assert
            assertNotNull(result.getResult());
            assertEquals(FIRST_TEAM_ID, result.getResult().getWinner());
            assertEquals("45 runs", result.getResult().getMargin());
            assertEquals(PLAYER_ID, result.getResult().getManOfTheMatchId());
            assertEquals("Wankhede Stadium", result.getVenue()); // Unchanged
            assertEquals("Scheduled", result.getStatus()); // Unchanged
        }

        @Test
        @DisplayName("Should patch result with different winner")
        void patchMatch_WithDifferentWinner_ShouldUpdateResult() {
            // Arrange
            Result newResult = createResult(SECOND_TEAM_ID, "3 wickets", PLAYER_ID);
            Match patchDetails = new Match();
            patchDetails.setResult(newResult);
            patchDetails.setStatus("Completed");

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.patchMatch(MATCH_ID, patchDetails);

            // Assert
            assertEquals(SECOND_TEAM_ID, result.getResult().getWinner());
            assertEquals("3 wickets", result.getResult().getMargin());
            assertEquals("Completed", result.getStatus());
        }

        @Test
        @DisplayName("Should patch only venue field")
        void patchMatch_WithOnlyVenue_ShouldUpdateOnlyVenue() {
            // Arrange
            Match patchDetails = new Match();
            patchDetails.setVenue("New Venue");

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.patchMatch(MATCH_ID, patchDetails);

            // Assert
            assertEquals("New Venue", result.getVenue());
            assertEquals(FIRST_TEAM_ID, result.getFirstTeam()); // Unchanged
            assertEquals(SECOND_TEAM_ID, result.getSecondTeam()); // Unchanged
            assertNull(result.getResult()); // Unchanged
            verify(teamRepository, never()).existsById(anyString());
        }

        @Test
        @DisplayName("Should patch only status field")
        void patchMatch_WithOnlyStatus_ShouldUpdateOnlyStatus() {
            // Arrange
            Match patchDetails = new Match();
            patchDetails.setStatus("In Progress");

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.patchMatch(MATCH_ID, patchDetails);

            // Assert
            assertEquals("In Progress", result.getStatus());
            assertEquals("Wankhede Stadium", result.getVenue()); // Unchanged
        }

        @Test
        @DisplayName("Should patch first team with validation")
        void patchMatch_WithFirstTeam_ShouldValidateAndUpdate() {
            // Arrange
            String newTeamId = "64a1b2c3d4e5f6g7h8i9j0k9";
            Match patchDetails = new Match();
            patchDetails.setFirstTeam(newTeamId);

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(teamRepository.existsById(newTeamId)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.patchMatch(MATCH_ID, patchDetails);

            // Assert
            assertEquals(newTeamId, result.getFirstTeam());
            assertEquals(SECOND_TEAM_ID, result.getSecondTeam()); // Unchanged
            verify(teamRepository, times(1)).existsById(newTeamId);
        }

        @Test
        @DisplayName("Should throw exception when patching with invalid first team")
        void patchMatch_WithInvalidFirstTeam_ShouldThrowException() {
            // Arrange
            String invalidTeamId = "invalidTeamId123456789012";
            Match patchDetails = new Match();
            patchDetails.setFirstTeam(invalidTeamId);

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(teamRepository.existsById(invalidTeamId)).thenReturn(false);

            // Act & Assert
            InvalidRequestException exception = assertThrows(
                    InvalidRequestException.class,
                    () -> matchService.patchMatch(MATCH_ID, patchDetails));

            assertTrue(exception.getMessage().contains(invalidTeamId));
            verify(matchRepository, never()).save(any(Match.class));
        }

        @Test
        @DisplayName("Should patch multiple fields including result")
        void patchMatch_WithMultipleFieldsAndResult_ShouldUpdateAll() {
            // Arrange
            Match patchDetails = new Match();
            patchDetails.setVenue("Updated Venue");
            patchDetails.setStatus("Completed");
            patchDetails.setResult(createResult(FIRST_TEAM_ID, "100 runs", PLAYER_ID));

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.patchMatch(MATCH_ID, patchDetails);

            // Assert
            assertEquals("Updated Venue", result.getVenue());
            assertEquals("Completed", result.getStatus());
            assertNotNull(result.getResult());
            assertEquals("100 runs", result.getResult().getMargin());
        }

        @Test
        @DisplayName("Should throw exception when match not found")
        void patchMatch_WhenMatchNotExists_ShouldThrowException() {
            // Arrange
            Match patchDetails = new Match();
            patchDetails.setVenue("New Venue");

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> matchService.patchMatch(MATCH_ID, patchDetails));

            verify(matchRepository, never()).save(any(Match.class));
        }

        @Test
        @DisplayName("Should patch existing match with result to update result")
        void patchMatch_UpdateExistingResult_ShouldUpdateResult() {
            // Arrange
            Match existingMatchWithResult = createCompletedMatch();

            Result updatedResult = createResult(SECOND_TEAM_ID, "DLS Method", PLAYER_ID);
            Match patchDetails = new Match();
            patchDetails.setResult(updatedResult);

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(existingMatchWithResult));
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.patchMatch(MATCH_ID, patchDetails);

            // Assert
            assertEquals(SECOND_TEAM_ID, result.getResult().getWinner());
            assertEquals("DLS Method", result.getResult().getMargin());
        }
    }

    // ==================== DELETE MATCH TESTS ====================
    @Nested
    @DisplayName("Delete Match Tests")
    class DeleteMatchTests {

        @Test
        @DisplayName("Should delete match and return it with result")
        void deleteMatch_WithResult_ShouldReturnDeletedMatchWithResult() {
            // Arrange
            Match completedMatch = createCompletedMatch();
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(completedMatch));
            doNothing().when(matchRepository).deleteById(MATCH_ID);

            // Act
            Match result = matchService.deleteMatch(MATCH_ID);

            // Assert
            assertNotNull(result);
            assertEquals(MATCH_ID, result.getId());
            assertNotNull(result.getResult());
            assertEquals(FIRST_TEAM_ID, result.getResult().getWinner());
            assertEquals("45 runs", result.getResult().getMargin());
            assertEquals(PLAYER_ID, result.getResult().getManOfTheMatchId());
            verify(matchRepository, times(1)).deleteById(MATCH_ID);
        }

        @Test
        @DisplayName("Should delete scheduled match without result")
        void deleteMatch_WithoutResult_ShouldReturnDeletedMatch() {
            // Arrange
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(testMatch));
            doNothing().when(matchRepository).deleteById(MATCH_ID);

            // Act
            Match result = matchService.deleteMatch(MATCH_ID);

            // Assert
            assertNotNull(result);
            assertEquals("Scheduled", result.getStatus());
            assertNull(result.getResult());
            verify(matchRepository, times(1)).deleteById(MATCH_ID);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent match")
        void deleteMatch_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> matchService.deleteMatch(MATCH_ID));

            verify(matchRepository, never()).deleteById(anyString());
        }
    }

    // ==================== RESULT-SPECIFIC TESTS ====================
    @Nested
    @DisplayName("Result-Specific Tests")
    class ResultSpecificTests {

        @Test
        @DisplayName("Should handle result with runs margin")
        void result_WithRunsMargin_ShouldBeHandledCorrectly() {
            // Arrange
            Result runsResult = createResult(FIRST_TEAM_ID, "156 runs", PLAYER_ID);
            testMatch.setResult(runsResult);
            testMatch.setStatus("Completed");

            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            Match result = matchService.create(testMatch);

            // Assert
            assertEquals("156 runs", result.getResult().getMargin());
        }

        @Test
        @DisplayName("Should handle result with wickets margin")
        void result_WithWicketsMargin_ShouldBeHandledCorrectly() {
            // Arrange
            Result wicketsResult = createResult(SECOND_TEAM_ID, "8 wickets", PLAYER_ID);
            testMatch.setResult(wicketsResult);
            testMatch.setStatus("Completed");

            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            Match result = matchService.create(testMatch);

            // Assert
            assertEquals("8 wickets", result.getResult().getMargin());
            assertEquals(SECOND_TEAM_ID, result.getResult().getWinner());
        }

        @Test
        @DisplayName("Should handle result with Super Over")
        void result_WithSuperOver_ShouldBeHandledCorrectly() {
            // Arrange
            Result superOverResult = createResult(FIRST_TEAM_ID, "Super Over", PLAYER_ID);
            testMatch.setResult(superOverResult);
            testMatch.setStatus("Completed");

            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            Match result = matchService.create(testMatch);

            // Assert
            assertEquals("Super Over", result.getResult().getMargin());
        }

        @Test
        @DisplayName("Should handle result with DLS Method")
        void result_WithDLSMethod_ShouldBeHandledCorrectly() {
            // Arrange
            Result dlsResult = createResult(SECOND_TEAM_ID, "DLS Method", PLAYER_ID);
            testMatch.setResult(dlsResult);
            testMatch.setStatus("Completed");

            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            Match result = matchService.create(testMatch);

            // Assert
            assertEquals("DLS Method", result.getResult().getMargin());
        }

        @Test
        @DisplayName("Should verify all Result fields are preserved")
        void result_AllFields_ShouldBePreserved() {
            // Arrange
            String winnerId = "64a1b2c3d4e5f6g7h8i9j0k5";
            String motmId = "64a1b2c3d4e5f6g7h8i9j0k6";
            Result fullResult = Result.builder()
                    .winner(winnerId)
                    .margin("25 runs")
                    .manOfTheMatchId(motmId)
                    .build();

            testMatch.setResult(fullResult);
            testMatch.setStatus("Completed");

            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            Match result = matchService.create(testMatch);

            // Assert
            assertNotNull(result.getResult());
            assertEquals(winnerId, result.getResult().getWinner());
            assertEquals("25 runs", result.getResult().getMargin());
            assertEquals(motmId, result.getResult().getManOfTheMatchId());
        }
    }

    // ==================== EDGE CASES TESTS ====================
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle match with null result")
        void create_WithNullResult_ShouldCreateSuccessfully() {
            // Arrange
            testMatch.setResult(null);
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            Match result = matchService.create(testMatch);

            // Assert
            assertNotNull(result);
            assertNull(result.getResult());
        }

        @Test
        @DisplayName("Should handle empty patch - no changes")
        void patchMatch_WithNoFields_ShouldReturnUnchangedMatch() {
            // Arrange
            Match existingMatchWithResult = createCompletedMatch();
            Match emptyPatch = new Match();

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(existingMatchWithResult));
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.patchMatch(MATCH_ID, emptyPatch);

            // Assert
            assertEquals("Wankhede Stadium", result.getVenue());
            assertEquals(FIRST_TEAM_ID, result.getFirstTeam());
            assertNotNull(result.getResult());
            assertEquals(FIRST_TEAM_ID, result.getResult().getWinner());
        }

        @Test
        @DisplayName("Should verify repository interaction order")
        void create_ShouldValidateTeamsBeforeSaving() {
            // Arrange
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            matchService.create(testMatch);

            // Assert - Verify order
            var inOrder = inOrder(teamRepository, matchRepository);
            inOrder.verify(teamRepository).existsById(FIRST_TEAM_ID);
            inOrder.verify(teamRepository).existsById(SECOND_TEAM_ID);
            inOrder.verify(matchRepository).save(any(Match.class));
        }

        @Test
        @DisplayName("Should handle transition from completed to cancelled")
        void updateMatch_FromCompletedToCancelled_ShouldClearResult() {
            // Arrange
            Match existingCompletedMatch = createCompletedMatch();

            Match cancelledDetails = new Match();
            cancelledDetails.setVenue("Wankhede Stadium");
            cancelledDetails.setDate(LocalDateTime.now());
            cancelledDetails.setFirstTeam(FIRST_TEAM_ID);
            cancelledDetails.setSecondTeam(SECOND_TEAM_ID);
            cancelledDetails.setStatus("Cancelled");
            cancelledDetails.setResult(null);

            when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(existingCompletedMatch));
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Match result = matchService.updateMatch(MATCH_ID, cancelledDetails);

            // Assert
            assertEquals("Cancelled", result.getStatus());
            assertNull(result.getResult());
        }
    }

    // ==================== VERIFICATION TESTS ====================
    @Nested
    @DisplayName("Repository Interaction Verification Tests")
    class VerificationTests {

        @Test
        @DisplayName("Should not interact with teamRepository in getAllMatches")
        void getAllMatches_ShouldNotInteractWithTeamRepository() {
            // Arrange
            when(matchRepository.findAll()).thenReturn(Arrays.asList(testMatch));

            // Act
            matchService.getAllMatches();

            // Assert
            verifyNoInteractions(teamRepository);
        }

        @Test
        @DisplayName("Should call save exactly once in successful create")
        void create_Success_ShouldCallSaveOnce() {
            // Arrange
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(testMatch)).thenReturn(testMatch);

            // Act
            matchService.create(testMatch);

            // Assert
            verify(matchRepository, times(1)).save(testMatch);
        }

        @Test
        @DisplayName("Should validate both teams in create")
        void create_ShouldValidateBothTeams() {
            // Arrange
            when(teamRepository.existsById(FIRST_TEAM_ID)).thenReturn(true);
            when(teamRepository.existsById(SECOND_TEAM_ID)).thenReturn(true);
            when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

            // Act
            matchService.create(testMatch);

            // Assert
            verify(teamRepository, times(1)).existsById(FIRST_TEAM_ID);
            verify(teamRepository, times(1)).existsById(SECOND_TEAM_ID);
        }
    }
}
