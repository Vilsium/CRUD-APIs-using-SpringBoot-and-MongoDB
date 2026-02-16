package com.example.tournament_data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    // for aggregation
    private final MongoTemplate mongoTemplate;

    /**
     * Create a new team
     */
    public TeamResponse create(@Valid TeamCreateRequest request) {
        // Check if team with same name already exists
        if (teamRepository.findByTeamNameIgnoreCase(request.getTeamName()).isPresent()) {
            throw new InvalidRequestException(
                    "teamName",
                    "Team with name '" + request.getTeamName() + "' already exists");
        }

        // Generate auto-incremented ID
        Integer teamId = sequenceGeneratorService.generateSequence(Team.SEQUENCE_NAME);

        // Convert player names to IDs
        List<Integer> playerIds = new ArrayList<>();
        if (request.getPlayerNames() != null && !request.getPlayerNames().isEmpty()) {
            for (String playerName : request.getPlayerNames()) {
                Player player = playerRepository.findByNameIgnoreCase(playerName)
                        .orElseThrow(() -> new InvalidRequestException(
                                "playerNames",
                                "Player not found with name: " + playerName));
                playerIds.add(player.getId());
            }
        }

        // Convert captain name to ID
        Integer captainId = null;
        if (request.getCaptainName() != null && !request.getCaptainName().isEmpty()) {
            Player captain = playerRepository.findByNameIgnoreCase(request.getCaptainName())
                    .orElseThrow(() -> new InvalidRequestException(
                            "captainName",
                            "Captain not found with name: " + request.getCaptainName()));

            // Validate that captain is part of the team
            if (!playerIds.contains(captain.getId())) {
                throw new InvalidRequestException(
                        "captainName",
                        "Captain must be a player in the team");
            }

            captainId = captain.getId();
        }

        // Build team entity
        Team team = Team.builder()
                .id(teamId)
                .teamName(request.getTeamName())
                .homeGround(request.getHomeGround())
                .coach(request.getCoach())
                .captainId(captainId)
                .playerIds(playerIds)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Update players' teamId
        for (Integer playerId : playerIds) {
            playerRepository.findById(playerId).ifPresent(player -> {
                player.setTeamId(savedTeam.getId());
                playerRepository.save(player);
            });
        }

        return convertToResponse(savedTeam);
    }

    /**
     * Get all teams
     */
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get team by ID
     */
    public TeamResponse getTeamById(Integer id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        return convertToResponse(team);
    }

    /**
     * Update team (full update)
     */
    public TeamResponse updateTeam(Integer id, @Valid TeamCreateRequest request) {
        // Find existing team
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        // Check if new team name already exists (excluding current team)
        teamRepository.findByTeamNameIgnoreCase(request.getTeamName())
                .ifPresent(team -> {
                    if (!team.getId().equals(id)) {
                        throw new InvalidRequestException(
                                "teamName",
                                "Team with name '" + request.getTeamName() + "' already exists");
                    }
                });

        // Get old player IDs to clear their teamId
        List<Integer> oldPlayerIds = new ArrayList<>(existingTeam.getPlayerIds());

        // Convert new player names to IDs
        List<Integer> newPlayerIds = new ArrayList<>();
        if (request.getPlayerNames() != null && !request.getPlayerNames().isEmpty()) {
            for (String playerName : request.getPlayerNames()) {
                Player player = playerRepository.findByNameIgnoreCase(playerName)
                        .orElseThrow(() -> new InvalidRequestException(
                                "playerNames",
                                "Player not found with name: " + playerName));
                newPlayerIds.add(player.getId());
            }
        }

        // Convert captain name to ID
        Integer captainId = null;
        if (request.getCaptainName() != null && !request.getCaptainName().isEmpty()) {
            Player captain = playerRepository.findByNameIgnoreCase(request.getCaptainName())
                    .orElseThrow(() -> new InvalidRequestException(
                            "captainName",
                            "Captain not found with name: " + request.getCaptainName()));

            if (!newPlayerIds.contains(captain.getId())) {
                throw new InvalidRequestException(
                        "captainName",
                        "Captain must be a player in the team");
            }

            captainId = captain.getId();
        }

        // Clear teamId for players no longer in team
        for (Integer oldPlayerId : oldPlayerIds) {
            if (!newPlayerIds.contains(oldPlayerId)) {
                playerRepository.findById(oldPlayerId).ifPresent(player -> {
                    player.setTeamId(null);
                    playerRepository.save(player);
                });
            }
        }

        // Set teamId for new players
        for (Integer newPlayerId : newPlayerIds) {
            playerRepository.findById(newPlayerId).ifPresent(player -> {
                player.setTeamId(id);
                playerRepository.save(player);
            });
        }

        // Update team fields
        existingTeam.setTeamName(request.getTeamName());
        existingTeam.setHomeGround(request.getHomeGround());
        existingTeam.setCoach(request.getCoach());
        existingTeam.setCaptainId(captainId);
        existingTeam.setPlayerIds(newPlayerIds);

        Team updatedTeam = teamRepository.save(existingTeam);
        return convertToResponse(updatedTeam);
    }

    /**
     * Patch team (partial update)
     */
    public TeamResponse patchTeam(Integer id, @Valid TeamPatchRequest request) {
        // Find existing team
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        // Update team name if provided
        if (request.getTeamName() != null && !request.getTeamName().isBlank()) {
            // Check if new name already exists
            teamRepository.findByTeamNameIgnoreCase(request.getTeamName())
                    .ifPresent(team -> {
                        if (!team.getId().equals(id)) {
                            throw new InvalidRequestException(
                                    "teamName",
                                    "Team with name '" + request.getTeamName() + "' already exists");
                        }
                    });
            existingTeam.setTeamName(request.getTeamName());
        }

        // Update home ground if provided
        if (request.getHomeGround() != null && !request.getHomeGround().isBlank()) {
            existingTeam.setHomeGround(request.getHomeGround());
        }

        // Update coach if provided
        if (request.getCoach() != null && !request.getCoach().isBlank()) {
            existingTeam.setCoach(request.getCoach());
        }

        // Update captain if provided
        if (request.getCaptainName() != null) {
            if (request.getCaptainName().isBlank()) {
                // Clear captain
                existingTeam.setCaptainId(null);
            } else {
                Player captain = playerRepository.findByNameIgnoreCase(request.getCaptainName())
                        .orElseThrow(() -> new InvalidRequestException(
                                "captainName",
                                "Captain not found with name: " + request.getCaptainName()));

                if (!existingTeam.getPlayerIds().contains(captain.getId())) {
                    throw new InvalidRequestException(
                            "captainName",
                            "Captain must be a player in the team");
                }

                existingTeam.setCaptainId(captain.getId());
            }
        }

        // Update players if provided
        if (request.getPlayerNames() != null) {
            // Get old player IDs
            List<Integer> oldPlayerIds = new ArrayList<>(existingTeam.getPlayerIds());

            // Convert new player names to IDs
            List<Integer> newPlayerIds = new ArrayList<>();
            for (String playerName : request.getPlayerNames()) {
                Player player = playerRepository.findByNameIgnoreCase(playerName)
                        .orElseThrow(() -> new InvalidRequestException(
                                "playerNames",
                                "Player not found with name: " + playerName));
                newPlayerIds.add(player.getId());
            }

            // Validate captain is still in team
            if (existingTeam.getCaptainId() != null && !newPlayerIds.contains(existingTeam.getCaptainId())) {
                existingTeam.setCaptainId(null);
            }

            // Clear teamId for removed players
            for (Integer oldPlayerId : oldPlayerIds) {
                if (!newPlayerIds.contains(oldPlayerId)) {
                    playerRepository.findById(oldPlayerId).ifPresent(player -> {
                        player.setTeamId(null);
                        playerRepository.save(player);
                    });
                }
            }

            // Set teamId for new players
            for (Integer newPlayerId : newPlayerIds) {
                playerRepository.findById(newPlayerId).ifPresent(player -> {
                    player.setTeamId(id);
                    playerRepository.save(player);
                });
            }

            existingTeam.setPlayerIds(newPlayerIds);
        }

        Team updatedTeam = teamRepository.save(existingTeam);
        return convertToResponse(updatedTeam);
    }

    /**
     * Delete team
     */
    public TeamResponse deleteTeam(Integer id) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        // Convert to response before deleting
        TeamResponse response = convertToResponse(existingTeam);

        // Clear teamId for all players in this team
        List<Player> players = playerRepository.findByTeamId(id);
        for (Player player : players) {
            player.setTeamId(null);
            playerRepository.save(player);
        }

        teamRepository.deleteById(id);
        return response;
    }

    /**
     * Get team details with aggregation
     */
    public TeamDetailsResponse getTeamDetails(Integer id) {
        // Verify team exists
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team", "id", id);
        }

        // Aggregation pipeline has 4 stages -> match, lookup, addFields, project

        // 1. match
        AggregationOperation matchStage = context -> new Document("$match", new Document("_id", id));

        // 2. lookup
        AggregationOperation lookupStage = context -> new Document("$lookup",
                new Document()
                        .append("from", "players")
                        .append("localField", "playerIds")
                        .append("foreignField", "_id")
                        .append("as", "players"));

        // 3. addFields
        AggregationOperation addFieldsStage = context -> new Document("$addFields",
                new Document("squad",
                        new Document("$map",
                                new Document()
                                        .append("input", "$players")
                                        .append("as", "player")
                                        .append("in", new Document()
                                                .append("_id", "$$player._id")
                                                .append("name", "$$player.name")
                                                .append("role", "$$player.role")))));

        // 4. project
        AggregationOperation projectionStage = context -> new Document("$project",
                new Document()
                        .append("playerIds", 0)
                        .append("players", 0));

        // making the pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                matchStage, lookupStage, addFieldsStage, projectionStage);

        // executing the pipeline
        AggregationResults<TeamDetailsResponse> result = mongoTemplate.aggregate(
                aggregation, "teams", TeamDetailsResponse.class);

        TeamDetailsResponse teamDetails = result.getUniqueMappedResult();

        if (teamDetails == null) {
            throw new ResourceNotFoundException("Team", "id", id);
        }

        return teamDetails;
    }

    /**
     * Convert Team entity to TeamResponse DTO
     */
    private TeamResponse convertToResponse(Team team) {
        // Get captain name
        String captainName = null;
        if (team.getCaptainId() != null) {
            Player captain = playerRepository.findById(team.getCaptainId()).orElse(null);
            if (captain != null) {
                captainName = captain.getName();
            }
        }

        // Get player names
        List<String> playerNames = new ArrayList<>();
        for (Integer playerId : team.getPlayerIds()) {
            Player player = playerRepository.findById(playerId).orElse(null);
            if (player != null) {
                playerNames.add(player.getName());
            }
        }

        return TeamResponse.builder()
                .id(team.getId())
                .teamName(team.getTeamName())
                .homeGround(team.getHomeGround())
                .coach(team.getCoach())
                .captainName(captainName)
                .playerNames(playerNames)
                .build();
    }
}
