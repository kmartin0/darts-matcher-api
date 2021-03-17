package com.dartsmatcher.dartsmatcherapi.features.livematch;

import com.dartsmatcher.dartsmatcherapi.features.match.MatchRepository;
import com.dartsmatcher.dartsmatcherapi.features.match.models.Match;
import com.dartsmatcher.dartsmatcherapi.utils.MatchUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class LiveMatchServiceImpl implements ILiveMatchService {

	private final MatchRepository matchRepository;

	public LiveMatchServiceImpl(MatchRepository matchRepository) {
		this.matchRepository = matchRepository;
	}

	@Override
	public Match getLiveMatch(ObjectId matchId) {
		return matchRepository.findById(matchId).orElse(null);
	}

	@Override
	public Match updateLiveMatch(Match match, ObjectId matchId) {
		// Check if the match id is equal to the match id.
		if (!matchId.equals(match.getId())) throw new IllegalArgumentException("matchId doesn't equal mapping id.");

		// Update match statistics and result.
		MatchUtils.updateMatchStatistics(match);

		// Save and return the updated match.
		return matchRepository.save(match);
	}
}
