package com.example.tournament_data.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.tournament_data.model.Player;

@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {
    List<Player> findByTeamId(String id);
}
