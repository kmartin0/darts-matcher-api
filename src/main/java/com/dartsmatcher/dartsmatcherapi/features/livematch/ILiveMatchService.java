package com.dartsmatcher.dartsmatcherapi.features.livematch;

import com.dartsmatcher.dartsmatcherapi.features.match.models.Match;
import org.bson.types.ObjectId;

public interface ILiveMatchService {

	Match getLiveMatch(ObjectId matchId);

	Match updateLiveMatch(Match match, ObjectId matchId);

}
