package com.example.tournament_data.service;

import java.util.List;

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

    private static final int MAX_TEAM_PLAYERS = 25;
    private static final String FIELD_NAME = "name";
    private static final String FIELD_TEAM_NAME = "teamName";
    private static final String FIELD_TEAM_NOT_FOUND_WITH_NAME = "Team not found with name: ";
    private static final String FIELD_PLAYER = "Player";

    /**
     * Create a new player
     */
    public PlayerResponse create(@Valid PlayerCreateRequest request) {
        // Team name is required - find team by name
        Team team = teamRepository.findByTeamNameIgnoreCase(request.getTeamName())
                .orElseThrow(() -> new InvalidRequestException(
                        FIELD_TEAM_NAME,
                        FIELD_TEAM_NOT_FOUND_WITH_NAME + request.getTeamName()));

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
                    FIELD_TEAM_NAME,
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
                .toList();
    }

    /**
     * Get player by ID
     */
    public PlayerResponse getPlayerById(Integer id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FIELD_PLAYER, "id", id));

        return convertToResponse(player);
    }

    /**
     * Update player (full update)
     */
    public PlayerResponse updatePlayer(Integer id, @Valid PlayerCreateRequest request) {
        // Find existing player
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FIELD_PLAYER, "id", id));

        // Find team by name
        Team newTeam = teamRepository.findByTeamNameIgnoreCase(request.getTeamName())
                .orElseThrow(() -> new InvalidRequestException(
                        FIELD_TEAM_NAME,
                        FIELD_TEAM_NOT_FOUND_WITH_NAME + request.getTeamName()));

        handleTeamTransfer(existingPlayer, newTeam, id);
        validateNameNotDuplicateInTeam(newTeam, request.getName(), id);
        updatePlayerFields(existingPlayer, request, newTeam.getId());

        // Save and return
        Player updatedPlayer = playerRepository.save(existingPlayer);
        return convertToResponse(updatedPlayer);
    }

    private void handleTeamTransfer(Player player, Team newTeam, Integer playerId) {
        Integer oldTeamId = player.getTeamId();
        Integer newTeamId = newTeam.getId();

        if (shouldTransferTeam(oldTeamId, newTeamId)) {
            removePlayerFromOldTeam(playerId, oldTeamId);
            addPlayerToTeam(playerId, newTeam);
        } else if (oldTeamId == null) {
            addPlayerToTeam(playerId, newTeam);
        }
    }

    private boolean shouldTransferTeam(Integer oldTeamId, Integer newTeamId) {
        return oldTeamId != null && !oldTeamId.equals(newTeamId);
    }

    private void addPlayerToTeam(Integer playerId, Team team) {
        if (!team.getPlayerIds().contains(playerId)) {
            team.getPlayerIds().add(playerId);
            teamRepository.save(team);
        }
    }

// ==================== Validation ====================

    private void validateNameNotDuplicateInTeam(Team team, String name, Integer excludePlayerId) {
        // Batch fetch all team players (optimized - avoids N+1)
        List<Player> teamPlayers = playerRepository.findByIdIn(team.getPlayerIds());

        boolean isDuplicate = teamPlayers.stream()
                .filter(p -> !p.getId().equals(excludePlayerId))
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));

        if (isDuplicate) {
            throw new InvalidRequestException(
                    FIELD_NAME,
                    "Player with name '" + name + "' already exists in team '" + team.getTeamName() + "'");
        }
    }

