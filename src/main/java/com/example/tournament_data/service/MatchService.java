package com.example.tournament_data.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tournament_data.model.Match;
import com.example.tournament_data.repository.MatchRepository;

@Service
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;

    public Match create(Match match) {
        return matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Match getMatchById(String id) {
        return matchRepository.findById(id).orElse(null);
    }

    public Match updateMatch(String id, Match matchDetails) {
        Match existingMatch = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found with id : " + id));

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
                .orElseThrow(() -> new RuntimeException("Match not found with id : " + id));

        if (matchDetails.getVenue() != null) {
            existingMatch.setVenue(matchDetails.getVenue());
        }

        if (matchDetails.getDate() != null) {
            existingMatch.setDate(matchDetails.getDate());
        }

        if (matchDetails.getFirstTeam() != null) {
            existingMatch.setFirstTeam(matchDetails.getFirstTeam());
        }

        if (matchDetails.getSecondTeam() != null) {
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
        Match existingMatch = matchRepository.findById(id).orElse(null);
        matchRepository.deleteById(id);
        return existingMatch;
    }
}
