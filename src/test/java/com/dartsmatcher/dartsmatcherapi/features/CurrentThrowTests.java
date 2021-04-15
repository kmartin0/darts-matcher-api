package com.dartsmatcher.dartsmatcherapi.features;

import com.dartsmatcher.dartsmatcherapi.config.LocaleConfig;
import com.dartsmatcher.dartsmatcherapi.features.match.*;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.bestof.X01BestOf;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01Leg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01LegRound;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.playerresult.X01PlayerResult;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.set.X01Set;
import com.dartsmatcher.dartsmatcherapi.utils.CurrentThrowUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@ExtendWith({SpringExtension.class})
@ContextConfiguration
@Import(LocaleConfig.class)
public class CurrentThrowTests {

	private ArrayList<MatchPlayer> players;
	private X01Match match;

	@BeforeEach
	void init() {
		players = new ArrayList<>();
		players.add(new MatchPlayer("John Doe", PlayerType.ANONYMOUS));
		players.add(new MatchPlayer("Jane Doe", PlayerType.ANONYMOUS));

		match = new X01Match(
				new ObjectId(),
				LocalDateTime.now().minus(60, ChronoUnit.MINUTES),
				LocalDateTime.now(),
				players.get(0).getPlayerId(),
				players,
				MatchType.X01,
				501,
				MatchStatus.IN_PLAY,
				new X01BestOf(3, 1),
				null,
				null,
				null
		);
	}

	/*
	=============== SET 1 ===============
	 */
	@Test
	void testSet1Leg1Round1FirstThrow_isJohnDoe() {
		match.updateCurrentThrower();

		Assertions.assertEquals("John Doe", match.getCurrentThrower());
	}