// ==================== Update Fields ====================

    private void updatePlayerFields(Player player, PlayerCreateRequest request, Integer newTeamId) {
        player.setName(request.getName());
        player.setTeamId(newTeamId);
        player.setRole(request.getRole());
        player.setBattingStyle(request.getBattingStyle());
        player.setBowlingStyle(request.getBowlingStyle());
        player.setStats(buildStats(request.getStats()));
    }

    /**
     * Patch player (partial update)
     */
    public PlayerResponse patchPlayer(Integer id, PlayerPatchRequest request) {
        // Find existing player
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FIELD_PLAYER, "id", id));

        updateName(existingPlayer, request.getName(), id);
        updateBasicFields(existingPlayer, request);
        updateTeam(existingPlayer, request.getTeamName(), id);
        updateStats(existingPlayer, request.getStats());

        // Save and return
        Player updatedPlayer = playerRepository.save(existingPlayer);
        return convertToResponse(updatedPlayer);
    }

    private void updateName(Player player, String newName, Integer playerId) {
        if (newName == null || newName.isBlank()) {
            return;
        }

        validateNameNotDuplicateInTeam(player.getTeamId(), newName, playerId);
        player.setName(newName);
    }

    private void validateNameNotDuplicateInTeam(Integer teamId, String name, Integer excludePlayerId) {
        if (teamId == null) {
            return;
        }

        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return;
        }

        // Batch fetch all team players
        List<Player> teamPlayers = playerRepository.findByIdIn(team.getPlayerIds());

        boolean isDuplicate = teamPlayers.stream()
                .filter(p -> !p.getId().equals(excludePlayerId))
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));

        if (isDuplicate) {
            throw new InvalidRequestException(
                    FIELD_NAME,
                    "Player with name '" + name + "' already exists in team '" + team.getTeamName() + "'");
        }
    }

    // ==================== Update Basic Fields ====================

    private void updateBasicFields(Player player, PlayerPatchRequest request) {
        updateRole(player, request.getRole());
        updateBattingStyle(player, request.getBattingStyle());
        updateBowlingStyle(player, request.getBowlingStyle());
    }

    private void updateRole(Player player, String role) {
        if (role != null && !role.isBlank()) {
            player.setRole(role);
        }
    }

    private void updateBattingStyle(Player player, String battingStyle) {
        if (battingStyle != null && !battingStyle.isBlank()) {
            player.setBattingStyle(battingStyle);
        }
    }

    private void updateBowlingStyle(Player player, String bowlingStyle) {
        if (bowlingStyle != null) {
            player.setBowlingStyle(bowlingStyle);
        }
    }

    // ==================== Update Team ====================

    private void updateTeam(Player player, String teamName, Integer playerId) {
        if (teamName == null || teamName.isBlank()) {
            return;
        }

        Team newTeam = findTeamByName(teamName);
        Integer oldTeamId = player.getTeamId();
        Integer newTeamId = newTeam.getId();

        if (isTeamChanged(oldTeamId, newTeamId)) {
            transferPlayerToNewTeam(playerId, oldTeamId, newTeam);
            player.setTeamId(newTeamId);
        }
    }

    private Team findTeamByName(String teamName) {
        return teamRepository.findByTeamNameIgnoreCase(teamName)
                .orElseThrow(() -> new InvalidRequestException(
                        FIELD_TEAM_NAME,
                        FIELD_TEAM_NOT_FOUND_WITH_NAME + teamName));
    }

    private boolean isTeamChanged(Integer oldTeamId, Integer newTeamId) {
        return oldTeamId == null || !oldTeamId.equals(newTeamId);
    }

    private void transferPlayerToNewTeam(Integer playerId, Integer oldTeamId, Team newTeam) {
        removePlayerFromOldTeam(playerId, oldTeamId);
        addPlayerToNewTeam(playerId, newTeam);
    }

    private void removePlayerFromOldTeam(Integer playerId, Integer oldTeamId) {
        if (oldTeamId == null) {
            return;
        }

        teamRepository.findById(oldTeamId).ifPresent(oldTeam -> {
            oldTeam.getPlayerIds().remove(playerId);
            clearCaptainIfNeeded(oldTeam, playerId);
            teamRepository.save(oldTeam);
        });
    }

    private void clearCaptainIfNeeded(Team team, Integer playerId) {
        if (playerId.equals(team.getCaptainId())) {
            team.setCaptainId(null);
        }
    }

    private void addPlayerToNewTeam(Integer playerId, Team newTeam) {
        validateTeamPlayerLimit(newTeam);

        if (!newTeam.getPlayerIds().contains(playerId)) {
            newTeam.getPlayerIds().add(playerId);
            teamRepository.save(newTeam);
        }
    }

    private void validateTeamPlayerLimit(Team team) {
        if (team.getPlayerIds().size() >= MAX_TEAM_PLAYERS) {
            throw new InvalidRequestException(
                    FIELD_TEAM_NAME,
                    "Team '" + team.getTeamName() + "' has reached maximum player limit of " + MAX_TEAM_PLAYERS);
        }
    }

    // ==================== Update Stats ====================

    private void updateStats(Player player, Stats requestStats) {
        if (requestStats == null) {
            return;
        }

        Stats existingStats = getOrCreateStats(player);
        applyStatsUpdates(existingStats, requestStats);
        player.setStats(existingStats);
    }

    private Stats getOrCreateStats(Player player) {
        Stats existingStats = player.getStats();

        if (existingStats == null) {
            return Stats.builder()
                    .matchesPlayed(0)
                    .runsScored(0)
                    .wicketsTaken(0)
                    .catchesTaken(0)
                    .build();
        }

        return existingStats;
    }

    private void applyStatsUpdates(Stats existingStats, Stats requestStats) {
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
    }

    /**
     * Delete player
     */
    public PlayerResponse deletePlayer(Integer id) {
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FIELD_PLAYER, "id", id));

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
