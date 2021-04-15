package com.dartsmatcher.dartsmatcherapi.features.livematch;

import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import org.bson.types.ObjectId;

public interface ILiveMatchService {

	X01Match getLiveMatch(ObjectId matchId);

	X01Match updateLiveMatch(X01Throw x01Throw);

	X01Match deleteThrowLiveMatch(X01DeleteThrow x01DeleteThrow);

	X01Match deleteSetLiveMatch(X01DeleteSet x01DeleteThrow);

	X01Match deleteLegLiveMatch(X01DeleteLeg x01DeleteThrow);

}
