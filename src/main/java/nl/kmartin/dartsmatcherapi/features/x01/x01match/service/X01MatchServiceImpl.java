package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegAlreadyWonException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnAlreadyExistsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.mapper.X01MatchExceptionMapper;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.message.X01MatchMessage;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.message.X01MatchMessageFactory;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.repository.IX01MatchRepository;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.service.IX01StandingsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.service.IX01StatisticsService;
import nl.kmartin.dartsmatcherapi.websocket.destination.WebSocketDestinations;
import nl.kmartin.dartsmatcherapi.websocket.event.publisher.IWebSocketEventPublisher;
import org.bson.types.ObjectId;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Coordinates creation, retrieval and mutation of X01 matches.
 *
 * Applies turns and edits, rebuilds derived match state, processes Dart Bot turns, persists changes
 * and publishes match updates. Maintains rematch references during creation, reprocessing and deletion.
 */
@Service
@Validated
public class X01MatchServiceImpl implements IX01MatchService {

    /**
     * Maximum consecutive bot turns while matches allow only one Dart Bot.
     *
     * Covers finishing a leg, winning the following leg and set with an opening
     * checkout, then taking the opening turn of the next set.
     */
    private static final int MAX_BOT_TURNS = 3;

    private final IX01MatchRepository matchRepository;
    private final IX01MatchSetupService matchSetupService;
    private final IX01MatchTurnService matchTurnService;
    private final IX01MatchRematchService matchRematchService;
    private final IX01MatchResultService matchResultService;
    private final IX01MatchProgressService matchProgressService;
    private final IX01StatisticsService statisticsService;
    private final IWebSocketEventPublisher webSocketEventPublisher;
    private final IX01StandingsService standingsService;
    private final X01MatchExceptionMapper matchExceptionMapper;

    public X01MatchServiceImpl(
            IX01MatchRepository matchRepository,
            IX01MatchSetupService matchSetupService,
            IX01MatchTurnService matchTurnService,
            IX01MatchRematchService matchRematchService,
            IX01MatchResultService matchResultService,
            IX01MatchProgressService matchProgressService,
            IX01StatisticsService statisticsService,
            IWebSocketEventPublisher webSocketEventPublisher,
            IX01StandingsService standingsService,
            X01MatchExceptionMapper matchExceptionMapper
    ) {
        this.matchRepository = matchRepository;
        this.matchSetupService = matchSetupService;
        this.matchTurnService = matchTurnService;
        this.matchRematchService = matchRematchService;
        this.matchResultService = matchResultService;
        this.matchProgressService = matchProgressService;
        this.statisticsService = statisticsService;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.standingsService = standingsService;
        this.matchExceptionMapper = matchExceptionMapper;
    }

    @Override
    @Transactional
    public X01Match createMatch(X01CreateMatchRequest request) {
        X01Match match = matchSetupService.initializeNewMatch(request);

        return saveMatchAndProcessBotTurns(match, X01MatchMessage.ProcessMatch::new);
    }

    @Override
    @Transactional
    public X01Match createRematch(ObjectId matchId) {
        X01Match matchToRematch = getMatch(matchId);
        X01Match rematch = matchRematchService.getOrCreateRematch(matchToRematch);

        // Skip persistence when the rematch already exists.
        if (matchToRematch.getRematchId() != null && Objects.equals(matchToRematch.getRematchId(), rematch.getId())) {
            return matchToRematch;
        }

        // Persist the new rematch, then link the original match to it.
        rematch = saveMatchAndProcessBotTurns(rematch, X01MatchMessage.ProcessMatch::new);
        matchToRematch.setRematchId(rematch.getId());

        return saveMatch(matchToRematch, X01MatchMessage.Rematch::new);
    }

