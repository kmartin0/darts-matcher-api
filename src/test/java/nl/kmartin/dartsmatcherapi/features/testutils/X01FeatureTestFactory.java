package nl.kmartin.dartsmatcherapi.features.testutils;

import nl.kmartin.dartsmatcherapi.features.dartboard.service.DartboardServiceImpl;
import nl.kmartin.dartsmatcherapi.features.dartboard.service.IDartboardService;
import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.service.IX01AverageStatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01averagestatistics.service.X01AverageStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.IX01CheckoutService;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.service.X01CheckoutServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.service.IX01CheckoutStatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkoutstatistics.service.X01CheckoutStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service.*;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.*;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.X01LegRoundServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.repository.IX01MatchRepository;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.*;
import nl.kmartin.dartsmatcherapi.features.x01.x01resultstatistics.service.IX01ResultStatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01resultstatistics.service.X01ResultStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01rules.service.IX01RulesService;
import nl.kmartin.dartsmatcherapi.features.x01.x01rules.service.X01RulesServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.service.IX01ScoreStatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01scorestatistics.service.X01ScoreStatisticsServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.service.*;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.service.IX01StandingsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.service.X01StandingsServiceImpl;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.service.IX01StatisticsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.service.X01StatisticsServiceImpl;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
import nl.kmartin.dartsmatcherapi.websocket.event.IWebSocketEventPublisher;
import nl.kmartin.dartsmatcherapi.websocket.event.WebSocketEventPublisherImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class X01FeatureTestFactory {

    private final IX01MatchRepository matchRepositoryMock;
    private final MessageResolver messageResolverMock;
    private final ApplicationEventPublisher eventPublisherMock;

    public X01FeatureTestFactory(
            IX01MatchRepository matchRepositoryMock,
            MessageResolver messageResolverMock,
            ApplicationEventPublisher eventPublisherMock
    ) {
        this.matchRepositoryMock = matchRepositoryMock;
        this.messageResolverMock = messageResolverMock;
        this.eventPublisherMock = eventPublisherMock;
    }

    public IX01MatchService createMatchService() {
        return new X01MatchServiceImpl(
                matchRepositoryMock,
                createMatchSetupService(),
                createMatchResultService(),
                createMatchProgressService(),
                createStatisticsService(),
                createSetProgressService(),
                createLegService(),
                createLegRoundService(),
                createDartBotService(),
                createWebsocketEventPublisher(),
                createStandingsService()
        );
    }

    public IX01MatchSetupService createMatchSetupService() {
        return new X01MatchSetupServiceImpl();
    }

    public IX01MatchResultService createMatchResultService() {
        return new X01MatchResultServiceImpl(createSetResultService(), createStandingsService());
    }

    public IX01MatchProgressService createMatchProgressService() {
        return new X01MatchProgressServiceImpl(
                createSetService(),
                createSetProgressService(),
                createLegProgressService(),
                createLegRoundService(),
                createRulesService()
        );
    }

    public IX01SetService createSetService() {
        return new X01SetServiceImpl();
    }

    public IX01SetResultService createSetResultService() {
        return new X01SetResultServiceImpl(createLegResultService(), createStandingsService());
    }

    public IX01SetProgressService createSetProgressService() {
        return new X01SetProgressServiceImpl(createLegService(), createLegProgressService(), createRulesService());
    }

    public IX01LegService createLegService() {
        return new X01LegServiceImpl(
                messageResolverMock,
                createLegProgressService(),
                createLegResultService(),
                createLegRoundService()
        );
    }

    public IX01LegResultService createLegResultService() {
        return new X01LegResultServiceImpl(createLegRoundService());
    }

    public IX01LegProgressService createLegProgressService() {
        return new X01LegProgressServiceImpl(createLegRoundService());
    }

    public IX01LegRoundService createLegRoundService() {
        return new X01LegRoundServiceImpl(createCheckoutService());
    }

    public IX01StandingsService createStandingsService() {
        return new X01StandingsServiceImpl(
                createMatchProgressService(),
                createRulesService()
        );
    }

    public IX01StatisticsService createStatisticsService() {
        return new X01StatisticsServiceImpl(
                createResultStatisticsService(),
                createScoreStatisticsService(),
                createCheckoutStatisticsService(),
                createAverageStatisticsService(),
                createLegService()
        );
    }

    public IX01ResultStatisticsService createResultStatisticsService() {
        return new X01ResultStatisticsServiceImpl();
    }

    public IX01ScoreStatisticsService createScoreStatisticsService() {
        return new X01ScoreStatisticsServiceImpl();
    }

    public IX01CheckoutStatisticsService createCheckoutStatisticsService() {
        return new X01CheckoutStatisticsServiceImpl();
    }

    public IX01AverageStatisticsService createAverageStatisticsService() {
        return new X01AverageStatisticsServiceImpl();
    }

    public IX01DartBotService createDartBotService() {
        return new X01DartBotServiceImpl(
                createMatchProgressService(),
                createDartBotThrowSimulator(),
                createLegResultService()
        );
    }

    public IX01DartBotThrowSimulator createDartBotThrowSimulator() {
        return new X01DartBotThrowSimulatorImpl(
                createDartboardService(),
                createCheckoutService(),
                createDartBotCheckoutPolicy(),
                createDartBotAccuracyCalculator(),
                createDartBotScoringStrategy()
        );
    }

    public IX01DartBotScoringStrategy createDartBotScoringStrategy() {
        return new X01DartBotScoringStrategyImpl();
    }

    public IX01DartBotCheckoutPolicy createDartBotCheckoutPolicy() {
        return new X01DartBotCheckoutPolicyImpl(createCheckoutService());
    }

    public IX01DartBotDeviationCalculator createDartBotAccuracyCalculator() {
        return new X01DartBotDeviationCalculatorImpl();
    }

    public IX01CheckoutService createCheckoutService() {
        Resource checkoutsResource = new ClassPathResource("data/checkouts.json");
        return new X01CheckoutServiceImpl(checkoutsResource, messageResolverMock);
    }

    public IDartboardService createDartboardService() {
        return new DartboardServiceImpl();
    }

    public IX01RulesService createRulesService() {
        return new X01RulesServiceImpl();
    }

    public IWebSocketEventPublisher createWebsocketEventPublisher() {
        return new WebSocketEventPublisherImpl(eventPublisherMock);
    }
}