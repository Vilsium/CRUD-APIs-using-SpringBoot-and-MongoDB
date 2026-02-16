package com.example.tournament_data.service;  
  
import java.util.List;  
import java.util.stream.Collectors;  
  
import org.springframework.stereotype.Service;  
  
import com.example.tournament_data.dto.MatchCreateRequest;  
import com.example.tournament_data.dto.MatchPatchRequest;  
import com.example.tournament_data.dto.MatchResponse;  
import com.example.tournament_data.dto.ResultCreateRequest;  
import com.example.tournament_data.dto.ResultResponse;  
import com.example.tournament_data.exception.InvalidRequestException;  
import com.example.tournament_data.exception.ResourceNotFoundException;  
import com.example.tournament_data.model.Match;  
import com.example.tournament_data.model.Player;  
import com.example.tournament_data.model.Result;  
import com.example.tournament_data.model.Team;  
import com.example.tournament_data.repository.MatchRepository;  
import com.example.tournament_data.repository.PlayerRepository;  
import com.example.tournament_data.repository.TeamRepository;  
  
import jakarta.validation.Valid;  
import lombok.RequiredArgsConstructor;  
  
@Service  
@RequiredArgsConstructor  
public class MatchService {  
  
    private final MatchRepository matchRepository;  
    private final TeamRepository teamRepository;  
    private final PlayerRepository playerRepository;  
    private final SequenceGeneratorService sequenceGeneratorService;  
  
    /**  
     * Create a new match  
     */  
    public MatchResponse create(@Valid MatchCreateRequest request) {  
        // Find first team by name  
        Team firstTeam = teamRepository.findByTeamNameIgnoreCase(request.getFirstTeamName())  
                .orElseThrow(() -> new InvalidRequestException(  
                        "firstTeamName",  
                        "Team not found with name: " + request.getFirstTeamName()));  
  
        // Find second team by name  
        Team secondTeam = teamRepository.findByTeamNameIgnoreCase(request.getSecondTeamName())  
                .orElseThrow(() -> new InvalidRequestException(  
                        "secondTeamName",  
                        "Team not found with name: " + request.getSecondTeamName()));  
  
        // Validate that both teams are different  
        if (firstTeam.getId().equals(secondTeam.getId())) {  
            throw new InvalidRequestException(  
                    "secondTeamName",  
                    "First team and second team cannot be the same");  
        }  
  
        // Validate result is provided when status is COMPLETED  
        if ("COMPLETED".equals(request.getStatus()) && request.getResult() == null) {  
            throw new InvalidRequestException(  
                    "result",  
                    "Result is required when match status is COMPLETED");  
        }  
  
        // Validate result is not provided when status is SCHEDULED  
        if ("SCHEDULED".equals(request.getStatus()) && request.getResult() != null) {  
            throw new InvalidRequestException(  
                    "result",  
                    "Result should not be provided when match status is SCHEDULED");  
        }  
  
        // Build Result if provided  
        Result result = null;  
        if (request.getResult() != null) {  
            result = buildResult(request.getResult(), firstTeam, secondTeam);  
        }  
  
        // Generate auto-incremented ID  
        Integer matchId = sequenceGeneratorService.generateSequence(Match.SEQUENCE_NAME);  
  
        // Build Match entity  
        Match match = Match.builder()  
                .id(matchId)  
                .venue(request.getVenue())  
                .date(request.getDate())  
                .firstTeam(firstTeam.getId())
                .secondTeam(secondTeam.getId())  
                .status(request.getStatus())  
                .result(result)  
                .build();  
  
        Match savedMatch = matchRepository.save(match);  
  
        return convertToResponse(savedMatch);  
    }  
  
    /**  
     * Get all matches  
     */  
    public List<MatchResponse> getAllMatches() {  
        return matchRepository.findAll()  
                .stream()  
                .map(this::convertToResponse)  
                .collect(Collectors.toList());  
    }  
  
    /**  
     * Get match by ID  
     */  
    public MatchResponse getMatchById(Integer id) {  
        Match match = matchRepository.findById(id)  
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", id));  
  
        return convertToResponse(match);  
    }  
  
