package com.example.tournament_data.controller;

import java.util.List;

import com.example.tournament_data.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "Team", description = "Team management APIs for cricket tournament")
@RequiredArgsConstructor
public class TeamController {

        private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

        private final TeamService teamService;

        @Operation(summary = "Get all teams", description = "Retrieves a list of all teams in the tournament database")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teams retrieved successfully", content = @Content(schema = @Schema(implementation = TeamResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping
        public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams() {

                logger.info("GET /api/v1/teams - Fetching all teams");

                List<TeamResponse> teams = teamService.getAllTeams();

                logger.info("Found {} teams", teams.size());

                ApiResponse<List<TeamResponse>> response = ApiResponse.success(
                                "Teams retrieved successfully", teams);

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get team details with players", description = "Retrieves detailed team information including player details using MongoDB aggregation")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team details retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
        })
        @GetMapping("/{id}/details")
        public ResponseEntity<ApiResponse<TeamDetailsResponse>> getTeamDetails(
                        @Parameter(description = "Team ID", required = true, example = "1") @PathVariable Integer id) {

                logger.info("GET /api/v1/teams/{}/details - Fetching team details with players", id);

                TeamDetailsResponse teamDetails = teamService.getTeamDetails(id);

                logger.info("Team details retrieved for: {}", teamDetails.getTeamName());

                ApiResponse<TeamDetailsResponse> response = ApiResponse.success(
                                "Team details retrieved successfully", teamDetails);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get count of each role in the team", description = "Retrieves the count of batsman, bowler, all-rounder and wicket-keeper in the team using MongoDB aggregation")
        @GetMapping("/{id}/role-count")
        public ResponseEntity<ApiResponse<List<RoleCount>>> getRoleCount(@Parameter(description = "Team ID", required = true, example = "1") @PathVariable Integer id) {

                logger.info("GET /api/v1/teams/{}/role-count - Fetching count of each role in the team", id);

                List<RoleCount> roleCounts = teamService.getRoleCount(id);

                ApiResponse<List<RoleCount>> response = ApiResponse.success("Role count retrieved successfully", roleCounts);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Create a new team", description = "Creates a new team in the tournament database")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Team created successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input - Validation failed")
        })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Team object to be created", required = true, content = @Content(schema = @Schema(implementation = TeamCreateRequest.class), examples = @ExampleObject(name = "Sample Team", value = """
                        {
                            "teamName": "Mumbai Indians",
                            "homeGround": "Wankhede Stadium, Mumbai",
                            "coach": "Mahela Jayawardene",
                            "captainName": "Rohit Sharma",
                            "playerNames": ["Rohit Sharma", "Jasprit Bumrah", "Suryakumar Yadav"]
                        }
                        """)))
        @PostMapping
        public ResponseEntity<ApiResponse<TeamResponse>> createTeam(
                        @Valid @RequestBody TeamCreateRequest request) {

                logger.info("POST /api/v1/teams - Creating new team: {}", request.getTeamName());

                TeamResponse createdTeam = teamService.create(request);

                logger.info("Team created successfully with ID: {}", createdTeam.getId());

                ApiResponse<TeamResponse> response = ApiResponse.success(
                                "Team created successfully", createdTeam);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @Operation(summary = "Update a team (Full Update)", description = "Updates all fields of an existing team")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team updated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
        })
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
                        @Parameter(description = "Team ID to update", required = true, example = "1") @PathVariable Integer id,
                        @Valid @RequestBody TeamCreateRequest request) {

                logger.info("PUT /api/v1/teams/{} - Full update request", id);

                TeamResponse updatedTeam = teamService.updateTeam(id, request);

                logger.info("Team {} updated successfully", id);

                ApiResponse<TeamResponse> response = ApiResponse.success(
                                "Team updated successfully", updatedTeam);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Partially update a team", description = "Updates only the provided fields of an existing team")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team updated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
        })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Fields to update", content = @Content(examples = @ExampleObject(name = "Partial Update Example", value = """
                        {
                            "teamName": "Updated Team Name",
                            "coach": "New Coach Name"
                        }
                        """)))
        @PatchMapping("/{id}")
        public ResponseEntity<ApiResponse<TeamResponse>> patchTeam(
                        @Parameter(description = "Team ID to update", required = true, example = "1") @PathVariable Integer id,
                        @Valid @RequestBody TeamPatchRequest request) {

                logger.info("PATCH /api/v1/teams/{} - Partial update request", id);

                TeamResponse updatedTeam = teamService.patchTeam(id, request);

                logger.info("Team {} partially updated successfully", id);

                ApiResponse<TeamResponse> response = ApiResponse.success(
                                "Team updated successfully", updatedTeam);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Delete a team", description = "Deletes a team from the database")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team deleted successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<TeamResponse>> deleteTeam(
                        @Parameter(description = "Team ID to delete", required = true, example = "1") @PathVariable Integer id) {

                logger.info("DELETE /api/v1/teams/{} - Deleting team", id);

                TeamResponse deletedTeam = teamService.deleteTeam(id);

                logger.info("Team '{}' deleted successfully", deletedTeam.getTeamName());

                ApiResponse<TeamResponse> response = ApiResponse.success(
                                "Team deleted successfully", deletedTeam);
                return ResponseEntity.ok(response);
        }
}
