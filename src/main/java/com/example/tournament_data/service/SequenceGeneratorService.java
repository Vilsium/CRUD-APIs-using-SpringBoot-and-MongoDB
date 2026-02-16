package com.example.tournament_data.service;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.example.tournament_data.model.DatabaseSequence;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;

@Service
public class SequenceGeneratorService {

    private final MongoOperations mongoOperations;

    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    /**
     * Generate next sequence value for given sequence name
     */
    public Integer generateSequence(String seqName) {
        DatabaseSequence counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq", 1),
                options().returnNew(true).upsert(true),
                DatabaseSequence.class);

        return counter != null ? counter.getSeq() : 1;
    }

    /**
     * Get current sequence value without incrementing
     */
    public Integer getCurrentSequence(String seqName) {
        DatabaseSequence counter = mongoOperations.findOne(
                Query.query(Criteria.where("_id").is(seqName)),
                DatabaseSequence.class);

        return counter != null ? counter.getSeq() : 0;
    }

    /**
     * Reset sequence to a specific value (useful for testing)
     */
    public void resetSequence(String seqName, Integer value) {
        mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(seqName)),
                new Update().set("seq", value),
                options().upsert(true),
                DatabaseSequence.class);
    }
}