    @Override
    @Transactional(readOnly = true)
    public X01Match getMatch(ObjectId matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException(X01Match.class, matchId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<X01Match> getMatches(List<ObjectId> matchIds) {
        // Index the matches that currently exist so the requested order can be restored.
        Map<ObjectId, X01Match> matchMap = matchRepository.findAllById(matchIds)
                .stream()
                .collect(Collectors.toMap(X01Match::getId, Function.identity()));

        // Preserve the supplied ID order while omitting matches that don't exist.
        return matchIds.stream()
                .map(matchMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void checkMatchExists(ObjectId matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException(X01Match.class, matchId);
        }
    }

    @Override
    @Transactional
    public X01Match addTurn(ObjectId matchId, X01CreateTurnRequest turnRequest) {
        X01Match match = getMatch(matchId);

        try {
            matchTurnService.addTurnToCurrentThrower(match, turnRequest);
        } catch (RuntimeException e) {
            throw matchExceptionMapper.map(e);
        }

        return saveMatchAndProcessBotTurns(match, X01MatchMessage.AddHumanTurn::new);
    }

    @Override
    @Transactional
    public X01Match editTurn(ObjectId matchId, X01EditTurnRequest turnRequest) {
        X01Match match = getMatch(matchId);

        try {
            matchTurnService.replaceTurn(match, turnRequest);
        } catch (RuntimeException e) {
            throw matchExceptionMapper.map(e);
        }

        return saveMatchAndProcessBotTurns(match, X01MatchMessage.EditTurn::new);
    }

    @Override
    @Transactional
    public X01Match deleteLastHumanTurn(ObjectId matchId) {
        X01Match match = getMatch(matchId);

        // Skip further processing when no turn was removed.
        if (!matchTurnService.deleteLastHumanTurn(match)) {
            return match;
        }

        return saveMatchAndProcessBotTurns(match, X01MatchMessage.DeleteLastTurn::new);
    }

    @Override
    @Transactional
    public void deleteMatch(ObjectId matchId) {
        checkMatchExists(matchId);

        clearRematchReferencesToMatch(matchId);

        matchRepository.deleteById(matchId);

        broadcastMatchEvent(matchId, new X01MatchMessage.DeleteMatch(matchId));
    }

    @Override
    @Transactional
    public X01Match resetMatch(ObjectId matchId) {
        X01Match match = getMatch(matchId);
        X01Match resetMatch = matchSetupService.resetMatch(match);

        return saveMatchAndProcessBotTurns(resetMatch, X01MatchMessage.ResetMatch::new);
    }

    @Override
    @Transactional
    public X01Match reprocessMatch(ObjectId matchId) {
        X01Match match = getMatch(matchId);

        matchRematchService.validateAndUpdateRematchId(match);

        return saveMatchAndProcessBotTurns(match, X01MatchMessage.ProcessMatch::new);
    }

    /**
     * Clears rematch references to the specified match, saving and publishing each affected match.
     *
     * @param matchId the referenced match id
     */
    private void clearRematchReferencesToMatch(ObjectId matchId) {
        matchRematchService.clearRematchReferencesToMatch(matchId)
                .forEach(match -> saveMatch(match, X01MatchMessage.ProcessMatch::new));
    }

    /**
     * Rebuilds, saves and publishes the match, then processes any following Dart Bot turns.
     *
     * @param match          the match to update and save
     * @param messageFactory creates the message for the triggering operation
     * @return the saved match after bot processing
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when bot processing encounters invalid match state or exceeds the turn limit
     */
    private X01Match saveMatchAndProcessBotTurns(X01Match match, X01MatchMessageFactory messageFactory) {
        X01Match savedMatch = updateAndSaveMatch(match, messageFactory);
        return processBotTurns(savedMatch);
    }

    /**
     * Processes automated Dart Bot turns while the current thrower is a Dart Bot.
     *
     * Rebuilds, saves and publishes the match after each turn.
     * Turn validation and missing-resource exceptions during automated turn processing indicate
     * invalid internal state rather than invalid user input, so they are wrapped in IllegalStateException.
     *
     * @param match the match whose derived state is up to date
     * @return the saved match after bot processing, or the supplied match when no bot turn is needed
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when bot processing encounters invalid match state or exceeds the turn limit
     */
    private X01Match processBotTurns(X01Match match) {
        X01Match savedMatch = match;

        try {
            for (int botTurn = 0; botTurn < MAX_BOT_TURNS; botTurn++) {
                if (!matchTurnService.addDartBotTurnToCurrentThrower(savedMatch)) {
                    return savedMatch;
                }

                savedMatch = updateAndSaveMatch(savedMatch, X01MatchMessage.AddBotTurn::new);
            }
        } catch (X01TurnAlreadyExistsException | X01LegAlreadyWonException | X01CheckoutInsufficientDartsException |
                 ResourceNotFoundException e) {
            throw new IllegalStateException("Dart Bot processing failed (matchId=" + savedMatch.getId() + ")", e);
        }

        if (matchProgressService.isCurrentThrowerDartBot(savedMatch)) {
            throw new IllegalStateException("Dart Bot turn limit exceeded (matchId=" + savedMatch.getId() + ")");
        }

        return savedMatch;
    }

    /**
     * Rebuilds, saves and publishes the current match state.
     *
     * @param match          the match to update and save
     * @param messageFactory creates the message from the saved match
     * @return the saved match
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     */
    private X01Match updateAndSaveMatch(X01Match match, X01MatchMessageFactory messageFactory) {
        updateMatch(match);
        return saveMatch(match, messageFactory);
    }

    /**
     * Rebuilds the calculated state of a match from its recorded history.
     *
     * @param match the match to rebuild
     */
    private void updateMatch(X01Match match) {
        // Rebuild results first so stale match history is removed before other calculations.
        matchResultService.updateMatchResult(match);

        // Rebuild player statistics from the normalized match history.
        statisticsService.updatePlayerStatistics(match);

        // Resolve or create the current set, leg and round.
        matchProgressService.updateMatchProgress(match);

        // Rebuild standings from the final current match structure.
        standingsService.updateMatchStandings(match);
    }

    /**
     * Increments the broadcast version, saves and publishes the match without rebuilding derived state.
     *
     * @param match          the match to save
     * @param messageFactory creates the message from the saved match
     * @return the saved match
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     */
    private X01Match saveMatch(X01Match match, X01MatchMessageFactory messageFactory) {
        match.setBroadcastVersion(match.getBroadcastVersion() + 1);
        X01Match saved = matchRepository.save(match);
        broadcastMatchEvent(saved.getId(), messageFactory.apply(saved));
        return saved;
    }

    /**
     * Publishes an X01 match message for broadcasting to match subscribers.
     *
     * @param matchId the match id
     * @param message the message to broadcast
     */
    private void broadcastMatchEvent(ObjectId matchId, X01MatchMessage<?> message) {
        webSocketEventPublisher.broadcast(
                WebSocketDestinations.broadcast(WebSocketDestinations.X01.MATCH, matchId),
                message
        );
    }
}