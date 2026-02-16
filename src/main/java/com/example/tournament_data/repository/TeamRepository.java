package com.example.tournament_data.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.tournament_data.model.Team;

@Repository
public interface TeamRepository extends MongoRepository<Team, Integer> {
    Optional<Team> findByTeamNameIgnoreCase(String teamName);
}
