package nl.kmartin.dartsmatcherapi.features;

import nl.kmartin.dartsmatcherapi.common.IEventPublisherService;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service.IX01DartBotService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.*;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.repository.IX01MatchRepository;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.*;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.service.IX01SetProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.service.IX01StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class X01MatchServiceTests {

    private IX01MatchService x01MatchService;

    @Mock
    IX01MatchRepository x01MatchRepository;

    @Mock
    IX01MatchSetupService matchSetupService;

    @Mock
    IX01MatchResultService matchResultService;

    @Mock
    IX01MatchProgressService matchProgressService;

    @Mock
    IX01StatisticsService statisticsService;

    @Mock
    IX01SetProgressService setProgressService;

    @Mock
    IX01LegService legService;

    @Mock
    IX01LegRoundService legRoundService;

    @Mock
    IX01DartBotService dartBotService;

    @Mock
    IEventPublisherService eventPublisherService;


    @BeforeEach
    void setUp() {
        this.x01MatchService = new X01MatchServiceImpl(
                x01MatchRepository,
                matchSetupService,
                matchResultService,
                matchProgressService,
                statisticsService,
                setProgressService,
                legService,
                legRoundService,
                dartBotService,
                eventPublisherService
        );
    }

    @Test
    void test_1() {
        // Given
        Mockito.when(x01MatchRepository.save(Mockito.any(X01Match.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        X01Match x01Match = new X01Match();
        X01ClearByTwoRule clearByTwoRule = new X01ClearByTwoRule(false, 0);
        X01BestOf bestOf = new X01BestOf(1, 5, X01BestOfType.SETS, clearByTwoRule, clearByTwoRule, clearByTwoRule);
        X01MatchSettings x01MatchSettings = new X01MatchSettings(501, false, bestOf);
        x01Match.setMatchSettings(x01MatchSettings);

        X01MatchPlayer player1 = new X01MatchPlayer();
        player1.setPlayerName("John Doe");
        player1.setPlayerType(PlayerType.HUMAN);

        X01MatchPlayer player2 = new X01MatchPlayer();
        player1.setPlayerName("Jane Doe");
        player1.setPlayerType(PlayerType.HUMAN);

        x01Match.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));
        x01Match.setMatchProgress(new X01MatchProgress(1, 1, 1, player1.getPlayerId()));

        // When
        X01Match createdMatch = x01MatchService.createMatch(x01Match);
        Mockito.when(x01MatchRepository.findById(createdMatch.getId())).thenReturn(Optional.of(createdMatch));
        Mockito.when(matchProgressService.getCurrentSetOrCreate(x01Match)).thenReturn(Optional.of(new X01SetEntry(1, new X01Set())));
        Mockito.when(matchProgressService.getCurrentLegOrCreate(Mockito.any(), Mockito.any())).thenReturn(Optional.of(new X01LegEntry(1, new X01Leg())));
        Mockito.when(matchProgressService.getCurrentLegRoundOrCreate(Mockito.any(), Mockito.any())).thenReturn(Optional.of(new X01LegRoundEntry(1, new X01LegRound())));

        X01Turn x01Turn = new X01Turn(60, 3, 0);

        x01MatchService.addTurn(createdMatch.getId(), x01Turn);

        // Then
        System.out.println(createdMatch);
    }

}
