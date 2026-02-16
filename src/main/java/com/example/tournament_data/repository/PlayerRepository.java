package com.example.tournament_data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.tournament_data.model.Player;

@Repository
public interface PlayerRepository extends MongoRepository<Player, Integer> {
    List<Player> findByTeamId(Integer id);

    Optional<Player> findByNameIgnoreCase(String name);
}
