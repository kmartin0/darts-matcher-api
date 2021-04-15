package com.dartsmatcher.dartsmatcherapi.features.livematch;

import com.dartsmatcher.dartsmatcherapi.features.match.MatchPlayer;
import com.dartsmatcher.dartsmatcherapi.features.x01match.X01MatchRepository;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01Leg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01LegRound;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.set.X01Set;
import com.dartsmatcher.dartsmatcherapi.utils.X01TimelineUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
public class LiveMatchServiceImpl implements ILiveMatchService {

	private final X01MatchRepository x01MatchRepository;

	public LiveMatchServiceImpl(X01MatchRepository x01MatchRepository) {
		this.x01MatchRepository = x01MatchRepository;
	}

	@Override
	public X01Match getLiveMatch(ObjectId matchId) {
		return x01MatchRepository.findById(matchId).orElse(null);
	}

	@Override
	public X01Match updateLiveMatch(X01Throw x01Throw) {

		X01Match match = getLiveMatch(x01Throw.getMatchId());

		// Check if the throwing play is in the match.
		MatchPlayer player = match.getPlayers().stream()
				.filter(_player -> Objects.equals(_player.getPlayerId().toLowerCase(), x01Throw.getPlayerId().toLowerCase()))
				.findFirst()
				.orElse(null);

		if (player == null) return match;

		// If there is no timeline, create one.
		if (match.getTimeline() == null) {
			match.setTimeline(new ArrayList<>());
		}

		// Get the set, a new set is created if it doesn't exist yet.
		X01Set set = getOrAddSet(match, x01Throw.getSet());

		// Get the leg, a new leg is created if it doesn't exist yet.
		X01Leg leg = getOrAddLeg(set, x01Throw.getLeg());

		// Get the leg round, a new leg round is created if it doesn't exist yet.
		X01LegRound legRound = getOrAddLegRound(leg, x01Throw.getRound());

		// Add the throw to the leg round.
		playerAddThrow(leg, legRound, x01Throw, match.getX01());

		// Update Statistics and Result of the match.
		match.updateAll(set.getSet());

		// Save and return the updated match.
		return x01MatchRepository.save(match);
	}

	@Override
	public X01Match deleteThrowLiveMatch(X01DeleteThrow x01DeleteThrow) {
		X01Match match = getLiveMatch(x01DeleteThrow.getMatchId());

		// Check if the throwing play is in the match.
		MatchPlayer player = match.getPlayers().stream()
				.filter(_player -> Objects.equals(_player.getPlayerId().toLowerCase(), x01DeleteThrow.getPlayerId().toLowerCase()))
				.findFirst()
				.orElse(null);

		if (player == null) return match;

		// Delete the queried throw.
		match.getSet(x01DeleteThrow.getSet())
				.flatMap(set -> set.getLeg(x01DeleteThrow.getLeg()))
				.ifPresent(x01Leg -> x01Leg.getRound(x01DeleteThrow.getRound())
						.ifPresent(x01LegRound -> x01LegRound.getPlayerScores().stream()
								.filter(x01LegRoundScore -> Objects.equals(x01LegRoundScore.getPlayerId(), x01DeleteThrow.getPlayerId()))
								.findFirst()
								.ifPresent(x01LegRoundScore -> {
									if (x01LegRound.getPlayerScores().size() > 1) {
										x01LegRound.getPlayerScores().remove(x01LegRoundScore);
									} else {
										x01Leg.getRounds().remove(x01LegRound);
									}
								})
						)
				);

		// Update Statistics and Result of the match.
		match.updateAll();

		return x01MatchRepository.save(match);
	}

	@Override
	public X01Match deleteSetLiveMatch(X01DeleteSet x01DeleteSet) {
		X01Match match = getLiveMatch(x01DeleteSet.getMatchId());

		// TODO: Verify PlayerId so that not everyone can simply delete a set.

		// Delete the queried throw.
		match.getSet(x01DeleteSet.getSet()).ifPresent(set -> match.getTimeline().remove(set));

		// Update Statistics and Result of the match.
		match.updateAll();

		return x01MatchRepository.save(match);
	}

