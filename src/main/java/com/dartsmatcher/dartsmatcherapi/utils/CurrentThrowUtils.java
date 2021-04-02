package com.dartsmatcher.dartsmatcherapi.utils;

import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01Leg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01PlayerLeg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.set.X01Set;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class CurrentThrowUtils {

	public static void updateCurrentThrower(X01Match match) {
		if (match == null) return;

		if (match.getTimeline() == null || match.getTimeline().isEmpty()) {
			match.setCurrentThrower(match.getOrderOfPlay().get(0));
			return;
		}

		boolean matchHasResult = match.getResult().stream().anyMatch(x01PlayerResult -> x01PlayerResult.getResult() != null);
		if (matchHasResult) {
			match.setCurrentThrower(null);
			return;
		}

		Optional<X01Set> currentSet = match.getTimeline().stream()
				.filter(x01Set -> {
					if (x01Set.getResult() == null) return true;
					else
						return x01Set.getResult().stream().anyMatch(x01PlayerResult -> x01PlayerResult.getResult() == null);
				})
				.findFirst();

		ArrayList<String> orderOfPlayInSet = createSetOrderOfPlay(match, currentSet.orElse(null));

		if (!currentSet.isPresent()) {
			match.setCurrentThrower(orderOfPlayInSet.get(0));
			return;
		}

		Optional<X01Leg> currentLeg = currentSet.get().getLegs().stream()
				.filter(x01Leg -> x01Leg.getWinner() == null)
				.findFirst();

		ArrayList<String> orderOfPlayInLeg = createLegOrderOfPlay(match.getPlayers().size(), orderOfPlayInSet, currentLeg.orElse(null), currentSet.get());

		match.setCurrentThrower(getCurrentThrowerInLeg(currentLeg.orElse(null), orderOfPlayInLeg));
	}

	public static String getCurrentThrowerInLeg(X01Leg leg, ArrayList<String> orderOfPlayInLeg) {
		if (leg == null) {
			return orderOfPlayInLeg.get(0);
		}

		int round = leg.getPlayers().stream().filter(x01PlayerLeg -> x01PlayerLeg.getScoring() != null).mapToInt(x -> x.getScoring().size()).max().orElse(1);

		boolean roundComplete = true;
		for (X01PlayerLeg x01PlayerLeg : leg.getPlayers()) {
			if (x01PlayerLeg.getScoring() == null || x01PlayerLeg.getScoring().size() < round) {
				roundComplete = false;
				break;
			}
		}
		if (roundComplete) round++;

		for (String playerId : orderOfPlayInLeg) {
			X01PlayerLeg x01PlayerLeg = leg.getPlayers().stream()
					.filter(_x01PlayerLeg -> Objects.equals(_x01PlayerLeg.getPlayerId(), playerId))
					.findFirst()
					.orElse(null);

			// When the player has not thrown this round, it's this players' turn.
			if (x01PlayerLeg == null || x01PlayerLeg.getScoring() == null || x01PlayerLeg.getScoring().size() != round) {
				return playerId;
			}
		}

		return orderOfPlayInLeg.get(0);
	}

	public static ArrayList<String> createLegOrderOfPlay(int numOfPlayers, ArrayList<String> setOrderOfPlay, X01Leg leg, X01Set set) {
		if (leg == null) {
			int legsPlayed = (set != null && set.getLegs() != null)
					? set.getLegs().stream().mapToInt(X01Leg::getLeg).max().orElse(0)
					: 0;

			int throwsFirstInLeg = legsPlayed % numOfPlayers;

			return createOrderOfPlay(setOrderOfPlay, throwsFirstInLeg);
		}

		int throwsFirstInLeg = (leg.getLeg() - 1) % numOfPlayers;

		return createOrderOfPlay(setOrderOfPlay, throwsFirstInLeg);
	}

	public static ArrayList<String> createSetOrderOfPlay(X01Match match, X01Set set) {

		if (set == null) {
			int setsPlayed = match.getTimeline().stream().mapToInt(X01Set::getSet).max().orElse(0);
			int throwsFirstInSet = setsPlayed % match.getPlayers().size();

			return createOrderOfPlay(match.getOrderOfPlay(), throwsFirstInSet);
		}

		int throwsFirstInSet = (set.getSet() - 1) % match.getPlayers().size();

		return createOrderOfPlay(match.getOrderOfPlay(), throwsFirstInSet);

	}

	public static ArrayList<String> createOrderOfPlay(ArrayList<String> initialOrder, int firstToThrow) {
		ArrayList<String> newOrderOfPlay = new ArrayList<>();
		if (initialOrder.size() == 1) return initialOrder;

		newOrderOfPlay.addAll(initialOrder.subList(firstToThrow, initialOrder.size()));
		newOrderOfPlay.addAll(initialOrder.subList(0, firstToThrow));

		return newOrderOfPlay;
	}


}
