package com.example.tournament_data.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tournament_data.dto.ApiResponse;
import com.example.tournament_data.model.Match;
import com.example.tournament_data.service.MatchService;

import java.util.List;

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
public class MatchController {
    @Autowired
    private MatchService matchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Match>>> getAllMatches() {
        List<Match> macthes = matchService.getAllMatches();
        ApiResponse<List<Match>> response = ApiResponse.success("Matches retreived successfully", macthes);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Match>> getMatch(@PathVariable String id) {
        Match match = matchService.getMatchById(id);
        if (match == null) {
            ApiResponse<Match> response = ApiResponse.error("Match not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<Match> response = ApiResponse.success(
                "Match retrieved successfully",
                match);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Match>> addPlayer(@RequestBody Match match) {
        // TODO: process POST request
        try {
            Match matchAdded = matchService.create(match);
            ApiResponse<Match> response = ApiResponse.success("match created successully", matchAdded);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<Match> response = ApiResponse.error("Failed to add match: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Match>> deleteMatch(@PathVariable String id) {
        try {
            Match match = matchService.deleteMatch(id);
            ApiResponse<Match> response = ApiResponse.success("Match deleted successfully", match);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Match> response = ApiResponse.error(
                    "Match not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Match>> updateMatch(@PathVariable String id,
            @RequestBody Match matchDetails) {
        // TODO: process PUT request
        try {
            Match updatedMatch = matchService.updateMatch(id, matchDetails);

            ApiResponse<Match> response = ApiResponse.success(
                    "Match updated successfully",
                    updatedMatch);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Match> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Match>> partialUpdateMatch(@PathVariable String id,
            @RequestBody Match matchDetails) {
        // TODO: process PUT request
        try {
            Match updatedMatch = matchService.patchMatch(id, matchDetails);

            ApiResponse<Match> response = ApiResponse.success(
                    "Match updated successfully",
                    updatedMatch);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Match> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
