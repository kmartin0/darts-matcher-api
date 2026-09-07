package nl.kmartin.dartsmatcherapi.features.x01.x01match;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.testutils.X01FeatureTestFactory;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.repository.IX01MatchRepository;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class X01MatchServiceTests {

    private X01MatchServiceTestData testData;

    @Mock
    private IX01MatchRepository matchRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final Map<ObjectId, X01Match> matches = new HashMap<>();

    private IX01MatchService matchService;

    @BeforeEach
    void setUp() {
        configureMatchRepository();

        X01FeatureTestFactory featureTestFactory = new X01FeatureTestFactory(
                matchRepository,
                eventPublisher
        );

        matchService = featureTestFactory.createMatchService();
        testData = new X01MatchServiceTestData(featureTestFactory.createObjectMapper());
    }

    /**
     * ===== Scenario 1 =====
     * Match configuration:
     * - 501
     * - Best of 3 legs
     * - 1 human player
     * - Clear-by-two disabled
     * - Track doubles enabled
     *
     * Stresses:
     * - Missed doubles
     * - Bust / normalized zero turn
     * - Edit earlier turn
     * - Delete last turn
     */
    @Test
    void scenarioOne() {
        X01CreateMatchRequest createMatchRequest = testData.getScenarioOneCreateMatchRequest();
        X01Match match = matchService.createMatch(createMatchRequest);
        ObjectId playerOneId = match.getPlayers().get(0).getPlayerId();

        runScenario(
                match.getId(),
                List.of(
                        // Leg 1: missed doubles before checkout.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(101, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(0, 3, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(40, 0, 1)),

                        // Leg 2: bust is normalized to a zero-score turn.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(145, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, 0, 3)),

                        // Leg 3: edit an earlier turn and remove the latest turn.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.EditTurn(new X01EditTurnRequest(120, 0, null, playerOneId, 1, 3, 2)),
                        new X01ScenarioStep.DeleteLastTurn(),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(101, 0, 3))
                )
        );

        Assertions.assertNotNull(match.getStartDate());
        Assertions.assertNotNull(match.getEndDate());

        X01Match expectedMatch = testData.getScenarioOneExpectedResult(
                match.getId(),
                playerOneId,
                match.getStartDate(),
                match.getEndDate()
        );

        assertThat(match)
                .usingRecursiveComparison()
                .isEqualTo(expectedMatch);
    }

    /**
     * ===== Scenario 2 =====
     * Match configuration:
     * - 501
     * - Best of 3 legs
     * - 2 human players
     * - Clear-by-two legs disabled
     * - Track doubles enabled
     *
     * Stresses:
     * - Normal two-player rotation
     * - Missed doubles
     * - Bust / normalized zero turn
     * - Edit earlier turn
     * - Delete last turn
     * - Deciding leg
     */
    @Test
    void scenarioTwo() {
        X01CreateMatchRequest createMatchRequest = testData.getScenarioTwoCreateMatchRequest();
        X01Match match = matchService.createMatch(createMatchRequest);
        ObjectId playerOneId = match.getPlayers().get(0).getPlayerId();
        ObjectId playerTwoId = match.getPlayers().get(1).getPlayerId();

        runScenario(
                match.getId(),
                List.of(
                        // Leg 1: Player 2 wins after Player 1 misses three doubles.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(101, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(101, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(0, 3, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(40, 0, 1)),

                        // Leg 2: Player 2 busts from 141 and Player 1 checks out.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(145, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, 0, 3)),

                        // Leg 3: edit Player 1's second turn and replace Player 2's deleted latest turn.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.EditTurn(new X01EditTurnRequest(120, 0, null, playerOneId, 1, 3, 2)),
                        new X01ScenarioStep.DeleteLastTurn(),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(120, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(140, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(140, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(41, 0, 2))
                )
        );

        Assertions.assertNotNull(match.getStartDate());
        Assertions.assertNotNull(match.getEndDate());

        X01Match expectedMatch = testData.getScenarioTwoExpectedResult(
                match.getId(),
                playerOneId,
                playerTwoId,
                match.getStartDate(),
                match.getEndDate()
        );

        assertThat(match)
                .usingRecursiveComparison()
                .isEqualTo(expectedMatch);
    }

    /**
     * ===== Scenario 3 =====
     * Match configuration:
     * - 501
     * - Best of 3 sets
     * - Best of 3 legs
     * - 3 human players
     * - All clear-by-two rules enabled with limit 2
     * - Track doubles disabled
     *
     * Stresses:
     * - Three-player rotation
     * - Player 3 to Player 1 wraparound
     * - Rotating set and leg starters
     * - Clear-by-two leg extension
     * - Multiple leg winners
     */
    @Test
    void scenarioThree() {
        X01CreateMatchRequest createMatchRequest = testData.getScenarioThreeCreateMatchRequest();
        X01Match match = matchService.createMatch(createMatchRequest);

        ObjectId playerOneId = match.getPlayers().get(0).getPlayerId();
        ObjectId playerTwoId = match.getPlayers().get(1).getPlayerId();
        ObjectId playerThreeId = match.getPlayers().get(2).getPlayerId();

        runScenario(
                match.getId(),
                List.of(
                        // Set 1, leg 1: Player 1 starts and wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Set 1, leg 2: Player 2 starts and wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Set 1, leg 3: Player 3 starts, Player 1 wins.
                        // The set is 2-1 after the configured best of 3 and must continue.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Set 1, leg 4: starter wraps back to Player 1, who wins 3-1.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Set 2, leg 1: set starter rotates to Player 2, Player 1 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Set 2, leg 2: Player 3 starts, Player 1 wins and concludes the match.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3))
                )
        );

        Assertions.assertNotNull(match.getStartDate());
        Assertions.assertNotNull(match.getEndDate());

        // Expected-match fixture next.
        Assertions.assertNotNull(match.getStartDate());
        Assertions.assertNotNull(match.getEndDate());

        X01Match expectedMatch = testData.getScenarioThreeExpectedResult(
                match.getId(),
                playerOneId,
                playerTwoId,
                playerThreeId,
                match.getStartDate(),
                match.getEndDate()
        );

        assertThat(match)
                .usingRecursiveComparison()
                .isEqualTo(expectedMatch);
    }

    /**
     * ===== Scenario 4 =====
     * Match configuration:
     * - 501
     * - Best of 3 sets
     * - Best of 3 legs
     * - 4 human players
     * - Clear-by-two disabled
     * - Track doubles enabled
     *
     * Stresses:
     * - Four-player rotation
     * - Rotating set and leg starters
     * - All four players winning at least one leg
     * - Missed doubles
     * - Bust / normalized zero turn
     * - Edit earlier turn
     * - Delete last turn
     */
    @Test
    void scenarioFour() {
        X01CreateMatchRequest createMatchRequest = testData.getScenarioFourCreateMatchRequest();
        X01Match match = matchService.createMatch(createMatchRequest);

        ObjectId playerOneId = match.getPlayers().get(0).getPlayerId();
        ObjectId playerTwoId = match.getPlayers().get(1).getPlayerId();
        ObjectId playerThreeId = match.getPlayers().get(2).getPlayerId();
        ObjectId playerFourId = match.getPlayers().get(3).getPlayerId();

        runScenario(
                match.getId(),
                List.of(
                        // Set 1, leg 1: Player 1 wins after missing two doubles.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(101, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(0, 2, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(40, 0, 1)),

                        // Set 1, leg 2: Player 2 busts from 141 and Player 3 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(145, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, 0, 3)),

                        // Set 1, leg 3: edit Player 1's second turn and replace the deleted latest turn.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.EditTurn(new X01EditTurnRequest(120, 0, null, playerOneId, 1, 3, 2)),
                        new X01ScenarioStep.DeleteLastTurn(),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(80, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(140, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(41, 0, 2)),

                        // Set 2, leg 1: Player 4 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, 0, 3)),

                        // Set 2, leg 2: Player 2 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, 0, 3)),

                        // Set 2, leg 3: Player 2 wins the set.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, 0, 3)),

                        // Set 3, leg 1: Player 1 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, 0, 3)),

                        // Set 3, leg 2: Player 1 wins again and concludes the match.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),

                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, 0, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, 0, 3))
                )
        );

        Assertions.assertNotNull(match.getStartDate());
        Assertions.assertNotNull(match.getEndDate());

        X01Match expectedMatch = testData.getScenarioFourExpectedResult(
                match.getId(),
                playerOneId,
                playerTwoId,
                playerThreeId,
                playerFourId,
                match.getStartDate(),
                match.getEndDate()
        );

        assertThat(match)
                .usingRecursiveComparison()
                .isEqualTo(expectedMatch);
    }

    /**
     * ===== Scenario 5 =====
     * Match configuration:
     * - 501
     * - Best of 4 legs
     * - 2 human players
     * - Clear-by-two disabled
     * - Track doubles disabled
     *
     * Stresses:
     * - Drawn set
     * - Drawn match
     * - Both players receive DRAW
     * - Equal final standings
     */
    @Test
    void scenarioFive() {
        X01CreateMatchRequest createMatchRequest = testData.getScenarioFiveCreateMatchRequest();
        X01Match match = matchService.createMatch(createMatchRequest);

        ObjectId playerOneId = match.getPlayers().get(0).getPlayerId();
        ObjectId playerTwoId = match.getPlayers().get(1).getPlayerId();

        runScenario(
                match.getId(),
                List.of(
                        // Leg 1: Player 1 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Leg 2: Player 2 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Leg 3: Player 2 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Leg 4: Player 1 wins and the match concludes 2-2.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3))
                )
        );

        Assertions.assertNotNull(match.getStartDate());
        Assertions.assertNotNull(match.getEndDate());

        X01Match expectedMatch = testData.getScenarioFiveExpectedResult(
                match.getId(),
                playerOneId,
                playerTwoId,
                match.getStartDate(),
                match.getEndDate()
        );

        assertThat(match)
                .usingRecursiveComparison()
                .isEqualTo(expectedMatch);
    }

    /**
     * ===== Scenario 6 =====
     * Match configuration:
     * - 501
     * - Best of 3 sets
     * - Best of 3 legs
     * - 2 human players
     * - Clear-by-two disabled
     * - Track doubles disabled
     *
     * Stresses:
     * - Match remains in progress
     * - Progress after a completed leg
     * - Progress during a partially completed round
     * - Current thrower resolution
     * - Partial statistics and standings
     */
    @Test
    void scenarioSix() {
        X01CreateMatchRequest createMatchRequest = testData.getScenarioSixCreateMatchRequest();
        X01Match match = matchService.createMatch(createMatchRequest);

        ObjectId playerOneId = match.getPlayers().get(0).getPlayerId();
        ObjectId playerTwoId = match.getPlayers().get(1).getPlayerId();

        runScenario(
                match.getId(),
                List.of(
                        // Set 1, leg 1: Player 1 wins.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(180, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(141, null, 3)),

                        // Set 1, leg 2: stop midway through round 2.
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, null, null)),
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(100, null, null))
                )
        );

        Assertions.assertNotNull(match.getStartDate());
        Assertions.assertNull(match.getEndDate());

        X01Match expectedMatch = testData.getScenarioSixExpectedResult(
                match.getId(),
                playerOneId,
                playerTwoId,
                match.getStartDate()
        );

        assertThat(match)
                .usingRecursiveComparison()
                .isEqualTo(expectedMatch);
    }

    /**
     * ===== Scenario 7 =====
     * Reprocess corrupted match
     *
     * Stresses:
     * - Turn recorded after a winning checkout
     * - Incorrect remaining scores
     * - Incorrect match, set and leg results
     * - Incorrect statistics
     * - Corrupted match progress
     * - Corrupted standings
     */
    @Test
    void scenarioSeven() {
        X01Match match = testData.getScenarioSevenInitial();
        matchRepository.save(match);

        X01Match reprocessedMatch = matchService.reprocessMatch(match.getId());
        X01Match expectedMatch = testData.getScenarioSevenExpectedResult();

        assertThat(reprocessedMatch)
                .usingRecursiveComparison()
                .isEqualTo(expectedMatch);
    }

    /**
     * ===== Scenario 8 =====
     * Reset match
     *
     * Stresses:
     * - Preserving match identity and configuration
     * - Preserving player identity and configuration
     * - Resetting player results and statistics
     * - Removing all played match history
     * - Resetting match progress and standings
     * - Starting the match with a new start date
     */
    @Test
    void scenarioEight() {
        X01Match match = testData.getScenarioEightInitial();
        matchRepository.save(match);

        X01Match resetMatch = matchService.resetMatch(match.getId());

        assertThat(resetMatch.getStartDate()).isNotEqualTo(match.getStartDate());

        X01Match expectedMatch = testData.getScenarioEightExpectedResult(resetMatch.getStartDate());

        assertThat(resetMatch)
                .usingRecursiveComparison()
                .isEqualTo(expectedMatch);
    }

    /**
     * ===== Scenario 9 =====
     * Delete match
     *
     * Stresses:
     * - Deleting an existing match
     * - Removing the persisted aggregate
     */
    @Test
    void scenarioNine() {
        X01Match match = testData.getScenarioNineInitial();
        matchRepository.save(match);

        matchService.deleteMatch(match.getId());

        assertThat(matchRepository.findById(match.getId())).isEmpty();
    }

    /**
     * ===== Scenario 10 =====
     * Check match exists
     *
     * Stresses:
     * - Existing match succeeds
     * - Missing match throws ResourceNotFoundException
     */
    @Test
    void scenarioTen() {
        X01Match match = testData.getScenarioTenInitial();

        // Existing match should pass without error.
        matchRepository.save(match);
        Assertions.assertDoesNotThrow(() -> matchService.checkMatchExists(match.getId()));

        // Missing match should report a resource-not-found error.
        ObjectId missingMatchId = new ObjectId();
        assertThatThrownBy(() -> matchService.checkMatchExists(missingMatchId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * ===== Scenario 11 =====
     * Get matches
     *
     * Stresses:
     * - Retrieving multiple matches
     * - Preserving requested ID order
     * - Omitting missing matches
     */
    @SuppressWarnings("UseBulkOperation")
    @Test
    void scenarioEleven() {
        List<X01Match> initialMatches = testData.getScenarioElevenInitial();
        initialMatches.forEach(matchRepository::save);

        X01Match matchOne = initialMatches.get(0);
        X01Match matchTwo = initialMatches.get(1);
        X01Match matchThree = initialMatches.get(2);
        ObjectId missingMatchId = new ObjectId();

        // Request matches in a different order and include a missing match.
        List<X01Match> matches = matchService.getMatches(
                List.of(matchThree.getId(), missingMatchId, matchOne.getId(), matchTwo.getId())
        );

        assertThat(matches)
                .extracting(X01Match::getId)
                .containsExactly(matchThree.getId(), matchOne.getId(), matchTwo.getId());
    }

    /**
     * ===== Scenario 12 =====
     * Match configuration:
     * - 501
     * - Best of 3 legs
     * - 1 human player
     * - 1 Dart Bot player
     * - Clear-by-two disabled
     * - Track doubles disabled
     *
     * Stresses:
     * - Automatic Dart Bot turn processing
     * - Returning control to the human player
     */
    @Test
    void scenarioTwelve() {
        X01CreateMatchRequest createMatchRequest = testData.getScenarioTwelveCreateMatchRequest();
        X01Match match = matchService.createMatch(createMatchRequest);

        ObjectId playerOneId = match.getPlayers().get(0).getPlayerId();
        ObjectId dartBotId = match.getPlayers().get(1).getPlayerId();

        // Match starts with the human player.
        assertThat(match.getMatchProgress().getCurrentThrower()).isEqualTo(playerOneId);

        runScenario(
                match.getId(),
                List.of(
                        new X01ScenarioStep.AddTurn(new X01CreateTurnRequest(60, null, null))
                )
        );

        X01LegRound firstRound = match.getSets().get(1).getLegs().get(1).getRounds().get(1);

        // Human turn and automatically generated Dart Bot turn should both be recorded.
        assertThat(firstRound.getTurns())
                .hasSize(2)
                .containsKeys(playerOneId, dartBotId);

        // Completing the round should return control to the human player.
        assertThat(match.getMatchProgress().getCurrentSet()).isEqualTo(1);
        assertThat(match.getMatchProgress().getCurrentLeg()).isEqualTo(1);
        assertThat(match.getMatchProgress().getCurrentRound()).isEqualTo(2);
        assertThat(match.getMatchProgress().getCurrentThrower()).isEqualTo(playerOneId);
    }

    // ===== Helpers =====
    private void configureMatchRepository() {
        matches.clear();

        lenient().when(matchRepository.save(any(X01Match.class))).thenAnswer(invocation -> {
            X01Match match = invocation.getArgument(0);

            if (match.getId() == null) {
                match.setId(new ObjectId());
            }

            matches.put(match.getId(), match);
            return match;
        });

        lenient().when(matchRepository.findById(any(ObjectId.class))).thenAnswer(invocation -> {
            ObjectId matchId = invocation.getArgument(0);
            return Optional.ofNullable(matches.get(matchId));
        });

        lenient().when(matchRepository.existsById(any(ObjectId.class))).thenAnswer(invocation -> {
            ObjectId matchId = invocation.getArgument(0);
            return matches.containsKey(matchId);
        });

        lenient().doAnswer(invocation -> {
            ObjectId matchId = invocation.getArgument(0);
            matches.remove(matchId);
            return null;
        }).when(matchRepository).deleteById(any(ObjectId.class));

        lenient().when(matchRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<ObjectId> matchIds = invocation.getArgument(0);
            List<X01Match> foundMatches = new ArrayList<>();

            for (ObjectId matchId : matchIds) {
                X01Match match = matches.get(matchId);

                if (match != null) {
                    foundMatches.add(match);
                }
            }
            return foundMatches;
        });
    }


    private void runScenario(ObjectId matchId, List<X01ScenarioStep> steps) {
        for (int i = 0; i < steps.size(); i++) {
            X01ScenarioStep step = steps.get(i);

            try {
                if (step instanceof X01ScenarioStep.AddTurn addTurn) {
                    matchService.addTurn(matchId, addTurn.request());
                } else if (step instanceof X01ScenarioStep.EditTurn editTurn) {
                    matchService.editTurn(matchId, editTurn.request());
                } else if (step instanceof X01ScenarioStep.DeleteLastTurn) {
                    matchService.deleteLastTurn(matchId);
                }
            } catch (RuntimeException exception) {
                throw new AssertionError(
                        "Scenario failed at step " + (i + 1) + " (" + step + ")",
                        exception
                );
            }
        }
    }

    private sealed interface X01ScenarioStep {

        record AddTurn(X01CreateTurnRequest request) implements X01ScenarioStep {
        }

        record EditTurn(X01EditTurnRequest request) implements X01ScenarioStep {
        }

        record DeleteLastTurn() implements X01ScenarioStep {
        }
    }
}