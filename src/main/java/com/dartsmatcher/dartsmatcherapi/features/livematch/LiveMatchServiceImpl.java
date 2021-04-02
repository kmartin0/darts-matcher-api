package com.dartsmatcher.dartsmatcherapi.features.livematch;

import com.dartsmatcher.dartsmatcherapi.features.x01match.X01MatchRepository;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01Leg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01PlayerLeg;
import com.dartsmatcher.dartsmatcherapi.features.match.MatchPlayer;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.set.X01Set;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
		else x01Throw.setPlayerId(player.getPlayerId());

		// If there is no timeline, create one.
		if (match.getTimeline() == null) {
			match.setTimeline(new ArrayList<>());
		}

		X01Set set = match.getSet(x01Throw.getSet()).orElse(null);

		// If the set does not exist, create it.
		if (set == null) {
			set = createSet(match, x01Throw.getSet());
		}

		X01Leg leg = set.getLeg(x01Throw.getLeg()).orElse(null);

		// If the leg does not exist, create it.
		if (leg == null) {
			leg = createLeg(set, x01Throw.getLeg(), match.getPlayers());
		}

		playerAddThrow(leg, x01Throw, match.getX01());

		// Update Statistics and Result of the match.
		match.updateResultAndStatisticsAndCurrentThrower(set.getSet());

		// Save and return the updated match.
		return x01MatchRepository.save(match);
	}

	// Create set, if already exists replace with empty set.
	private X01Set createSet(X01Match match, int setNumber) {
		Optional<X01Set> set = match.getSet(setNumber);

		X01Set newSet = new X01Set(setNumber, null, new ArrayList<>());

		if (set.isPresent()) {
			match.getTimeline().set(match.getTimeline().indexOf(set.get()), newSet);
		} else {
			match.getTimeline().add(newSet);
		}

		return newSet;
	}

	// Create leg in a set, if already exists replace with empty leg.
	private X01Leg createLeg(X01Set set, int legNumber, ArrayList<MatchPlayer> players) {
		Optional<X01Leg> leg = set.getLeg(legNumber);

		X01Leg newLeg = new X01Leg(legNumber, null, 0, new ArrayList<>());

		players.forEach(matchPlayer ->
				newLeg.getPlayers().add(
						new X01PlayerLeg(matchPlayer.getPlayerId(), 0, new ArrayList<>())
				)
		);

		if (leg.isPresent()) {
			set.getLegs().set(set.getLegs().indexOf(leg.get()), newLeg);
		} else {
			set.getLegs().add(newLeg);
		}

		return newLeg;
	}

	private void playerAddThrow(X01Leg leg, X01Throw x01Throw, Integer x01) {

		// If the player doesn't have a player leg then add one.
		X01PlayerLeg playerLeg = leg.getPlayers().stream()
				.filter(_leg -> Objects.equals(_leg.getPlayerId(), x01Throw.getPlayerId()))
				.findFirst()
				.orElse(null);

		if (playerLeg == null) {
			playerLeg = new X01PlayerLeg(x01Throw.getPlayerId(), 0, new ArrayList<>());
			leg.getPlayers().add(playerLeg);
		}

		ArrayList<Integer> tmpScoring = new ArrayList<>(playerLeg.getScoring());

		// If the round is already in the scoring update it. Otherwise add it at the end.
		if (playerLeg.getScoring().size() > x01Throw.getRound()) {
			tmpScoring.set(x01Throw.getRound(), x01Throw.getScore());
		} else {
			tmpScoring.add(x01Throw.getScore());
		}

		// If the new scoring result is negative dont allow it. // TODO: Send back and error
		int remaining = x01 - tmpScoring.stream().mapToInt(i -> i).sum();
		if (remaining >= 0) playerLeg.setScoring(tmpScoring);

		// TODO: Shouldn't be able to edit so that both players get a remaining of 0.

		// Update doubles missed.
		playerLeg.setDoublesMissed(playerLeg.getDoublesMissed() + x01Throw.getDoublesMissed());

		// Set darts used in final throw.
		leg.setDartsUsedFinalThrow(x01Throw.getDartsUsedFinalThrow());

	}
}
