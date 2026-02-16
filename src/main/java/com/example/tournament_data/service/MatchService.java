package com.example.tournament_data.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tournament_data.exception.InvalidRequestException;
import com.example.tournament_data.exception.ResourceNotFoundException;
import com.example.tournament_data.model.Match;
import com.example.tournament_data.repository.MatchRepository;
import com.example.tournament_data.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public Match create(Match match) {
        if (!teamRepository.existsById(match.getFirstTeam())) {
            throw new InvalidRequestException("firstTeam",
                    "Team not found with id: " + match.getFirstTeam());
        }
        if (!teamRepository.existsById(match.getSecondTeam())) {
            throw new InvalidRequestException("secondTeam",
                    "Team not found with id: " + match.getSecondTeam());
        }
        return matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Match getMatchById(String id) {
        return matchRepository.findById(id).orElseThrow(() -> {
            return new ResourceNotFoundException("Match", "id", id);
        });
    }

    public Match updateMatch(String id, Match matchDetails) {
        Match existingMatch = matchRepository.findById(id)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Match", "id", id);
                });

        if (!teamRepository.existsById(matchDetails.getFirstTeam())) {
            throw new InvalidRequestException("firstTeam",
                    "Team not found with id: " + matchDetails.getFirstTeam());
        }
        if (!teamRepository.existsById(matchDetails.getSecondTeam())) {
            throw new InvalidRequestException("secondTeam",
                    "Team not found with id: " + matchDetails.getSecondTeam());
        }

        existingMatch.setVenue(matchDetails.getVenue());
        existingMatch.setDate(matchDetails.getDate());
        existingMatch.setFirstTeam(matchDetails.getFirstTeam());
        existingMatch.setSecondTeam(matchDetails.getSecondTeam());
        existingMatch.setStatus(matchDetails.getStatus());
        existingMatch.setResult(matchDetails.getResult());

        return matchRepository.save(existingMatch);
    }

    public Match patchMatch(String id, Match matchDetails) {
        Match existingMatch = matchRepository.findById(id)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Match", "id", id);
                });

        if (matchDetails.getVenue() != null) {
            existingMatch.setVenue(matchDetails.getVenue());
        }

        if (matchDetails.getDate() != null) {
            existingMatch.setDate(matchDetails.getDate());
        }

        if (matchDetails.getFirstTeam() != null) {
            if (!teamRepository.existsById(matchDetails.getFirstTeam())) {
                throw new InvalidRequestException("firstTeam",
                        "Team not found with id: " + matchDetails.getFirstTeam());
            }
            existingMatch.setFirstTeam(matchDetails.getFirstTeam());
        }

        if (matchDetails.getSecondTeam() != null) {
            if (!teamRepository.existsById(matchDetails.getSecondTeam())) {
                throw new InvalidRequestException("secondTeam",
                        "Team not found with id: " + matchDetails.getSecondTeam());
            }
            existingMatch.setSecondTeam(matchDetails.getSecondTeam());
        }

        if (matchDetails.getStatus() != null) {
            existingMatch.setStatus(matchDetails.getStatus());
        }

        if (matchDetails.getResult() != null) {
            existingMatch.setResult(matchDetails.getResult());
        }

        return matchRepository.save(existingMatch);
    }

    public Match deleteMatch(String id) {
        Match existingMatch = matchRepository.findById(id).orElseThrow(() -> {
            return new ResourceNotFoundException("Match", "id", id);
        });
        matchRepository.deleteById(id);
        return existingMatch;
    }
}
