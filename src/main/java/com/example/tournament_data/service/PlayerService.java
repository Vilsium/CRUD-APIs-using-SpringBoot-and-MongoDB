package com.example.tournament_data.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Player;
import com.example.tournament_data.model.Team;
import com.example.tournament_data.repository.PlayerRepository;
import com.example.tournament_data.repository.TeamRepository;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    public Player create(Player player) {
        Player savedPlayer = playerRepository.save(player);

        if (savedPlayer.getTeamId() != null && !savedPlayer.getTeamId().isEmpty()) {
            Team team = teamRepository.findById(savedPlayer.getTeamId()).orElseThrow(
                    () -> new InvalidRequestException("teamId", "Team not found with id: " + savedPlayer.getTeamId()));
            if (!team.getPlayerIds().contains(savedPlayer.getId())) {
                team.getPlayerIds().add(savedPlayer.getId());
                teamRepository.save(team);
            }
        }

        return savedPlayer;
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player getPlayerById(String id) {
        return playerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));
    }

    public Player updatePlayer(String id, Player playerDetails) {
        // Find existing player
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        // Update fields
        existingPlayer.setName(playerDetails.getName());
        existingPlayer.setRole(playerDetails.getRole());
        existingPlayer.setBattingStyle(playerDetails.getBattingStyle());
        existingPlayer.setBowlingStyle(playerDetails.getBowlingStyle());
        existingPlayer.setTeamId(playerDetails.getTeamId());
        existingPlayer.setStats(playerDetails.getStats());

        // Save and return
        return playerRepository.save(existingPlayer);
    }

    public Player patchPlayer(String id, Player playerDetails) {

        // Step 1: Find existing player
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));

        // Step 2: Update only non-null fields
        if (playerDetails.getName() != null) {
            existingPlayer.setName(playerDetails.getName());
        }

        if (playerDetails.getRole() != null) {
            existingPlayer.setRole(playerDetails.getRole());
        }

        if (playerDetails.getBattingStyle() != null) {
            existingPlayer.setBattingStyle(playerDetails.getBattingStyle());
        }

        if (playerDetails.getBowlingStyle() != null) {
            existingPlayer.setBowlingStyle(playerDetails.getBowlingStyle());
        }

        if (playerDetails.getTeamId() != null) {
            existingPlayer.setTeamId(playerDetails.getTeamId());
        }

        if (playerDetails.getStats() != null) {
            existingPlayer.setStats(playerDetails.getStats());
        }

        // Step 3: Save and return
        return playerRepository.save(existingPlayer);
    }

    public Player deletePlayer(String id) {
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));
        if (existingPlayer.getTeamId() != null && !existingPlayer.getTeamId().isEmpty()) {
            teamRepository.findById(existingPlayer.getTeamId()).ifPresent(team -> {

                // Remove player ID from team's playerIds
                team.getPlayerIds().remove(id);

                // If deleted player was captain, clear captainId
                if (id.equals(team.getCaptainId())) {
                    team.setCaptainId(null);
                }

                teamRepository.save(team);
            });
        }
        playerRepository.deleteById(id);
        return existingPlayer;
    }
}