	@Test
	void testSet1Leg1Round1SecondThrow_isJaneDoe() {
		X01Leg x01Leg = new X01Leg(1, null, new ArrayList<>());

		X01LegRound round1 = new X01LegRound(1, new ArrayList<>());
		round1.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 50));

		x01Leg.getRounds().add(round1);

		X01Set x01Set = new X01Set(1, null, new ArrayList<>(Collections.singletonList(x01Leg)));
		match.setTimeline(new ArrayList<>(Collections.singletonList(x01Set)));

		match.updateCurrentThrower();

		Assertions.assertEquals("Jane Doe", match.getCurrentThrower());
	}

	@Test
	void testSet1Leg1Round2FirstThrow_isJohnDoe() {
		X01Leg x01Leg = new X01Leg(1, null, new ArrayList<>());

		X01LegRound round1 = new X01LegRound(1, new ArrayList<>());
		round1.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 50));
		round1.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 75));

		x01Leg.getRounds().add(round1);

		X01Set x01Set = new X01Set(1, null, new ArrayList<>(Collections.singletonList(x01Leg)));
		match.setTimeline(new ArrayList<>(Collections.singletonList(x01Set)));

		match.updateCurrentThrower();

		Assertions.assertEquals("John Doe", match.getCurrentThrower());
	}

	@Test
	void testSet1Leg1Round2SecondThrow_isJaneDoe() {
		X01Leg x01Leg = new X01Leg(1, null, new ArrayList<>());

		X01LegRound round1 = new X01LegRound(1, new ArrayList<>());
		round1.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 50));
		round1.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 75));

		X01LegRound round2 = new X01LegRound(2, new ArrayList<>());
		round2.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 150));

		x01Leg.getRounds().add(round1);
		x01Leg.getRounds().add(round2);

		X01Set x01Set = new X01Set(1, null, new ArrayList<>(Collections.singletonList(x01Leg)));
		match.setTimeline(new ArrayList<>(Collections.singletonList(x01Set)));

		match.updateCurrentThrower();

		Assertions.assertEquals("Jane Doe", match.getCurrentThrower());
	}

	/*
	=============== SET 2 ===============
	 */
	@Test
	void testSet2Leg1Round1FirstThrow_isJaneDoe() {
		// Set 1
		X01Leg set1Leg1 = new X01Leg(1, "John Doe", new ArrayList<>());

		X01LegRound round1 = new X01LegRound(1, new ArrayList<>());
		round1.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 180));
		round1.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 180));

		X01LegRound round2 = new X01LegRound(2, new ArrayList<>());
		round2.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 150));
		round2.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 180));

		X01LegRound round3 = new X01LegRound(2, new ArrayList<>());
		round3.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 141));

		set1Leg1.getRounds().add(round1);
		set1Leg1.getRounds().add(round2);
		set1Leg1.getRounds().add(round3);

		X01PlayerResult johnResult = new X01PlayerResult("John Doe", 1, ResultType.WIN);
		X01PlayerResult janeResult = new X01PlayerResult("Jane Doe", 1, ResultType.LOSE);

		X01Set set1 = new X01Set(1, new ArrayList<>(Arrays.asList(johnResult, janeResult)), new ArrayList<>(Collections.singletonList(set1Leg1)));

		// Set 2
		X01Leg set2Leg1 = new X01Leg(1, null, new ArrayList<>());

		X01Set set2 = new X01Set(2, null, new ArrayList<>(Collections.singletonList(set2Leg1)));

		// Add sets to timeline
		match.setTimeline(new ArrayList<>(Arrays.asList(set1, set2)));

		match.updateCurrentThrower();

		Assertions.assertEquals("Jane Doe", match.getCurrentThrower());
	}

	/*
	=============== Create Order Of Play ===============
	 */
	@Test
	void testCreateOrderOfPlay1Player_is1Player() {
		players.remove(1);

		ArrayList<String> orderOfPlay = CurrentThrowUtils.createOrderOfPlay(
				match.getPlayers().stream().map(MatchPlayer::getPlayerId).collect(Collectors.toCollection(ArrayList::new)),
				0
		);

		Assertions.assertEquals(Collections.singletonList("John Doe"), orderOfPlay);
	}

	@Test
	void testCreateOrderOfPlayPlayer1ThrowFirst_isOrder1_2() {
		ArrayList<String> orderOfPlay = CurrentThrowUtils.createOrderOfPlay(
				match.getPlayers().stream().map(MatchPlayer::getPlayerId).collect(Collectors.toCollection(ArrayList::new)),
				0
		);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("John Doe", "Jane Doe")), orderOfPlay);
	}

	@Test
	void testCreateOrderOfPlayPlayer2ThrowFirst_isOrder2_1() {
		ArrayList<String> orderOfPlay = CurrentThrowUtils.createOrderOfPlay(
				match.getPlayers().stream().map(MatchPlayer::getPlayerId).collect(Collectors.toCollection(ArrayList::new)),
				1
		);

		Assertions.assertEquals(Arrays.asList("Jane Doe", "John Doe"), orderOfPlay);
	}

	/*
=============== Create Set Order Of Play ===============
 */
	@Test
	void testCreateSetOrderOfPlayPlayer1ThrowFirstSet3_isOrder_1_2() {
		X01Set set3 = new X01Set(3, new ArrayList<>(), new ArrayList<>());

		ArrayList<String> orderOfPlay = CurrentThrowUtils.createSetOrderOfPlay(match, set3);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("John Doe", "Jane Doe")), orderOfPlay);
	}

	@Test
	void testCreateSetOrderOfPlayPlayer1ThrowFirstSet4_isOrder_2_1() {
		X01Set set4 = new X01Set(4, new ArrayList<>(), new ArrayList<>());

		ArrayList<String> orderOfPlay = CurrentThrowUtils.createSetOrderOfPlay(match, set4);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("Jane Doe", "John Doe")), orderOfPlay);
	}

	/*
	=============== Create Leg Order Of Play ===============
	 */
	@Test
	void testCreateLegOrderOfPlayPlayer1ThrowFirstSet1Leg1_isOrder_1_2() {
		ArrayList<String> setOrderOfPlay = new ArrayList<>(Arrays.asList("John Doe", "Jane Doe"));

		ArrayList<String> orderOfPlay = CurrentThrowUtils.createLegOrderOfPlay(match.getPlayers().size(), setOrderOfPlay, null, null);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("John Doe", "Jane Doe")), orderOfPlay);
	}

	@Test
	void testCreateLegOrderOfPlayPlayer1ThrowFirstSet3Leg3_isOrder_1_2() {
		X01Leg set3Leg3 = new X01Leg(3, "John Doe", new ArrayList<>());

		X01Set set3 = new X01Set(3, new ArrayList<>(), new ArrayList<>(Collections.singletonList(set3Leg3)));

		ArrayList<String> setOrderOfPlay = new ArrayList<>(Arrays.asList("John Doe", "Jane Doe"));

		ArrayList<String> orderOfPlay = CurrentThrowUtils.createLegOrderOfPlay(match.getPlayers().size(), setOrderOfPlay, set3Leg3, set3);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("John Doe", "Jane Doe")), orderOfPlay);
	}

	@Test
	void testCreateLegOrderOfPlayPlayer1ThrowFirstSet3Leg4_isOrder_2_1() {
		X01Leg set3Leg4 = new X01Leg(4, "John Doe", new ArrayList<>());

		X01Set set3 = new X01Set(3, new ArrayList<>(), new ArrayList<>(Collections.singletonList(set3Leg4)));

		ArrayList<String> setOrderOfPlay = new ArrayList<>(Arrays.asList("John Doe", "Jane Doe"));

		ArrayList<String> orderOfPlay = CurrentThrowUtils.createLegOrderOfPlay(match.getPlayers().size(), setOrderOfPlay, set3Leg4, set3);

		Assertions.assertEquals(new ArrayList<>(Arrays.asList("Jane Doe", "John Doe")), orderOfPlay);
	}

	/*
=============== Get Current Thrower In Leg ===============
 */
	@Test
	void testGetCurrentThrowerInLeg4Round1_isJohnDoe() {
		X01Leg leg1 = new X01Leg(1, "John Doe", new ArrayList<>());

		ArrayList<String> legOrderOfPlay = new ArrayList<>(Arrays.asList("John Doe", "Jane Doe"));

		String currentThrower = CurrentThrowUtils.getCurrentThrowerInLeg(match, leg1, legOrderOfPlay);

		Assertions.assertEquals("John Doe", currentThrower);
	}

	@Test
	void testGetCurrentThrowerInLeg1Round2_isJaneDoe() {
		X01Leg leg1 = new X01Leg(1, "John Doe", new ArrayList<>());

		X01LegRound round1 = new X01LegRound(1, new ArrayList<>());
		round1.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 180));
		round1.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 180));
		leg1.getRounds().add(round1);

		X01LegRound round2 = new X01LegRound(1, new ArrayList<>());
		round2.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 180));
		leg1.getRounds().add(round2);

		ArrayList<String> legOrderOfPlay = new ArrayList<>(Arrays.asList("John Doe", "Jane Doe"));

		String currentThrower = CurrentThrowUtils.getCurrentThrowerInLeg(match, leg1, legOrderOfPlay);

		Assertions.assertEquals("Jane Doe", currentThrower);
	}

	@Test
	void testGetCurrentThrowerInLeg3Round1_isJohnDoe() {
		X01Leg leg1 = new X01Leg(1, "John Doe", new ArrayList<>());

		ArrayList<String> legOrderOfPlay = new ArrayList<>(Arrays.asList("John Doe", "Jane Doe"));

		String currentThrower = CurrentThrowUtils.getCurrentThrowerInLeg(match, leg1, legOrderOfPlay);

		Assertions.assertEquals("John Doe", currentThrower);
	}

	@Test
	void testGetCurrentThrowerInLeg3Round5_isJohnDoe() {
		X01Leg leg1 = new X01Leg(3, "John Doe", new ArrayList<>());

		X01LegRound round1 = new X01LegRound(1, new ArrayList<>());
		round1.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 80));
		round1.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 88));
		leg1.getRounds().add(round1);

		X01LegRound round2 = new X01LegRound(1, new ArrayList<>());
		round2.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 60));
		round2.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 43));
		leg1.getRounds().add(round2);

		X01LegRound round3 = new X01LegRound(1, new ArrayList<>());
		round3.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 40));
		round3.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 64));
		leg1.getRounds().add(round3);

		X01LegRound round4 = new X01LegRound(1, new ArrayList<>());
		round4.getPlayerScores().add(new X01LegRoundScore("John Doe", 0, 3, 50));
		round4.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 53));
		leg1.getRounds().add(round4);

		X01LegRound round5 = new X01LegRound(1, new ArrayList<>());
		round5.getPlayerScores().add(new X01LegRoundScore("Jane Doe", 0, 3, 44));
		leg1.getRounds().add(round5);

		ArrayList<String> legOrderOfPlay = new ArrayList<>(Arrays.asList("John Doe", "Jane Doe"));

		String currentThrower = CurrentThrowUtils.getCurrentThrowerInLeg(match, leg1, legOrderOfPlay);

		Assertions.assertEquals("John Doe", currentThrower);
	}
}
