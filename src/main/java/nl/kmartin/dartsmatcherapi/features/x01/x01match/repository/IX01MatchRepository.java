package nl.kmartin.dartsmatcherapi.features.x01.x01match.repository;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Provides MongoDB persistence for X01 matches.
 */
public interface IX01MatchRepository extends MongoRepository<X01Match, ObjectId> {
}