package com.example.tournament_data.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tournament_data.dto.ApiResponse;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.service.PlayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Player", description = "Player management APIs for cricket tournament")
public class PlayerController {
        private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

        @Autowired
        private PlayerService playerService;

        @Operation(summary = "Get all players", description = "Retrieves a list of all players in the tournament database")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Players retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping
        public ResponseEntity<ApiResponse<List<Player>>> getAllPlayers() {
                logger.info("GET /api/players - Fetching all players");

                List<Player> players = playerService.getAllPlayers();

                logger.info("Found {} players", players.size());
                logger.debug("Players: {}", players);

                ApiResponse<List<Player>> response = ApiResponse.success("Players retrieved successfully", players);

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get player by ID", description = "Retrieves a specific player by their unique ID")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Player not found")
        })
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<Player>> getPlayerById(
                        @Parameter(description = "Player ID", required = true, example = "64a1b2c3d4e5f6g7h8i9j0k1") @PathVariable String id) {

                logger.info("GET /api/players/{} - Fetching player by ID", id);

                Player player = playerService.getPlayerById(id);

                logger.info("Player found: {}", player.getName());
                logger.debug("Player details: {}", player);

                ApiResponse<Player> response = ApiResponse.success(
                                "Player retrieved successfully", player);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Create a new player", description = "Creates a new player in the tournament database. If teamId is provided, the player will be added to that team.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Player created successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input - Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid teamId - Team not found")
        })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Player object to be created", required = true, content = @Content(schema = @Schema(implementation = Player.class), examples = @ExampleObject(name = "Sample Player", value = """
                        {
                            "name": "Virat Kohli",
                            "role": "Batsman",
                            "battingStyle": "Right-Handed",
                            "bowlingStyle": "Right-Arm Medium",
                            "teamId": null,
                            "stats": {
                                "matches": 100,
                                "runs": 5000,
                                "wickets": 10,
                                "catches": 50
                            }
                        }
                        """)))
        @PostMapping
        public ResponseEntity<ApiResponse<Player>> addPlayer(@Valid @RequestBody Player player) {
                logger.info("POST /api/players - Creating new player");
                logger.info("Player name: {}, Role: {}", player.getName(), player.getRole());
                logger.debug("Full player details: {}", player);

                Player playerAdded = playerService.create(player);

                logger.info("Player created successfully with ID: {}", playerAdded.getId());

                ApiResponse<Player> response = ApiResponse.success(
                                "Player created successfully", playerAdded);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @Operation(summary = "Delete a player", description = "Deletes a player from the database. Also removes the player from their team if assigned.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player deleted successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Player not found")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Player>> deletePlayer(
                        @Parameter(description = "Player ID to delete", required = true) @PathVariable String id) {
                logger.info("DELETE /api/players/{} - Deleting player", id);
                logger.warn("Attempting to delete player with ID: {}", id);

                Player player = playerService.deletePlayer(id);

                logger.info("Player '{}' deleted successfully", player.getName());

                ApiResponse<Player> response = ApiResponse.success(
                                "Player deleted successfully", player);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Update a player (Full Update)", description = "Updates all fields of an existing player. All fields will be replaced with new values.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player updated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Player not found")
        })
        @PutMapping("/update/{id}")
        public ResponseEntity<ApiResponse<Player>> updatePlayer(
                        @Parameter(description = "Player ID to update", required = true) @PathVariable String id,
                        @Valid @RequestBody Player playerDetails) {
                logger.info("PUT /api/players/update/{} - Full update request", id);
                logger.info("Updating player with new name: {}, role: {}", playerDetails.getName(),
                                playerDetails.getRole());
                logger.debug("Full update details: {}", playerDetails);

                Player updatedPlayer = playerService.updatePlayer(id, playerDetails);

                logger.info("Player {} updated successfully", id);

                ApiResponse<Player> response = ApiResponse.success(
                                "Player updated successfully", updatedPlayer);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Partially update a player", description = "Updates only the provided fields of an existing player. Fields not included in the request will remain unchanged.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player updated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Player not found")
        })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Fields to update (only include fields you want to change)", content = @Content(examples = @ExampleObject(name = "Partial Update Example", value = """
                        {
                            "name": "Updated Name",
                            "role": "All-Rounder"
                        }
                        """)))
        @PatchMapping("/update/{id}")
        public ResponseEntity<ApiResponse<Player>> partialUpdatePlayer(
                        @Parameter(description = "Player ID to update", required = true) @PathVariable String id,
                        @Valid @RequestBody Player playerDetails) {
                logger.info("PATCH /api/players/update/{} - Partial update request", id);
                logger.debug("Partial update details: {}", playerDetails);

                Player updatedPlayer = playerService.patchPlayer(id, playerDetails);

                logger.info("Player {} partially updated successfully", id);

                ApiResponse<Player> response = ApiResponse.success(
                                "Player updated successfully", updatedPlayer);
                return ResponseEntity.ok(response);
        }
}
