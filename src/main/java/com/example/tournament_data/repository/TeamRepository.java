package com.example.tournament_data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.tournament_data.model.Team;

@Repository
public interface TeamRepository extends MongoRepository<Team, String> {
    
}
