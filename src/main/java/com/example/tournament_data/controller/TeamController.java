package com.example.tournament_data.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tournament_data.dto.ApiResponse;
import com.example.tournament_data.dto.TeamDetailsResponse;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.service.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Team", description = "Team management APIs for cricket tournament")
@RequiredArgsConstructor
public class TeamController {

    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

    private final TeamService teamService;

    @Operation(summary = "Get all teams", description = "Retrieves a list of all teams in the tournament database")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teams retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Team>>> getAllTeams() {

        logger.info("GET /api/teams - Fetching all teams");

        List<Team> teams = teamService.getAllTeams();

        logger.info("Found {} teams", teams.size());
        logger.debug("Teams: {}", teams);

        ApiResponse<List<Team>> response = ApiResponse.success("Team retreived successfully", teams);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get team by ID", description = "Retrieves a specific team by its unique ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> getTeamById(
            @Parameter(description = "Team ID", required = true, example = "64a1b2c3d4e5f6g7h8i9j0k1") @PathVariable String id) {

        logger.info("GET /api/teams/{} - Fetching team by ID", id);
        Team team = teamService.getTeamById(id);

        logger.info("Team found: {}", team.getTeamName());
        logger.debug("Team details: {}", team);

        ApiResponse<Team> response = ApiResponse.success(
                "Team retrieved successfully",
                team);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get team details with players", description = "Retrieves detailed team information including player details (name, role) using MongoDB aggregation")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    @GetMapping("/details/{id}")
    public ResponseEntity<ApiResponse<TeamDetailsResponse>> getTeamDetails(
            @Parameter(description = "Team ID", required = true, example = "64a1b2c3d4e5f6g7h8i9j0k1") @PathVariable String id) {

        logger.info("GET /api/teams/details/{} - Fetching team details with players", id);

        TeamDetailsResponse teamDetails = teamService.getTeamDetails(id);

        logger.info("Team details retrieved for: {}", teamDetails.getTeamName());
        logger.debug("Team details: {}", teamDetails);

        ApiResponse<TeamDetailsResponse> response = ApiResponse.success(
                "Team details retrieved successfully",
                teamDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new team", description = "Creates a new team in the tournament database")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Team created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input - Validation failed")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Team object to be created", required = true, content = @Content(schema = @Schema(implementation = Team.class), examples = @ExampleObject(name = "Sample Team", value = """
            {
                "teamName": "Mumbai Indians",
                "homeGround": "Wankhede Stadium, Mumbai",
                "coach": "Mahela Jayawardene",
                "captainId": null,
                "playerIds": []
            }
            """)))
    @PostMapping
    public ResponseEntity<ApiResponse<Team>> addTeam(@Valid @RequestBody Team team) {

        logger.info("POST /api/teams - Creating new team");
        logger.info("Team name: {}, Coach: {}", team.getTeamName(), team.getCoach());
        logger.debug("Full team details: {}", team);

        Team addedTeam = teamService.create(team);

        logger.info("Team created successfully with ID: {}", addedTeam.getId());

        ApiResponse<Team> response = ApiResponse.success("Team created successully", addedTeam);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a team", description = "Deletes a team from the database. Also removes the team reference from all associated players.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> deleteTeam(
            @Parameter(description = "Team ID to delete", required = true) @PathVariable String id) {

        logger.info("DELETE /api/teams/{} - Deleting team", id);
        logger.warn("Attempting to delete team with ID: {}", id);

        Team team = teamService.deleteTeam(id);

        logger.info("Team '{}' deleted successfully", team.getTeamName());

        ApiResponse<Team> response = ApiResponse.success("Team deleted successfully", team);
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "Update a team (Full Update)", description = "Updates all fields of an existing team. All fields will be replaced with new values.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Team>> deleteTeam(
            @Parameter(description = "Team ID to update", required = true) @PathVariable String id,
            @Valid @RequestBody Team teamDetails) {

        logger.info("PUT /api/teams/update/{} - Full update request", id);
        logger.info("Updating team with new name: {}, coach: {}", teamDetails.getTeamName(), teamDetails.getCoach());
        logger.debug("Full update details: {}", teamDetails);

        Team updatedTeam = teamService.updateTeam(id, teamDetails);

        logger.info("Team {} updated successfully", id);

        ApiResponse<Team> response = ApiResponse.success(
                "Team updated successfully",
                updatedTeam);

        return ResponseEntity.ok(response);

    }

    @Operation(summary = "Partially update a team", description = "Updates only the provided fields of an existing team. Fields not included in the request will remain unchanged.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Fields to update (only include fields you want to change)", content = @Content(examples = @ExampleObject(name = "Partial Update Example", value = """
            {
                "teamName": "Updated Team Name",
                "coach": "New Coach Name"
            }
            """)))
    @PatchMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Team>> partialUpdateTeam(
            @Parameter(description = "Team ID to update", required = true) @PathVariable String id,
            @Valid @RequestBody Team teamDetails) {

        logger.info("PATCH /api/teams/update/{} - Partial update request", id);
        logger.debug("Partial update details: {}", teamDetails);

        Team updatedTeam = teamService.patchTeam(id, teamDetails);

        logger.info("Team {} partially updated successfully", id);

        ApiResponse<Team> response = ApiResponse.success(
                "Team updated successfully",
                updatedTeam);

        return ResponseEntity.ok(response);
    }

    // adding player to a team, so basically, updating the playerIds

    @Operation(summary = "Add a player to a team", description = "Adds an existing player to a team. The player must not already belong to another team.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player added to team successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request - Player already in team or belongs to another team"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team or Player not found")
    })
    @PatchMapping("/add-player/{teamId}")
    public ResponseEntity<ApiResponse<Team>> addPlayerToTeam(
            @Parameter(description = "Team ID", required = true, example = "64a1b2c3d4e5f6g7h8i9j0k1") @PathVariable String teamId,
            @Valid @RequestBody Player player) {

        logger.info("PATCH /api/teams/addPlayer/{} - Adding player to team", teamId);

        Team team = teamService.addPlayerToTeam(teamId, player.getId());

        logger.info("Player {} added to team {} successfully", player.getId(), teamId);

        ApiResponse<Team> response = ApiResponse.success("Player added successfully", team);
        return ResponseEntity.ok(response);
    }

    // removing player from a team, so basically, updating the playerIds

    @Operation(summary = "Remove a player from a team", description = "Removes a player from a team. If the player is the captain, the captain will be cleared.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player removed from team successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request - Player not in team"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team or Player not found")
    })
    @PatchMapping("/remove-player/{teamId}")
    public ResponseEntity<ApiResponse<Team>> removePlayerFromTeam(
            @Parameter(description = "Team ID", required = true, example = "64a1b2c3d4e5f6g7h8i9j0k1") @PathVariable String teamId,
            @Valid @RequestBody Player player) {

        logger.info("DELETE /api/teams/removePlayer/{} - Removing player from team", teamId);
        logger.warn("Removing player {} from team {}", player.getId(), teamId);

        Team team = teamService.removePlayerFromTeam(teamId, player.getId());

        logger.info("Player {} removed from team {} successfully", player.getId(), teamId);

        ApiResponse<Team> response = ApiResponse.success("Player removed successfully", team);
        return ResponseEntity.ok(response);
    }
}