    /**  
     * Update match (full update)  
     */  
    public MatchResponse updateMatch(Integer id, @Valid MatchCreateRequest request) {  
        // Find existing match  
        Match existingMatch = matchRepository.findById(id)  
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", id));  
  
        // Find first team by name  
        Team firstTeam = teamRepository.findByTeamNameIgnoreCase(request.getFirstTeamName())  
                .orElseThrow(() -> new InvalidRequestException(  
                        "firstTeamName",  
                        "Team not found with name: " + request.getFirstTeamName()));  
  
        // Find second team by name  
        Team secondTeam = teamRepository.findByTeamNameIgnoreCase(request.getSecondTeamName())  
                .orElseThrow(() -> new InvalidRequestException(  
                        "secondTeamName",  
                        "Team not found with name: " + request.getSecondTeamName()));  
  
        // Validate that both teams are different  
        if (firstTeam.getId().equals(secondTeam.getId())) {  
            throw new InvalidRequestException(  
                    "secondTeamName",  
                    "First team and second team cannot be the same");  
        }  
  
        // Validate result is provided when status is COMPLETED  
        if ("COMPLETED".equals(request.getStatus()) && request.getResult() == null) {  
            throw new InvalidRequestException(  
                    "result",  
                    "Result is required when match status is COMPLETED");  
        }  
  
        // Validate result is not provided when status is SCHEDULED  
        if ("SCHEDULED".equals(request.getStatus()) && request.getResult() != null) {  
            throw new InvalidRequestException(  
                    "result",  
                    "Result should not be provided when match status is SCHEDULED");  
        }  
  
        // Build Result if provided  
        Result result = null;  
        if (request.getResult() != null) {  
            result = buildResult(request.getResult(), firstTeam, secondTeam);  
        }  
  
        // Update fields  
        existingMatch.setVenue(request.getVenue());  
        existingMatch.setDate(request.getDate());  
        existingMatch.setFirstTeam(firstTeam.getId());  
        existingMatch.setSecondTeam(secondTeam.getId());  
        existingMatch.setStatus(request.getStatus());  
        existingMatch.setResult(result);  
  
        Match updatedMatch = matchRepository.save(existingMatch);  
  
        return convertToResponse(updatedMatch);  
    }  
  
    /**  
     * Patch match (partial update)  
     */  
    public MatchResponse patchMatch(Integer id, @Valid MatchPatchRequest request) {  
        // Find existing match  
        Match existingMatch = matchRepository.findById(id)  
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", id));  
  
        // Get current teams for validation  
        Team currentFirstTeam = teamRepository.findById(existingMatch.getFirstTeam()).orElse(null);  
        Team currentSecondTeam = teamRepository.findById(existingMatch.getSecondTeam()).orElse(null);  
  
        // Update venue if provided  
        if (request.getVenue() != null && !request.getVenue().isBlank()) {  
            existingMatch.setVenue(request.getVenue());  
        }  
  
        // Update date if provided  
        if (request.getDate() != null) {  
            existingMatch.setDate(request.getDate());  
        }  
  
        // Update first team if provided  
        if (request.getFirstTeamName() != null && !request.getFirstTeamName().isBlank()) {  
            Team firstTeam = teamRepository.findByTeamNameIgnoreCase(request.getFirstTeamName())  
                    .orElseThrow(() -> new InvalidRequestException(  
                            "firstTeamName",  
                            "Team not found with name: " + request.getFirstTeamName()));  
  
            // Validate not same as second team  
            if (firstTeam.getId().equals(existingMatch.getSecondTeam())) {  
                throw new InvalidRequestException(  
                        "firstTeamName",  
                        "First team and second team cannot be the same");  
            }  
  
            existingMatch.setFirstTeam(firstTeam.getId());  
            currentFirstTeam = firstTeam;  
        }  
  
        // Update second team if provided  
        if (request.getSecondTeamName() != null && !request.getSecondTeamName().isBlank()) {  
            Team secondTeam = teamRepository.findByTeamNameIgnoreCase(request.getSecondTeamName())  
                    .orElseThrow(() -> new InvalidRequestException(  
                            "secondTeamName",  
                            "Team not found with name: " + request.getSecondTeamName()));  
  
            // Validate not same as first team  
            if (secondTeam.getId().equals(existingMatch.getFirstTeam())) {  
                throw new InvalidRequestException(  
                        "secondTeamName",  
                        "First team and second team cannot be the same");  
            }  
  
            existingMatch.setSecondTeam(secondTeam.getId());  
            currentSecondTeam = secondTeam;  
        }  
  
        // Update status if provided  
        if (request.getStatus() != null && !request.getStatus().isBlank()) {  
            // If changing to COMPLETED, result must be provided  
            if ("COMPLETED".equals(request.getStatus()) &&  
                    existingMatch.getResult() == null &&  
                    request.getResult() == null) {  
                throw new InvalidRequestException(  
                        "result",  
                        "Result is required when changing match status to COMPLETED");  
            }  
  
            // If changing to SCHEDULED, clear result  
            if ("SCHEDULED".equals(request.getStatus())) {  
                existingMatch.setResult(null);  
            }  
  
            existingMatch.setStatus(request.getStatus());  
        }  
  
        // Update result if provided  
        if (request.getResult() != null) {  
            // Validate match is COMPLETED or being changed to COMPLETED  
            String effectiveStatus = request.getStatus() != null ? request.getStatus() : existingMatch.getStatus();  
            if (!"COMPLETED".equals(effectiveStatus)) {  
                throw new InvalidRequestException(  
                        "result",  
                        "Result can only be set for COMPLETED matches");  
            }  
  
            if (currentFirstTeam != null && currentSecondTeam != null) {  
                Result result = buildResult(request.getResult(), currentFirstTeam, currentSecondTeam);  
                existingMatch.setResult(result);  
            }  
        }  
  
        Match updatedMatch = matchRepository.save(existingMatch);  
  
        return convertToResponse(updatedMatch);  
    }  
  