	@Override
	public X01Match deleteLegLiveMatch(X01DeleteLeg x01DeleteLeg) {
		X01Match match = getLiveMatch(x01DeleteLeg.getMatchId());

		// TODO: Verify PlayerId so that not everyone can simply delete a leg.

		// Delete the queried throw.
		match.getSet(x01DeleteLeg.getLeg()).ifPresent(x01Set -> x01Set.getLeg(x01DeleteLeg.getLeg())
				.ifPresent(x01Leg -> x01Set.getLegs().remove(x01Leg)));

		// Update Statistics and Result of the match.
		match.updateAll();

		return x01MatchRepository.save(match);
	}

	// Create set, if already exists replace with empty set.
	private X01Set getOrAddSet(X01Match match, int setNumber) {
		Optional<X01Set> set = match.getSet(setNumber);

		if (set.isPresent()) {
			return set.get();
		} else {
			X01Set newSet = X01TimelineUtils.createSet(match.getPlayers(), setNumber);
			match.getTimeline().add(newSet);
			return newSet;

		}
	}

	// Create leg in a set, if already exists replace with empty leg.
	private X01Leg getOrAddLeg(X01Set set, int legNumber) {
		Optional<X01Leg> leg = set.getLeg(legNumber);

		if (leg.isPresent()) {
			return leg.get();
		} else {
			X01Leg newLeg = X01TimelineUtils.createLeg(legNumber);
			set.getLegs().add(newLeg);
			return newLeg;
		}
	}

	// Create leg round in a leg, if already exists replace with empty leg round.
	private X01LegRound getOrAddLegRound(X01Leg leg, int roundNumber) {
		Optional<X01LegRound> round = leg.getRound(roundNumber);

		if (round.isPresent()) {
			return round.get();
		} else {
			X01LegRound newRound = X01TimelineUtils.createLegRound(roundNumber);
			leg.getRounds().add(newRound);
			return newRound;
		}
	}

	private void playerAddThrow(X01Leg x01Leg, X01LegRound x01LegRound, X01Throw x01Throw, Integer x01) {

		// If the player isn't in the leg round then add him.
		X01LegRoundScore x01LegRoundScore = x01LegRound.getPlayerScore(x01Throw.getPlayerId()).orElse(null);
		int oldScore = 0;
		int oldDoublesMissed = 0;
		int oldDartsUsed = 0;

		if (x01LegRoundScore == null) {
			x01LegRoundScore = new X01LegRoundScore(x01Throw.getPlayerId(), 0, 0, 0);
			x01LegRound.getPlayerScores().add(x01LegRoundScore);
		} else {
			oldScore = x01LegRoundScore.getScore();
			oldDoublesMissed = x01LegRoundScore.getDoublesMissed();
			oldDartsUsed = x01LegRoundScore.getDartsUsed();
		}

		x01LegRoundScore.setScore(x01Throw.getScore());
		x01LegRoundScore.setDoublesMissed(x01Throw.getDoublesMissed());
		x01LegRoundScore.setDartsUsed(x01Throw.getDartsUsed());

		int playerScore = x01Leg.getRounds().stream()
				.flatMap(x01LegRound1 -> x01LegRound1.getPlayerScores()
						.stream()
						.filter(x01LegRoundScore1 -> Objects.equals(x01LegRoundScore1.getPlayerId(), x01Throw.getPlayerId()))
				)
				.mapToInt(X01LegRoundScore::getScore).sum();

		// If the new scoring result is negative dont allow it. // TODO: Send back and error
		int remaining = x01 - playerScore;
		if (remaining < 0) {
			x01LegRoundScore.setScore(oldScore);
			x01LegRoundScore.setDoublesMissed(oldDoublesMissed);
			x01LegRoundScore.setDartsUsed(oldDartsUsed);
		}

		// TODO: Shouldn't be able to edit so that both players get a remaining of 0.
		// TODO: Check if finish is legit, a.k.a can't do 141 in 2 darts.

	}

}
