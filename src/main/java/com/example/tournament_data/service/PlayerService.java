package com.example.tournament_data.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    /**
     * Create a new player
     */
    public PlayerResponse create(@Valid PlayerCreateRequest request) {
        // Team name is required - find team by name
        Team team = teamRepository.findByTeamNameIgnoreCase(request.getTeamName())
                .orElseThrow(() -> new InvalidRequestException(
                        "teamName",
                        "Team not found with name: " + request.getTeamName()));

        // Check if player with same name already exists in this team
        for (Integer existingPlayerId : team.getPlayerIds()) {
            Player existingPlayer = playerRepository.findById(existingPlayerId).orElse(null);
            if (existingPlayer != null && existingPlayer.getName().equalsIgnoreCase(request.getName())) {
                throw new InvalidRequestException(
                        "name",
                        "Player with name '" + request.getName() + "' already exists in team '" + team.getTeamName()
                                + "'");
            }
        }

        // Check if team has reached maximum player limit (25 players)
        if (team.getPlayerIds().size() >= 25) {
            throw new InvalidRequestException(
                    "teamName",
                    "Team '" + team.getTeamName() + "' has reached maximum player limit of 25");
        }

        // Generate auto-incremented ID
        Integer playerId = sequenceGeneratorService.generateSequence(Player.SEQUENCE_NAME);

        // Build Stats with defaults if not provided
        Stats stats = buildStats(request.getStats());

        // Create Player entity using Builder
        Player player = Player.builder()
                .id(playerId)
                .name(request.getName())
                .teamId(team.getId())
                .role(request.getRole())
                .battingStyle(request.getBattingStyle())
                .bowlingStyle(request.getBowlingStyle())
                .stats(stats)
                .build();

        // Save the player
        Player savedPlayer = playerRepository.save(player);

        // Add player to team's playerIds list
        team.getPlayerIds().add(savedPlayer.getId());
        teamRepository.save(team);

        return convertToResponse(savedPlayer);
    }

    /**
     * Get all players
     */
    public List<PlayerResponse> getAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get player by ID
     */
    public PlayerResponse getPlayerById(Integer id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        return convertToResponse(player);
    }

    /**
     * Update player (full update)
     */
    public PlayerResponse updatePlayer(Integer id, @Valid PlayerCreateRequest request) {
        // Find existing player
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        // Find team by name
        Team newTeam = teamRepository.findByTeamNameIgnoreCase(request.getTeamName())
                .orElseThrow(() -> new InvalidRequestException(
                        "teamName",
                        "Team not found with name: " + request.getTeamName()));

        // Check if team is changing
        Integer oldTeamId = existingPlayer.getTeamId();
        Integer newTeamId = newTeam.getId();

        // If team is changing, handle the transfer
        if (oldTeamId != null && !oldTeamId.equals(newTeamId)) {
            // Remove player from old team
            teamRepository.findById(oldTeamId).ifPresent(oldTeam -> {
                oldTeam.getPlayerIds().remove(id);
                // If player was captain, clear captain
                if (id.equals(oldTeam.getCaptainId())) {
                    oldTeam.setCaptainId(null);
                }
                teamRepository.save(oldTeam);
            });

            // Add player to new team
            if (!newTeam.getPlayerIds().contains(id)) {
                newTeam.getPlayerIds().add(id);
                teamRepository.save(newTeam);
            }
        } else if (oldTeamId == null) {
            // Player had no team, add to new team
            if (!newTeam.getPlayerIds().contains(id)) {
                newTeam.getPlayerIds().add(id);
                teamRepository.save(newTeam);
            }
        }

        // Check for duplicate name in new team (excluding current player)
        for (Integer existingPlayerId : newTeam.getPlayerIds()) {
            if (!existingPlayerId.equals(id)) {
                Player teamPlayer = playerRepository.findById(existingPlayerId).orElse(null);
                if (teamPlayer != null && teamPlayer.getName().equalsIgnoreCase(request.getName())) {
                    throw new InvalidRequestException(
                            "name",
                            "Player with name '" + request.getName() + "' already exists in team '"
                                    + newTeam.getTeamName() + "'");
                }
            }
        }

        // Build Stats
        Stats stats = buildStats(request.getStats());

        // Update fields
        existingPlayer.setName(request.getName());
        existingPlayer.setTeamId(newTeamId);
        existingPlayer.setRole(request.getRole());
        existingPlayer.setBattingStyle(request.getBattingStyle());
        existingPlayer.setBowlingStyle(request.getBowlingStyle());
        existingPlayer.setStats(stats);

        // Save and return
        Player updatedPlayer = playerRepository.save(existingPlayer);
        return convertToResponse(updatedPlayer);
    }

    /**
     * Patch player (partial update)
     */
    public PlayerResponse patchPlayer(Integer id, PlayerPatchRequest request) {
        // Find existing player
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            // Check for duplicate name in current team
            if (existingPlayer.getTeamId() != null) {
                Team currentTeam = teamRepository.findById(existingPlayer.getTeamId()).orElse(null);
                if (currentTeam != null) {
                    for (Integer existingPlayerId : currentTeam.getPlayerIds()) {
                        if (!existingPlayerId.equals(id)) {
                            Player teamPlayer = playerRepository.findById(existingPlayerId).orElse(null);
                            if (teamPlayer != null && teamPlayer.getName().equalsIgnoreCase(request.getName())) {
                                throw new InvalidRequestException(
                                        "name",
                                        "Player with name '" + request.getName() + "' already exists in team '"
                                                + currentTeam.getTeamName() + "'");
                            }
                        }
                    }
                }
            }
            existingPlayer.setName(request.getName());
        }

        // Update role if provided
        if (request.getRole() != null && !request.getRole().isBlank()) {
            existingPlayer.setRole(request.getRole());
        }

        // Update batting style if provided
        if (request.getBattingStyle() != null && !request.getBattingStyle().isBlank()) {
            existingPlayer.setBattingStyle(request.getBattingStyle());
        }

        // Update bowling style if provided
        if (request.getBowlingStyle() != null) {
            existingPlayer.setBowlingStyle(request.getBowlingStyle());
        }

        // Update team if provided
        if (request.getTeamName() != null && !request.getTeamName().isBlank()) {
            Team newTeam = teamRepository.findByTeamNameIgnoreCase(request.getTeamName())
                    .orElseThrow(() -> new InvalidRequestException(
                            "teamName",
                            "Team not found with name: " + request.getTeamName()));

            Integer oldTeamId = existingPlayer.getTeamId();
            Integer newTeamId = newTeam.getId();

            // Handle team transfer
            if (oldTeamId == null || !oldTeamId.equals(newTeamId)) {
                // Remove from old team
                if (oldTeamId != null) {
                    teamRepository.findById(oldTeamId).ifPresent(oldTeam -> {
                        oldTeam.getPlayerIds().remove(id);
                        if (id.equals(oldTeam.getCaptainId())) {
                            oldTeam.setCaptainId(null);
                        }
                        teamRepository.save(oldTeam);
                    });
                }

                // Check team limit
                if (newTeam.getPlayerIds().size() >= 25) {
                    throw new InvalidRequestException(
                            "teamName",
                            "Team '" + newTeam.getTeamName() + "' has reached maximum player limit of 25");
                }

                // Add to new team
                if (!newTeam.getPlayerIds().contains(id)) {
                    newTeam.getPlayerIds().add(id);
                    teamRepository.save(newTeam);
                }

                existingPlayer.setTeamId(newTeamId);
            }
        }

        // Update stats if provided
        if (request.getStats() != null) {
            Stats existingStats = existingPlayer.getStats();
            if (existingStats == null) {
                existingStats = Stats.builder()
                        .matchesPlayed(0)
                        .runsScored(0)
                        .wicketsTaken(0)
                        .catchesTaken(0)
                        .build();
            }

            Stats requestStats = request.getStats();
            if (requestStats.getMatchesPlayed() != null) {
                existingStats.setMatchesPlayed(requestStats.getMatchesPlayed());
            }
            if (requestStats.getRunsScored() != null) {
                existingStats.setRunsScored(requestStats.getRunsScored());
            }
            if (requestStats.getWicketsTaken() != null) {
                existingStats.setWicketsTaken(requestStats.getWicketsTaken());
            }
            if (requestStats.getCatchesTaken() != null) {
                existingStats.setCatchesTaken(requestStats.getCatchesTaken());
            }

            existingPlayer.setStats(existingStats);
        }

        // Save and return
        Player updatedPlayer = playerRepository.save(existingPlayer);
        return convertToResponse(updatedPlayer);
    }

    /**
     * Delete player
     */
    public PlayerResponse deletePlayer(Integer id) {
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        // Remove player from team if assigned
        if (existingPlayer.getTeamId() != null) {
            teamRepository.findById(existingPlayer.getTeamId()).ifPresent(team -> {
                // Remove player ID from team's playerIds
                team.getPlayerIds().remove(id);

                // If deleted player was captain, clear captainId
                if (id.equals(team.getCaptainId())) {
                    team.setCaptainId(null);
                }

                teamRepository.save(team);
            });
        }

        // Convert to response before deleting
        PlayerResponse response = convertToResponse(existingPlayer);

        // Delete player
        playerRepository.deleteById(id);

        return response;
    }

    /**
     * Convert Player entity to PlayerResponse DTO
     */
    private PlayerResponse convertToResponse(Player player) {
        String teamName = null;

        // Get team name from teamId
        if (player.getTeamId() != null) {
            Team team = teamRepository.findById(player.getTeamId()).orElse(null);
            if (team != null) {
                teamName = team.getTeamName();
            }
        }

        return PlayerResponse.builder()
                .id(player.getId())
                .name(player.getName())
                .teamName(teamName)
                .role(player.getRole())
                .battingStyle(player.getBattingStyle())
                .bowlingStyle(player.getBowlingStyle())
                .stats(player.getStats())
                .build();
    }

    /**
     * Build Stats with default values
     */
    private Stats buildStats(Stats requestStats) {
        if (requestStats == null) {
            return Stats.builder()
                    .matchesPlayed(0)
                    .runsScored(0)
                    .wicketsTaken(0)
                    .catchesTaken(0)
                    .build();
        }

        return Stats.builder()
                .matchesPlayed(requestStats.getMatchesPlayed() != null ? requestStats.getMatchesPlayed() : 0)
                .runsScored(requestStats.getRunsScored() != null ? requestStats.getRunsScored() : 0)
                .wicketsTaken(requestStats.getWicketsTaken() != null ? requestStats.getWicketsTaken() : 0)
                .catchesTaken(requestStats.getCatchesTaken() != null ? requestStats.getCatchesTaken() : 0)
                .build();
    }
}
