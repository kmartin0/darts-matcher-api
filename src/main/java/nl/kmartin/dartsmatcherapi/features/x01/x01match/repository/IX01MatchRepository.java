package nl.kmartin.dartsmatcherapi.features.x01.x01match.repository;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

/**
 * Provides MongoDB persistence for X01 matches.
 */
public interface IX01MatchRepository extends MongoRepository<X01Match, ObjectId> {
    /**
     * Finds matches that reference the supplied match as their rematch.
     *
     * @param rematchId the referenced match id
     * @return the matches referencing that rematch
     */
    List<X01Match> findAllByRematchId(ObjectId rematchId);

    /**
     * Finds the existing match IDs among the supplied IDs.
     *
     * @param matchIds the match IDs to check
     * @return projections containing the existing match IDs
     */
    @Query(value = "{ '_id': { '$in': ?0 } }", fields = "{ '_id': 1 }")
    List<MatchIdProjection> findExistingIds(Collection<ObjectId> matchIds);

    /**
     * Contains only a match's persisted identity.
     */
    interface MatchIdProjection {
        ObjectId getId();
    }
}