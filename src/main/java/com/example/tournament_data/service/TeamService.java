package com.example.tournament_data.service;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import com.example.tournament_data.dto.TeamDetailsResponse;
import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.repository.PlayerRepository;
import com.example.tournament_data.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    // for aggregation
    private final MongoTemplate mongoTemplate;

    public Team create(Team team) {
        // check if captainId is actual player or not
        if (team.getCaptainId() != null && !team.getCaptainId().isEmpty()) {
            if (!playerRepository.existsById(team.getCaptainId())) {
                throw new InvalidRequestException("captainId", "Player not found with id: " + team.getCaptainId());
            }
        }

        if (team.getPlayerIds() != null && !team.getPlayerIds().isEmpty()) {
            for (String playerId : team.getPlayerIds()) {
                if (!playerRepository.existsById(playerId)) {
                    throw new InvalidRequestException("playerIds", "Player not found with id: " + playerId);
                }
            }
        }

        return teamRepository.save(team);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(String id) {
        return teamRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
    }

    public Team updateTeam(String id, Team teamDetails) {
        // first we will find the team
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        existingTeam.setTeamName(teamDetails.getTeamName());
        existingTeam.setHomeGround(teamDetails.getHomeGround());
        existingTeam.setCaptainId(teamDetails.getCaptainId());
        existingTeam.setCoach(teamDetails.getCoach());
        existingTeam.setPlayerIds(teamDetails.getPlayerIds());

        return teamRepository.save(existingTeam);
    }

    public Team patchTeam(String id, Team teamDetails) {

        // Step 1: Find existing team
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        // Step 2: Update only non-null fields
        if (teamDetails.getTeamName() != null) {
            existingTeam.setTeamName(teamDetails.getTeamName());
        }

        if (teamDetails.getHomeGround() != null) {
            existingTeam.setHomeGround(teamDetails.getHomeGround());
        }

        if (teamDetails.getCaptainId() != null) {
            existingTeam.setCaptainId(teamDetails.getCaptainId());
        }

        if (teamDetails.getCoach() != null) {
            existingTeam.setCoach(teamDetails.getCoach());
        }

        if (teamDetails.getPlayerIds() != null) {
            existingTeam.setPlayerIds(teamDetails.getPlayerIds());
        }

        // Step 3: Save and return
        return teamRepository.save(existingTeam);
    }

    public Team deleteTeam(String id) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        List<Player> players = playerRepository.findByTeamId(id);
        for (Player player : players) {
            player.setTeamId(null);
            playerRepository.save(player);
        }
        teamRepository.deleteById(id);
        return existingTeam;
    }

    public Team addPlayerToTeam(String id, String playerId) {
        // 1. first get the team
        // 2. update the array
        // 3. update players collection
        // 4. save

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        if (!team.getPlayerIds().contains(playerId))
            team.getPlayerIds().add(playerId);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
        player.setTeamId(id);
        playerRepository.save(player);

        return teamRepository.save(team);
    }

    public Team removePlayerFromTeam(String id, String playerId) {
        // 1. first get the team
        // 2. update the array
        // 3. update players collection
        // 4. save

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));

        team.getPlayerIds().remove(playerId);

        if (playerId.equals(team.getCaptainId()))
            team.setCaptainId(null);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
        player.setTeamId(null);
        playerRepository.save(player);

        return teamRepository.save(team);
    }

    public TeamDetailsResponse getTeamDetails(String id) {
        // Aggregation pipeline has 4 stages -> match, lookup, addFields, project

        // 1. match
        AggregationOperation matchStage = context -> new Document("$match", new Document("_id", id));

        // 2. lookup
        AggregationOperation lookupStage = context -> new Document("$lookup", new Document().append("from", "players")
                .append("localField", "playerIds").append("foreignField", "_id").append("as", "players"));

        // 3. addFields
        AggregationOperation addFieldsStage = context -> new Document("$addFields",
                new Document("squad",
                        new Document("$map",
                                new Document().append("input", "$players").append("as", "player").append("in",
                                        new Document().append("_id", "$$player._id").append("name", "$$player.name")
                                                .append("role", "$$player.role")))));

        // 4. project
        AggregationOperation projectionStage = context -> new Document("$project",
                new Document().append("playerIds", 0).append("players", 0));

        // making the pipeline
        Aggregation aggregation = Aggregation.newAggregation(matchStage, lookupStage, addFieldsStage, projectionStage);

        // executing the pipeline
        AggregationResults<TeamDetailsResponse> result = mongoTemplate.aggregate(aggregation, "teams",
                TeamDetailsResponse.class);

        return result.getUniqueMappedResult();
    }

}
