package com.example.tournament_data.controller;

import java.util.List;

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

import com.example.tournament_data.dto.ApiResponse;
import com.example.tournament_data.dto.MatchCreateRequest;
import com.example.tournament_data.dto.MatchPatchRequest;
import com.example.tournament_data.dto.MatchResponse;
import com.example.tournament_data.service.MatchService;

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
@RequestMapping("/api/v1/matches")
@Tag(name = "Match", description = "Match management APIs for cricket tournament")
@RequiredArgsConstructor
public class MatchController {

        private static final Logger logger = LoggerFactory.getLogger(MatchController.class);

        private final MatchService matchService;

        @Operation(summary = "Get all matches", description = "Retrieves a list of all matches in the tournament")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Matches retrieved successfully", content = @Content(schema = @Schema(implementation = MatchResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping
        public ResponseEntity<ApiResponse<List<MatchResponse>>> getAllMatches() {

                logger.info("GET /api/v1/matches - Fetching all matches");

                List<MatchResponse> matches = matchService.getAllMatches();

                logger.info("Found {} matches", matches.size());

                ApiResponse<List<MatchResponse>> response = ApiResponse.success(
                                "Matches retrieved successfully", matches);

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get match by ID", description = "Retrieves a match by its unique identifier")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match retrieved successfully", content = @Content(schema = @Schema(implementation = MatchResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
        })
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<MatchResponse>> getMatchById(
                        @Parameter(description = "Match ID", required = true, example = "1") @PathVariable Integer id) {

                logger.info("GET /api/v1/matches/{} - Fetching match by ID", id);

                MatchResponse match = matchService.getMatchById(id);

                logger.info("Match found: {} vs {} at {}",
                                match.getFirstTeamName(), match.getSecondTeamName(), match.getVenue());

                ApiResponse<MatchResponse> response = ApiResponse.success(
                                "Match retrieved successfully", match);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Create a new match", description = "Creates a new match in the tournament")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Match created successfully", content = @Content(schema = @Schema(implementation = MatchResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input - Validation failed")
        })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Match object to be created", required = true, content = @Content(schema = @Schema(implementation = MatchCreateRequest.class), examples = {
                        @ExampleObject(name = "Scheduled Match", value = """
                                        {
                                            "venue": "Wankhede Stadium, Mumbai",
                                            "date": "2024-04-15T19:30:00",
                                            "firstTeamName": "Mumbai Indians",
                                            "secondTeamName": "Chennai Super Kings",
                                            "status": "SCHEDULED"
                                        }
                                        """),
                        @ExampleObject(name = "Completed Match", value = """
                                        {
                                            "venue": "M. A. Chidambaram Stadium",
                                            "date": "2024-04-10T19:30:00",
                                            "firstTeamName": "Chennai Super Kings",
                                            "secondTeamName": "Mumbai Indians",
                                            "status": "COMPLETED",
                                            "result": {
                                                "winner": "Chennai Super Kings",
                                                "margin": "5 wickets",
                                                "manOfTheMatchName": "MS Dhoni"
                                            }
                                        }
                                        """)
        }))
        @PostMapping
        public ResponseEntity<ApiResponse<MatchResponse>> createMatch(
                        @Valid @RequestBody MatchCreateRequest request) {

                logger.info("POST /api/v1/matches - Creating match: {} vs {}",
                                request.getFirstTeamName(), request.getSecondTeamName());

                MatchResponse createdMatch = matchService.create(request);

                logger.info("Match created successfully with ID: {}", createdMatch.getId());

                ApiResponse<MatchResponse> response = ApiResponse.success(
                                "Match created successfully", createdMatch);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @Operation(summary = "Update a match (Full Update)", description = "Updates all fields of an existing match")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match updated successfully", content = @Content(schema = @Schema(implementation = MatchResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
        })
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<MatchResponse>> updateMatch(
                        @Parameter(description = "Match ID to update", required = true, example = "1") @PathVariable Integer id,
                        @Valid @RequestBody MatchCreateRequest request) {

                logger.info("PUT /api/v1/matches/{} - Full update request", id);

                MatchResponse updatedMatch = matchService.updateMatch(id, request);

                logger.info("Match {} updated successfully", id);

                ApiResponse<MatchResponse> response = ApiResponse.success(
                                "Match updated successfully", updatedMatch);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Partially update a match", description = "Updates only the provided fields of an existing match")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match updated successfully", content = @Content(schema = @Schema(implementation = MatchResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
        })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Fields to update", content = @Content(examples = {
                        @ExampleObject(name = "Update Venue", value = """
                                        {
                                            "venue": "New Stadium Name"
                                        }
                                        """),
                        @ExampleObject(name = "Complete Match", value = """
                                        {
                                            "status": "COMPLETED",
                                            "result": {
                                                "winner": "Mumbai Indians",
                                                "margin": "10 runs",
                                                "manOfTheMatchName": "Rohit Sharma"
                                            }
                                        }
                                        """)
        }))
        @PatchMapping("/{id}")
        public ResponseEntity<ApiResponse<MatchResponse>> patchMatch(
                        @Parameter(description = "Match ID to update", required = true, example = "1") @PathVariable Integer id,
                        @Valid @RequestBody MatchPatchRequest request) {

                logger.info("PATCH /api/v1/matches/{} - Partial update request", id);

                MatchResponse updatedMatch = matchService.patchMatch(id, request);

                logger.info("Match {} partially updated successfully", id);

                ApiResponse<MatchResponse> response = ApiResponse.success(
                                "Match updated successfully", updatedMatch);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Delete a match", description = "Deletes a match from the database")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Match deleted successfully", content = @Content(schema = @Schema(implementation = MatchResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<MatchResponse>> deleteMatch(
                        @Parameter(description = "Match ID to delete", required = true, example = "1") @PathVariable Integer id) {

                logger.info("DELETE /api/v1/matches/{} - Deleting match", id);

                MatchResponse deletedMatch = matchService.deleteMatch(id);

                logger.info("Match {} vs {} deleted successfully",
                                deletedMatch.getFirstTeamName(), deletedMatch.getSecondTeamName());

                ApiResponse<MatchResponse> response = ApiResponse.success(
                                "Match deleted successfully", deletedMatch);
                return ResponseEntity.ok(response);
        }
}
