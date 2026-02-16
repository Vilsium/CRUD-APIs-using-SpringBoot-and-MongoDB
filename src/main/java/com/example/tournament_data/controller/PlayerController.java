package com.example.tournament_data.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.tournament_data.dto.ApiResponse;
import com.example.tournament_data.dto.PlayerCreateRequest;
import com.example.tournament_data.dto.PlayerPatchRequest;
import com.example.tournament_data.dto.PlayerResponse;
import com.example.tournament_data.service.PlayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Player", description = "Player management APIs")
public class PlayerController {

        private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

        private final PlayerService playerService;

        public PlayerController(PlayerService playerService) {
                this.playerService = playerService;
        }

        @PostMapping
        @Operation(summary = "Create a new player", description = "Create a new player and assign to a team")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Player created successfully", content = @Content(schema = @Schema(implementation = PlayerResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
        })
        public ResponseEntity<ApiResponse<PlayerResponse>> createPlayer(
                        @Valid @RequestBody PlayerCreateRequest request) {

                logger.info("POST /api/players - Creating player: {}", request.getName());

                PlayerResponse createdPlayer = playerService.create(request);

                logger.info("Player created successfully with ID: {}", createdPlayer.getId());

                ApiResponse<PlayerResponse> response = ApiResponse.success(
                                "Player created successfully", createdPlayer);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        @GetMapping
        @Operation(summary = "Get all players", description = "Retrieve all players")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Players retrieved successfully")
        })
        public ResponseEntity<ApiResponse<List<PlayerResponse>>> getAllPlayers() {

                logger.info("GET /api/players - Fetching all players");

                List<PlayerResponse> players = playerService.getAllPlayers();

                logger.info("Found {} players", players.size());

                ApiResponse<List<PlayerResponse>> response = ApiResponse.success(
                                "Players retrieved successfully", players);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get player by ID", description = "Retrieve a player by their unique identifier")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player retrieved successfully", content = @Content(schema = @Schema(implementation = PlayerResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Player not found")
        })
        public ResponseEntity<ApiResponse<PlayerResponse>> getPlayerById(
                        @Parameter(description = "Player ID", required = true, example = "1") @PathVariable Integer id) {

                logger.info("GET /api/players/{} - Fetching player by ID", id);

                PlayerResponse player = playerService.getPlayerById(id);

                logger.info("Player found: {}", player.getName());

                ApiResponse<PlayerResponse> response = ApiResponse.success(
                                "Player retrieved successfully", player);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update player", description = "Fully update an existing player")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player updated successfully", content = @Content(schema = @Schema(implementation = PlayerResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Player not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
        })
        public ResponseEntity<ApiResponse<PlayerResponse>> updatePlayer(
                        @Parameter(description = "Player ID", required = true, example = "1") @PathVariable Integer id,
                        @Valid @RequestBody PlayerCreateRequest request) {

                logger.info("PUT /api/players/{} - Updating player", id);

                PlayerResponse updatedPlayer = playerService.updatePlayer(id, request);

                logger.info("Player updated successfully: {}", updatedPlayer.getName());

                ApiResponse<PlayerResponse> response = ApiResponse.success(
                                "Player updated successfully", updatedPlayer);
                return ResponseEntity.ok(response);
        }

        @PatchMapping("/{id}")
        @Operation(summary = "Partially update player", description = "Partially update an existing player")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player updated successfully", content = @Content(schema = @Schema(implementation = PlayerResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Player not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
        })
        public ResponseEntity<ApiResponse<PlayerResponse>> patchPlayer(
                        @Parameter(description = "Player ID", required = true, example = "1") @PathVariable Integer id,
                        @Valid @RequestBody PlayerPatchRequest request) {

                logger.info("PATCH /api/players/{} - Partially updating player", id);

                PlayerResponse updatedPlayer = playerService.patchPlayer(id, request);

                logger.info("Player patched successfully: {}", updatedPlayer.getName());

                ApiResponse<PlayerResponse> response = ApiResponse.success(
                                "Player updated successfully", updatedPlayer);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete player", description = "Delete a player by their ID")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Player deleted successfully", content = @Content(schema = @Schema(implementation = PlayerResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Player not found")
        })
        public ResponseEntity<ApiResponse<PlayerResponse>> deletePlayer(
                        @Parameter(description = "Player ID", required = true, example = "1") @PathVariable Integer id) {

                logger.info("DELETE /api/players/{} - Deleting player", id);

                PlayerResponse deletedPlayer = playerService.deletePlayer(id);

                logger.info("Player deleted successfully: {}", deletedPlayer.getName());

                ApiResponse<PlayerResponse> response = ApiResponse.success(
                                "Player deleted successfully", deletedPlayer);
                return ResponseEntity.ok(response);
        }
}
