package com.example.tournament_data.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tournament_data.dto.ApiResponse;
import com.example.tournament_data.model.Match;
import com.example.tournament_data.service.MatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Match", description = "Match management APIs for cricket tournament")
public class MatchController {

    private static final Logger logger = LoggerFactory.getLogger(MatchController.class);

    @Autowired
    private MatchService matchService;

    @Operation(summary = "Get all matches", description = "Retrieves a list of all matches in the tournament database")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Matches retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Match>>> getAllMatches() {

        logger.info("GET /api/matches - Fetching all matches");

        List<Match> matches = matchService.getAllMatches();

        logger.info("Found {} matches", matches.size());
        logger.debug("Matches: {}", matches);

        ApiResponse<List<Match>> response = ApiResponse.success("Matches retreived successfully", matches);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get match by ID", description = "Retrieves a specific match by its unique ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Match>> getMatch(
            @Parameter(description = "Match ID", required = true, example = "64a1b2c3d4e5f6g7h8i9j0k1") @PathVariable String id) {

        logger.info("GET /api/matches/{} - Fetching match by ID", id);

        Match match = matchService.getMatchById(id);

        logger.info("Match found at venue: {}", match.getVenue());
        logger.debug("Match details: {}", match);

        ApiResponse<Match> response = ApiResponse.success(
                "Match retrieved successfully",
                match);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new match", description = "Creates a new match between two teams. Both teams must exist in the database. Result should only be provided if status is COMPLETED.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Match created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input - Validation failed or teams are invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Team not found")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Match object to be created", required = true, content = @Content(schema = @Schema(implementation = Match.class), examples = {
            @ExampleObject(name = "Scheduled Match", summary = "Create a scheduled match", value = """
                    {
                        "venue": "Wankhede Stadium, Mumbai",
                        "date": "2024-04-15T19:30:00",
                        "firstTeam": "64a1b2c3d4e5f6g7h8i9j0k2",
                        "secondTeam": "64a1b2c3d4e5f6g7h8i9j0k3",
                        "status": "SCHEDULED",
                        "result": null
                    }
                    """),
            @ExampleObject(name = "Completed Match", summary = "Create a completed match with result", value = """
                    {
                        "venue": "M. Chinnaswamy Stadium, Bangalore",
                        "date": "2024-04-10T15:30:00",
                        "firstTeam": "64a1b2c3d4e5f6g7h8i9j0k2",
                        "secondTeam": "64a1b2c3d4e5f6g7h8i9j0k3",
                        "status": "COMPLETED",
                        "result": {
                            "winner": "64a1b2c3d4e5f6g7h8i9j0k2",
                            "margin": "45 runs",
                            "manOfTheMatchId": "64a1b2c3d4e5f6g7h8i9j0k4"
                        }
                    }
                    """)
    }))
    @PostMapping
    public ResponseEntity<ApiResponse<Match>> addPlayer(@Valid @RequestBody Match match) {

        logger.info("POST /api/matches - Creating new match");
        logger.info("Venue: {}, Teams: {} vs {}", match.getVenue(), match.getFirstTeam(), match.getSecondTeam());
        logger.debug("Full match details: {}", match);

        Match matchAdded = matchService.create(match);

        logger.info("Match created successfully with ID: {}", matchAdded.getId());

        ApiResponse<Match> response = ApiResponse.success("match created successully", matchAdded);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a match", description = "Deletes a match from the database")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Match>> deleteMatch(
            @Parameter(description = "Match ID to delete", required = true) @PathVariable String id) {

        logger.info("DELETE /api/matches/{} - Deleting match", id);
        logger.warn("Attempting to delete match with ID: {}", id);

        Match match = matchService.deleteMatch(id);

        logger.info("Match at '{}' deleted successfully", match.getVenue());

        ApiResponse<Match> response = ApiResponse.success("Match deleted successfully", match);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a match (Full Update)", description = "Updates all fields of an existing match. All fields will be replaced with new values.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or invalid team/result data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Match>> updateMatch(
            @Parameter(description = "Match ID to update", required = true) @PathVariable String id,
            @Valid @RequestBody Match matchDetails) {

        logger.info("PUT /api/matches/update/{} - Full update request", id);
        logger.info("Updating match with venue: {}, status: {}", matchDetails.getVenue(), matchDetails.getStatus());
        logger.debug("Full update details: {}", matchDetails);

        Match updatedMatch = matchService.updateMatch(id, matchDetails);

        logger.info("Match {} updated successfully", id);

        ApiResponse<Match> response = ApiResponse.success(
                "Match updated successfully",
                updatedMatch);

        return ResponseEntity.ok(response);

    }

    @Operation(summary = "Partially update a match", description = "Updates only the provided fields of an existing match. Fields not included in the request will remain unchanged.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Fields to update (only include fields you want to change)", content = @Content(examples = @ExampleObject(name = "Partial Update Example", value = """
            {
                "venue": "Updated Stadium Name",
                "status": "LIVE"
            }
            """)))
    @PatchMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Match>> partialUpdateMatch(
            @Parameter(description = "Match ID to update", required = true) @PathVariable String id,
            @Valid @RequestBody Match matchDetails) {

        logger.info("PATCH /api/matches/update/{} - Partial update request", id);
        logger.debug("Partial update details: {}", matchDetails);

        Match updatedMatch = matchService.patchMatch(id, matchDetails);

        logger.info("Match {} partially updated successfully", id);

        ApiResponse<Match> response = ApiResponse.success(
                "Match updated successfully",
                updatedMatch);

        return ResponseEntity.ok(response);
    }
}
