package com.dartsmatcher.dartsmatcherapi.features;

import com.dartsmatcher.dartsmatcherapi.features.x01Dartbot.X01DartBotService;
import com.dartsmatcher.dartsmatcherapi.features.x01Dartbot.X01DartBotSettings;
import com.dartsmatcher.dartsmatcherapi.features.x01Dartbot.X01DartBotThrow;
import com.dartsmatcher.dartsmatcherapi.features.x01checkout.X01CheckoutServiceImpl;
import com.dartsmatcher.dartsmatcherapi.features.x01livematch.dto.X01Throw;
import com.dartsmatcher.dartsmatcherapi.features.x01match.IX01MatchService;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01Leg;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01LegRound;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg.X01LegRoundScore;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.set.X01Set;
import org.bson.types.ObjectId;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {X01DartBotService.class, X01CheckoutServiceImpl.class})
public class X01DartBotServiceTests {

	@MockBean
	private IX01MatchService matchService;

	@InjectMocks
	@Autowired
	private X01DartBotService dartBotService;

	@Test
	void testAllExpectedAverages_dartsUsedWithinMarginOfError() throws IOException {

		final X01Match match = createTestMatch();
		Mockito.when(matchService.getMatch(Mockito.any())).thenReturn(match);

		final String dartBotId = "dartBot";
		final int x01 = 501;
		final double maxAvgDeviation = X01DartBotService.MAX_ONE_DART_AVG_DEVIATION * 3;

		for (int avg = 180; avg > 0; avg--) {

			// Initialize the match with the base settings.
			X01Leg x01Leg = new X01Leg(1, null, dartBotId, new ArrayList<>());
			X01Set x01Set = new X01Set(1, null, new ArrayList<>(Collections.singletonList(x01Leg)));

			match.setTimeline(new ArrayList<>(Collections.singletonList(x01Set)));
			match.setDartBotSettings(new X01DartBotSettings(avg));

			// Initialize the minimum and maximum number of darts a dart bot must complete a leg in.
			double minAvg = avg - maxAvgDeviation;
			double maxAvg = avg + maxAvgDeviation;

			int marginOfError = 1;

			int minDarts = (int) Math.floor((x01 / (maxAvg < 1 ? 1 : maxAvg)) * 3) - marginOfError;
			int maxDarts = (int) Math.ceil((x01 / (minAvg < 1 ? 1 : minAvg)) * 3) + marginOfError;

			// Let the dart bot complete x number of legs and for each leg check that the darts used is within the range of minimum and maximum darts.
			for (int j = 0; j < 50; j++) {
				int round = 1;
				int remaining = x01;
				while (remaining != 0) {
					X01Throw x01Throw = dartBotService.dartBotThrow(new X01DartBotThrow(dartBotId, match.getId(), 1, x01Leg.getLeg(), round));
					X01LegRoundScore newRound = new X01LegRoundScore(dartBotId, 0, x01Throw.getDartsUsed(), x01Throw.getScore());

					x01Leg.getRounds().add(new X01LegRound(round++, new ArrayList<>(Collections.singletonList(newRound))));

					remaining -= x01Throw.getScore();
				}

				int dartsUsed = x01Leg.getDartsUsed(dartBotId);

				MatcherAssert.assertThat(dartsUsed, Matchers.allOf(
						Matchers.greaterThanOrEqualTo(minDarts),
						Matchers.lessThanOrEqualTo(maxDarts)
						)
				);

				x01Leg.setRounds(new ArrayList<>());
			}
		}
	}

	private X01Match createTestMatch() {
		X01Match match = new X01Match();
		match.setId(new ObjectId());
		match.setX01(501);

		return match;
	}
}