    /**  
     * Delete match  
     */  
    public MatchResponse deleteMatch(Integer id) {  
        Match existingMatch = matchRepository.findById(id)  
                .orElseThrow(() -> new ResourceNotFoundException("Match", "id", id));  
  
        // Convert to response before deleting  
        MatchResponse response = convertToResponse(existingMatch);  
  
        matchRepository.deleteById(id);  
  
        return response;  
    }  
  
    /**  
     * Build Result entity from request  
     */  
    private Result buildResult(ResultCreateRequest request, Team firstTeam, Team secondTeam) {  
        // Validate winner team name matches one of the playing teams  
        Integer winnerId;  
        if (request.getWinner().equalsIgnoreCase(firstTeam.getTeamName())) {  
            winnerId = firstTeam.getId();  
        } else if (request.getWinner().equalsIgnoreCase(secondTeam.getTeamName())) {  
            winnerId = secondTeam.getId();  
        } else {  
            throw new InvalidRequestException(  
                    "result.winner",  
                    "Winner must be one of the playing teams: " + firstTeam.getTeamName() + " or "  
                            + secondTeam.getTeamName());  
        }  
  
        // Find man of the match by name  
        Player manOfTheMatch = playerRepository.findByNameIgnoreCase(request.getManOfTheMatchName())  
                .orElseThrow(() -> new InvalidRequestException(  
                        "result.manOfTheMatchName",  
                        "Player not found with name: " + request.getManOfTheMatchName()));  
  
        // Validate player belongs to one of the playing teams  
        if (!manOfTheMatch.getTeamId().equals(firstTeam.getId()) &&  
                !manOfTheMatch.getTeamId().equals(secondTeam.getId())) {  
            throw new InvalidRequestException(  
                    "result.manOfTheMatchName",  
                    "Man of the match must belong to one of the playing teams");  
        }  
  
        return Result.builder()  
                .winner(winnerId)  
                .margin(request.getMargin())  
                .manOfTheMatchId(manOfTheMatch.getId())  
                .build();  
    }  
  
    /**  
     * Convert Match entity to MatchResponse DTO  
     */  
    private MatchResponse convertToResponse(Match match) {  
        // Get first team name  
        String firstTeamName = getTeamName(match.getFirstTeam());  
  
        // Get second team name  
        String secondTeamName = getTeamName(match.getSecondTeam());  
  
        // Convert result if present  
        ResultResponse resultResponse = null;  
        if (match.getResult() != null) {  
            resultResponse = convertResultToResponse(match.getResult());  
        }  
  
        return MatchResponse.builder()  
                .id(match.getId())  
                .venue(match.getVenue())  
                .date(match.getDate())  
                .firstTeamName(firstTeamName)  
                .secondTeamName(secondTeamName)  
                .status(match.getStatus())  
                .result(resultResponse)  
                .build();  
    }  
  
    /**  
     * Convert Result entity to ResultResponse DTO  
     */  
    private ResultResponse convertResultToResponse(Result result) {  
        // Get winner team name  
        String winner = getTeamName(result.getWinner());  
  
        // Get man of the match name  
        String manOfTheMatch = getPlayerName(result.getManOfTheMatchId());  
  
        return ResultResponse.builder()  
                .winner(winner)  
                .margin(result.getMargin())  
                .manOfTheMatch(manOfTheMatch)  
                .build();  
    }  
  
    /**  
     * Get team name from team ID  
     */  
    private String getTeamName(Integer teamId) {  
        if (teamId == null) {  
            return null;  
        }  
        Team team = teamRepository.findById(teamId).orElse(null);  
        return team != null ? team.getTeamName() : null;  
    }  
  
    /**  
     * Get player name from player ID  
     */  
    private String getPlayerName(Integer playerId) {  
        if (playerId == null) {  
            return null;  
        }  
        Player player = playerRepository.findById(playerId).orElse(null);  
        return player != null ? player.getName() : null;  
    }  
}