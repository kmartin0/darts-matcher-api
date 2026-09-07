package nl.kmartin.dartsmatcherapi.features.x01.x01match;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOfType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01ClearByTwoRule;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchSettings;
import org.bson.types.ObjectId;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class X01MatchServiceTestData {

    private final ObjectMapper objectMapper;

    public X01MatchServiceTestData(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ===== Scenario 1 =====
    public X01CreateMatchRequest getScenarioOneCreateMatchRequest() {
        return new X01CreateMatchRequest(
                new X01MatchSettings(
                        501,
                        true,
                        new X01BestOf(
                                1,
                                3,
                                X01BestOfType.LEGS,
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0)
                        )
                ),
                List.of(
                        new X01CreateMatchRequest.Player("Player 1", PlayerType.HUMAN, null)
                )
        );
    }

    public X01Match getScenarioOneExpectedResult(
            ObjectId matchId,
            ObjectId playerOneId,
            Instant startDate,
            Instant endDate
    ) {
        return readExpectedMatch(
                "data/x01-match-service/scenario-1-expected.json",
                new Placeholder("${matchId}", matchId.toHexString()),
                new Placeholder("${player1Id}", playerOneId.toHexString()),
                new Placeholder("${startDate}", startDate.toString()),
                new Placeholder("${endDate}", endDate.toString())
        );
    }

    // ===== Scenario 2 =====
    public X01CreateMatchRequest getScenarioTwoCreateMatchRequest() {
        return new X01CreateMatchRequest(
                new X01MatchSettings(
                        501,
                        true,
                        new X01BestOf(
                                1,
                                3,
                                X01BestOfType.LEGS,
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0)
                        )
                ),
                List.of(
                        new X01CreateMatchRequest.Player("Player 1", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Player 2", PlayerType.HUMAN, null)
                )
        );
    }

    public X01Match getScenarioTwoExpectedResult(
            ObjectId matchId,
            ObjectId playerOneId,
            ObjectId playerTwoId,
            Instant startDate,
            Instant endDate
    ) {
        return readExpectedMatch(
                "data/x01-match-service/scenario-2-expected.json",
                new Placeholder("${matchId}", matchId.toHexString()),
                new Placeholder("${player1Id}", playerOneId.toHexString()),
                new Placeholder("${player2Id}", playerTwoId.toHexString()),
                new Placeholder("${startDate}", startDate.toString()),
                new Placeholder("${endDate}", endDate.toString())
        );
    }

    // ===== Scenario 3 =====
    public X01CreateMatchRequest getScenarioThreeCreateMatchRequest() {
        return new X01CreateMatchRequest(
                new X01MatchSettings(
                        501,
                        false,
                        new X01BestOf(
                                3,
                                3,
                                X01BestOfType.SETS,
                                new X01ClearByTwoRule(true, 2),
                                new X01ClearByTwoRule(true, 2),
                                new X01ClearByTwoRule(true, 2)
                        )
                ),
                List.of(
                        new X01CreateMatchRequest.Player("Player 1", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Player 2", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Player 3", PlayerType.HUMAN, null)
                )
        );
    }

    public X01Match getScenarioThreeExpectedResult(
            ObjectId matchId,
            ObjectId playerOneId,
            ObjectId playerTwoId,
            ObjectId playerThreeId,
            Instant startDate,
            Instant endDate
    ) {
        return readExpectedMatch(
                "data/x01-match-service/scenario-3-expected.json",
                new Placeholder("${matchId}", matchId.toHexString()),
                new Placeholder("${player1Id}", playerOneId.toHexString()),
                new Placeholder("${player2Id}", playerTwoId.toHexString()),
                new Placeholder("${player3Id}", playerThreeId.toHexString()),
                new Placeholder("${startDate}", startDate.toString()),
                new Placeholder("${endDate}", endDate.toString())
        );
    }

    // ===== Scenario 4 =====
    public X01CreateMatchRequest getScenarioFourCreateMatchRequest() {
        return new X01CreateMatchRequest(
                new X01MatchSettings(
                        501,
                        true,
                        new X01BestOf(
                                3,
                                3,
                                X01BestOfType.SETS,
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0)
                        )
                ),
                List.of(
                        new X01CreateMatchRequest.Player("Player 1", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Player 2", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Player 3", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Player 4", PlayerType.HUMAN, null)
                )
        );
    }

    public X01Match getScenarioFourExpectedResult(
            ObjectId matchId,
            ObjectId playerOneId,
            ObjectId playerTwoId,
            ObjectId playerThreeId,
            ObjectId playerFourId,
            Instant startDate,
            Instant endDate
    ) {
        return readExpectedMatch(
                "data/x01-match-service/scenario-4-expected.json",
                new Placeholder("${matchId}", matchId.toHexString()),
                new Placeholder("${player1Id}", playerOneId.toHexString()),
                new Placeholder("${player2Id}", playerTwoId.toHexString()),
                new Placeholder("${player3Id}", playerThreeId.toHexString()),
                new Placeholder("${player4Id}", playerFourId.toHexString()),
                new Placeholder("${startDate}", startDate.toString()),
                new Placeholder("${endDate}", endDate.toString())
        );
    }

    // ===== Scenario 5 =====
    public X01CreateMatchRequest getScenarioFiveCreateMatchRequest() {
        return new X01CreateMatchRequest(
                new X01MatchSettings(
                        501,
                        false,
                        new X01BestOf(
                                1,
                                4,
                                X01BestOfType.LEGS,
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0)
                        )
                ),
                List.of(
                        new X01CreateMatchRequest.Player("Player 1", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Player 2", PlayerType.HUMAN, null)
                )
        );
    }

    public X01Match getScenarioFiveExpectedResult(
            ObjectId matchId,
            ObjectId playerOneId,
            ObjectId playerTwoId,
            Instant startDate,
            Instant endDate
    ) {
        return readExpectedMatch(
                "data/x01-match-service/scenario-5-expected.json",
                new Placeholder("${matchId}", matchId.toHexString()),
                new Placeholder("${player1Id}", playerOneId.toHexString()),
                new Placeholder("${player2Id}", playerTwoId.toHexString()),
                new Placeholder("${startDate}", startDate.toString()),
                new Placeholder("${endDate}", endDate.toString())
        );
    }

    // ===== Scenario 6 =====
    public X01CreateMatchRequest getScenarioSixCreateMatchRequest() {
        return new X01CreateMatchRequest(
                new X01MatchSettings(
                        501,
                        false,
                        new X01BestOf(
                                3,
                                3,
                                X01BestOfType.SETS,
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0)
                        )
                ),
                List.of(
                        new X01CreateMatchRequest.Player("Player 1", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Player 2", PlayerType.HUMAN, null)
                )
        );
    }

    public X01Match getScenarioSixExpectedResult(
            ObjectId matchId,
            ObjectId playerOneId,
            ObjectId playerTwoId,
            Instant startDate
    ) {
        return readExpectedMatch(
                "data/x01-match-service/scenario-6-expected.json",
                new Placeholder("${matchId}", matchId.toHexString()),
                new Placeholder("${player1Id}", playerOneId.toHexString()),
                new Placeholder("${player2Id}", playerTwoId.toHexString()),
                new Placeholder("${startDate}", startDate.toString())
        );
    }

    // ===== Scenario 7 =====
    public X01Match getScenarioSevenInitial() {
        return readExpectedMatch("data/x01-match-service/scenario-7-initial.json");
    }

    public X01Match getScenarioSevenExpectedResult() {
        return readExpectedMatch("data/x01-match-service/scenario-7-expected.json");
    }

    // ===== Scenario 8 =====
    public X01Match getScenarioEightInitial() {
        return readExpectedMatch("data/x01-match-service/scenario-8-initial.json");
    }

    public X01Match getScenarioEightExpectedResult(Instant startDate) {
        return readExpectedMatch(
                "data/x01-match-service/scenario-8-expected.json",
                new Placeholder("${startDate}", startDate.toString())
        );
    }

    // ===== Scenario 9 =====
    public X01Match getScenarioNineInitial() {
        return readExpectedMatch("data/x01-match-service/scenario-9-initial.json");
    }

    // ===== Scenario 10 =====
    public X01Match getScenarioTenInitial() {
        return readExpectedMatch("data/x01-match-service/scenario-10-initial.json");
    }

    // ===== Scenario 11 =====
    public List<X01Match> getScenarioElevenInitial() {
        X01Match matchOne = new X01Match();
        matchOne.setId(new ObjectId("68bf0ad46f84c37a912e5510"));

        X01Match matchTwo = new X01Match();
        matchTwo.setId(new ObjectId("68bf0ad46f84c37a912e5511"));

        X01Match matchThree = new X01Match();
        matchThree.setId(new ObjectId("68bf0ad46f84c37a912e5512"));

        return List.of(matchOne, matchTwo, matchThree);
    }


    // ===== Scenario 12 =====
    public X01CreateMatchRequest getScenarioTwelveCreateMatchRequest() {
        return new X01CreateMatchRequest(
                new X01MatchSettings(
                        501,
                        false,
                        new X01BestOf(
                                1,
                                3,
                                X01BestOfType.LEGS,
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0),
                                new X01ClearByTwoRule(false, 0)
                        )
                ),
                List.of(
                        new X01CreateMatchRequest.Player("Player 1", PlayerType.HUMAN, null),
                        new X01CreateMatchRequest.Player("Dart Bot", PlayerType.DART_BOT, new X01DartBotSettings(60))
                )
        );
    }

    // ===== Helpers =====
    private X01Match readExpectedMatch(String path, Placeholder... placeholders) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String json = resource.getContentAsString(StandardCharsets.UTF_8);

            for (Placeholder placeholder : placeholders) {
                json = json.replace(placeholder.key(), placeholder.value());
            }

            return objectMapper.readValue(json, X01Match.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read expected X01 match from " + path, exception);
        }
    }

    private record Placeholder(String key, String value) {
    }
}