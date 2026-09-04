package nl.kmartin.dartsmatcherapi.features;

import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service.IX01DartBotService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.message.X01MatchMessageType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchProgress;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.repository.IX01MatchRepository;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchResultService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchSetupService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.X01MatchServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.service.IX01SetProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.service.IX01StandingsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.service.IX01StatisticsService;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import nl.kmartin.dartsmatcherapi.websocket.event.IWebSocketEventPublisher;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
    IWebSocketEventPublisher webSocketEventPublisher;

    @Mock
    IX01StandingsService standingsService;

    @Mock
    MessageResolver messageResolver;


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
                webSocketEventPublisher,
                standingsService,
                messageResolver
        );
    }

    @Test
    void createMatchSetsUpProcessesAndSavesMatch() {
        // Given
        X01CreateMatchRequest request = Mockito.mock(X01CreateMatchRequest.class);

        X01Match match = new X01Match();
        match.setId(new ObjectId());
        match.setMatchProgress(new X01MatchProgress(null, null, null, null));

        Mockito.when(matchSetupService.initializeNewMatch(request)).thenReturn(match);
        Mockito.when(x01MatchRepository.save(match)).thenReturn(match);

        // When
        X01Match result = x01MatchService.createMatch(request);

        // Then
        Assertions.assertSame(match, result);

        Mockito.verify(matchSetupService).initializeNewMatch(request);
        Mockito.verify(matchResultService).updateMatchResult(match);
        Mockito.verify(statisticsService).updatePlayerStatistics(match);
        Mockito.verify(matchProgressService).updateMatchProgress(match);
        Mockito.verify(standingsService).updateMatchStandings(match);
        Mockito.verify(x01MatchRepository).save(match);

        Mockito.verify(webSocketEventPublisher).broadcast(
                Mockito.anyString(),
                Mockito.eq(X01MatchMessageType.PROCESS_MATCH),
                Mockito.same(match)
        );

        Mockito.verifyNoInteractions(dartBotService);
    }

}
