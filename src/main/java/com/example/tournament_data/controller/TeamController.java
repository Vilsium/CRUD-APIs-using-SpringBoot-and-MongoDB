package com.example.tournament_data.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tournament_data.dto.ApiResponse;
import com.example.tournament_data.dto.TeamDetailsResponse;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.service.TeamService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Team>>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();

        ApiResponse<List<Team>> response = ApiResponse.success("Team retreived successfully", teams);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> getTeamById(@PathVariable String id) {
        Team team = teamService.getTeamById(id);
        if (team == null) {
            ApiResponse<Team> response = ApiResponse.error("Team not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<Team> response = ApiResponse.success(
                "Team retrieved successfully",
                team);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<ApiResponse<TeamDetailsResponse>> getTeamDetails(@PathVariable String id) {
        TeamDetailsResponse teamDetails = teamService.getTeamDetails(id);

        if (teamDetails == null) {
            ApiResponse<TeamDetailsResponse> response = ApiResponse.error("Team not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<TeamDetailsResponse> response = ApiResponse.success(
                "Team details retrieved successfully",
                teamDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Team>> addTeam(@RequestBody Team team) {
        // TODO: process POST request
        try {
            Team addedTeam = teamService.create(team);
            ApiResponse<Team> response = ApiResponse.success("Team created successully", addedTeam);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<Team> response = ApiResponse.error("Failed to add player: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> deletePlayer(@PathVariable String id) {
        try {
            Team team = teamService.deleteTeam(id);
            ApiResponse<Team> response = ApiResponse.success("Team deleted successfully", team);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Team> response = ApiResponse.error(
                    "Team not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Team>> updatePlayer(@PathVariable String id,
            @RequestBody Team teamDetails) {
        // TODO: process PUT request
        try {
            Team updatedTeam = teamService.updateTeam(id, teamDetails);

            ApiResponse<Team> response = ApiResponse.success(
                    "Team updated successfully",
                    updatedTeam);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Team> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Team>> partialUpdatePlayer(@PathVariable String id,
            @RequestBody Team teamDetails) {
        // TODO: process PUT request
        try {
            Team updatedTeam = teamService.patchTeam(id, teamDetails);

            ApiResponse<Team> response = ApiResponse.success(
                    "Team updated successfully",
                    updatedTeam);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Team> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
