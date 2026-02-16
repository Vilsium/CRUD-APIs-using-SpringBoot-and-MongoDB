package com.example.tournament_data.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.tournament_data.model.Match;

@Repository
public interface MatchRepository extends MongoRepository<Match, Integer> {
    List<Match> findByFirstTeam(String firstTeam);

    List<Match> findBySecondTeam(String secondTeam);

    List<Match> findByVenue(String venue);
}
