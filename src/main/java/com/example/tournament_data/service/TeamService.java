package com.example.tournament_data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.tournament_data.dto.*;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

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

    private static final String FIELD_CAPTAIN_NAME = "captainName";
    private static final String FIELD_PLAYER_NAMES = "playerNames";
    private static final String FIELD_PLAYERS = "players";

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
                                FIELD_PLAYER_NAMES,
                                "Player not found with name: " + playerName));
                playerIds.add(player.getId());
            }
        }

        // Convert captain name to ID
        Integer captainId = null;
        if (request.getCaptainName() != null && !request.getCaptainName().isEmpty()) {
            Player captain = playerRepository.findByNameIgnoreCase(request.getCaptainName())
                    .orElseThrow(() -> new InvalidRequestException(
                            FIELD_CAPTAIN_NAME,
                            "Captain not found with name: " + request.getCaptainName()));

            // Validate that captain is part of the team
            if (!playerIds.contains(captain.getId())) {
                throw new InvalidRequestException(
                        FIELD_CAPTAIN_NAME,
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
                .toList();
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
     * Get count of each role in the team
     */
    public List<RoleCount> getRoleCount(Integer id) {
        return playerRepository.findRoleCount(id);
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
                                FIELD_PLAYER_NAMES,
                                "Player not found with name: " + playerName));
                newPlayerIds.add(player.getId());
            }
        }

        // Convert captain name to ID
        Integer captainId = null;
        if (request.getCaptainName() != null && !request.getCaptainName().isEmpty()) {
            Player captain = playerRepository.findByNameIgnoreCase(request.getCaptainName())
                    .orElseThrow(() -> new InvalidRequestException(
                            FIELD_CAPTAIN_NAME,
                            "Captain not found with name: " + request.getCaptainName()));

            if (!newPlayerIds.contains(captain.getId())) {
                throw new InvalidRequestException(
                        FIELD_CAPTAIN_NAME,
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

        updateTeamName(existingTeam, request.getTeamName(), id);
        updateHomeGround(existingTeam, request.getHomeGround());
        updateCoach(existingTeam, request.getCoach());
        updatePlayers(existingTeam, request.getPlayerNames(), id);
        updateCaptain(existingTeam, request.getCaptainName());

        Team updatedTeam = teamRepository.save(existingTeam);
        return convertToResponse(updatedTeam);
    }

    private void updateTeamName(Team team, String newName, Integer teamId) {
        if (newName == null || newName.isBlank()) {
            return;
        }

        teamRepository.findByTeamNameIgnoreCase(newName)
                .ifPresent(existingTeam -> {
                    if (!existingTeam.getId().equals(teamId)) {
                        throw new InvalidRequestException(
                                "teamName",
                                "Team with name '" + newName + "' already exists");
                    }
                });

        team.setTeamName(newName);
    }

    private void updateHomeGround(Team team, String homeGround) {
        if (homeGround != null && !homeGround.isBlank()) {
            team.setHomeGround(homeGround);
        }
    }

    private void updateCoach(Team team, String coach) {
        if (coach != null && !coach.isBlank()) {
            team.setCoach(coach);
        }
    }

    private void updateCaptain(Team team, String captainName) {
        if (captainName == null) {
            return;
        }

        if (captainName.isBlank()) {
            team.setCaptainId(null);
            return;
        }

        Player captain = findPlayerByName(captainName, FIELD_CAPTAIN_NAME, "Captain");
        validateCaptainInTeam(team, captain);
        team.setCaptainId(captain.getId());
    }

    private void validateCaptainInTeam(Team team, Player captain) {
        if (!team.getPlayerIds().contains(captain.getId())) {
            throw new InvalidRequestException(
                    FIELD_CAPTAIN_NAME,
                    "Captain must be a player in the team");
        }
    }

    private void updatePlayers(Team team, List<String> playerNames, Integer teamId) {
        if (playerNames == null) {
            return;
        }

        List<Integer> oldPlayerIds = new ArrayList<>(team.getPlayerIds());
        List<Integer> newPlayerIds = convertPlayerNamesToIds(playerNames);

        // Clear captain if no longer in team
        clearCaptainIfNotInTeam(team, newPlayerIds);

        // Update player team associations
        updatePlayerTeamAssociations(oldPlayerIds, newPlayerIds, teamId);

        team.setPlayerIds(newPlayerIds);
    }

    private List<Integer> convertPlayerNamesToIds(List<String> playerNames) {
        return playerNames.stream()
                .map(name -> findPlayerByName(name, FIELD_PLAYER_NAMES, "Player"))
                .map(Player::getId)
                .toList();
    }

    private Player findPlayerByName(String name, String fieldName, String entityName) {
        return playerRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new InvalidRequestException(
                        fieldName,
                        entityName + " not found with name: " + name));
    }

    private void clearCaptainIfNotInTeam(Team team, List<Integer> newPlayerIds) {
        if (team.getCaptainId() != null && !newPlayerIds.contains(team.getCaptainId())) {
            team.setCaptainId(null);
        }
    }

    private void updatePlayerTeamAssociations(List<Integer> oldPlayerIds,
                                              List<Integer> newPlayerIds,
                                              Integer teamId) {
        // Clear teamId for removed players
        clearTeamIdForRemovedPlayers(oldPlayerIds, newPlayerIds);

        // Set teamId for new players
        setTeamIdForNewPlayers(newPlayerIds, teamId);
    }

    private void clearTeamIdForRemovedPlayers(List<Integer> oldPlayerIds,
                                              List<Integer> newPlayerIds) {
        oldPlayerIds.stream()
                .filter(id -> !newPlayerIds.contains(id))
                .forEach(this::clearPlayerTeamId);
    }

    private void setTeamIdForNewPlayers(List<Integer> playerIds, Integer teamId) {
        playerIds.forEach(playerId -> setPlayerTeamId(playerId, teamId));
    }

    private void clearPlayerTeamId(Integer playerId) {
        playerRepository.findById(playerId).ifPresent(player -> {
            player.setTeamId(null);
            playerRepository.save(player);
        });
    }

    private void setPlayerTeamId(Integer playerId, Integer teamId) {
        playerRepository.findById(playerId).ifPresent(player -> {
            player.setTeamId(teamId);
            playerRepository.save(player);
        });
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
                        .append("from", FIELD_PLAYERS)
                        .append("localField", "playerIds")
                        .append("foreignField", "_id")
                        .append("as", FIELD_PLAYERS));

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
                        .append(FIELD_PLAYERS, 0));

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
        String captainName = null;
        List<String> playerNames = new ArrayList<>();

        // Single DB call for all players (including captain)
        if (team.getPlayerIds() != null && !team.getPlayerIds().isEmpty()) {
            List<Player> players = playerRepository.findByIdIn(team.getPlayerIds());

            // Create map for quick lookup
            Map<Integer, Player> playerMap = players.stream()
                    .collect(Collectors.toMap(Player::getId, p -> p));

            // Get captain name from map (no extra DB call)
            if (team.getCaptainId() != null) {
                Player captain = playerMap.get(team.getCaptainId());
                captainName = captain != null ? captain.getName() : null;
            }

            // Get player names maintaining order
            playerNames = team.getPlayerIds().stream()
                    .map(playerMap::get)
                    .filter(Objects::nonNull)
                    .map(Player::getName)
                    .toList();
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
