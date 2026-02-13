package com.example.tournament_data.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tournament_data.dto.ApiResponse;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.service.PlayerService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Player>>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();

        ApiResponse<List<Player>> response = ApiResponse.success("Players retreived successfully", players);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Player>> getPlayerByid(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        if (player == null) {
            ApiResponse<Player> response = ApiResponse.error("Player not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<Player> response = ApiResponse.success(
                "Player retrieved successfully",
                player);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Player>> addPlayer(@RequestBody Player player) {
        // TODO: process POST request
        try {
            Player playerAdded = playerService.create(player);
            ApiResponse<Player> response = ApiResponse.success("Player created successully", playerAdded);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<Player> response = ApiResponse.error("Failed to add player: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Player>> deletePlayer(@PathVariable String id) {
        try {
            Player player = playerService.deletePlayer(id);
            ApiResponse<Player> response = ApiResponse.success("Player deleted successfully", player);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Player> response = ApiResponse.error(
                    "Player not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Player>> updatePlayer(@PathVariable String id,
            @RequestBody Player playerDetails) {
        // TODO: process PUT request
        try {
            Player updatedPlayer = playerService.updatePlayer(id, playerDetails);

            ApiResponse<Player> response = ApiResponse.success(
                    "Player updated successfully",
                    updatedPlayer);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Player> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Player>> partialUpdatePlayer(@PathVariable String id,
            @RequestBody Player playerDetails) {
        // TODO: process PUT request
        try {
            Player updatedPlayer = playerService.patchPlayer(id, playerDetails);

            ApiResponse<Player> response = ApiResponse.success(
                    "Player updated successfully",
                    updatedPlayer);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Player> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
