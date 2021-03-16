package com.dartsmatcher.dartsmatcherapi.features.match;

import com.dartsmatcher.dartsmatcherapi.features.match.models.Match;
import com.dartsmatcher.dartsmatcherapi.features.match.models.MatchPlayers;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;

public interface MatchRepository extends MongoRepository<Match, ObjectId> {
	ArrayList<Match> findAll();
//	ArrayList<Match> findAllByPlayersContaining(@NotEmpty MatchPlayers players);
	ArrayList<Match> findAllByPlayersRegisteredContaining(ObjectId player);
}
