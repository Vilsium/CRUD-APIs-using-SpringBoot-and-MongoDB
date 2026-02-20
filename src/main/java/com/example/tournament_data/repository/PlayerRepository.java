package com.example.tournament_data.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.example.tournament_data.dto.RoleCount;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.tournament_data.model.Player;

@Repository
public interface PlayerRepository extends MongoRepository<Player, Integer> {
    List<Player> findByTeamId(Integer id);

    Optional<Player> findByNameIgnoreCase(String name);

    @Aggregation(pipeline = {
            "{ $match: { teamId: ?0 } }",
            "{ $group: { _id: '$role', count: { $sum: 1 } } }",
            "{ $project: { _id: 0, role: '$_id', count: 1 } }"
    })
    List<RoleCount> findRoleCount(Integer id);

    List<Player> findByIdIn(Collection<Integer> ids);
}
