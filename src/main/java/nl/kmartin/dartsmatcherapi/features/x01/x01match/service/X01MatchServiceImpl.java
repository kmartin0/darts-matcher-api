package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurn;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service.IX01DartBotService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnMutation;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.message.X01MatchMessageType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.repository.IX01MatchRepository;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.service.IX01SetProgressService;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.service.IX01StandingsService;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.service.IX01StatisticsService;
import nl.kmartin.dartsmatcherapi.websocket.WebSocketDestinations;
import nl.kmartin.dartsmatcherapi.websocket.event.IWebSocketEventPublisher;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Coordinates creation, retrieval and mutation of X01 matches.
 *
 * Applies turns and edits, rebuilds derived match state, processes Dart Bot turns, persists changes
 * and publishes match updates.
 */
@Service
@Validated
public class X01MatchServiceImpl implements IX01MatchService {

    private static final int MAX_BOT_TURNS = 2;

    private final IX01MatchRepository matchRepository;
    private final IX01MatchSetupService matchSetupService;
    private final IX01MatchResultService matchResultService;
    private final IX01MatchProgressService matchProgressService;
    private final IX01StatisticsService statisticsService;
    private final IX01SetProgressService setProgressService;
    private final IX01LegService legService;
    private final IX01LegRoundService legRoundService;
    private final IX01DartBotService dartBotService;
    private final IWebSocketEventPublisher webSocketEventPublisher;
    private final IX01StandingsService standingsService;

    public X01MatchServiceImpl(
            IX01MatchRepository matchRepository,
            IX01MatchSetupService matchSetupService,
            IX01MatchResultService matchResultService,
            IX01MatchProgressService matchProgressService,
            IX01StatisticsService statisticsService,
            IX01SetProgressService setProgressService,
            IX01LegService legService,
            IX01LegRoundService legRoundService,
            IX01DartBotService dartBotService,
            IWebSocketEventPublisher webSocketEventPublisher,
            IX01StandingsService standingsService
    ) {
        this.matchRepository = matchRepository;
        this.matchSetupService = matchSetupService;
        this.matchResultService = matchResultService;
        this.matchProgressService = matchProgressService;
        this.statisticsService = statisticsService;
        this.setProgressService = setProgressService;
        this.legService = legService;
        this.legRoundService = legRoundService;
        this.dartBotService = dartBotService;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.standingsService = standingsService;
    }

    @Override
    @Transactional
    public X01Match createMatch(X01CreateMatchRequest request) {
        // Initialize the complete match state from the creation request.
        X01Match match = matchSetupService.initializeNewMatch(request);

        // Persist the initialized match and process any immediately scheduled Dart Bot turns.
        saveMatchAndProcessBotTurns(match, X01MatchMessageType.PROCESS_MATCH);

        return match;
    }

    @Override
    @Transactional(readOnly = true)
    public X01Match getMatch(ObjectId matchId) throws ResourceNotFoundException {
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

        // Preserve the supplied ID order while omitting matches that no longer exist.
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
    public X01Match addTurn(ObjectId matchId, X01CreateTurnRequest turn) {
        X01Match match = getMatch(matchId);

        // Apply the submitted turn to the currently active round and thrower.
        addTurnToCurrentPlayer(match, turn);

        // Rebuild and persist the match before processing any following Dart Bot turns.
        saveMatchAndProcessBotTurns(match, X01MatchMessageType.ADD_HUMAN_TURN);

        return match;
    }

    @Override
    @Transactional
    public X01Match editTurn(ObjectId matchId, X01EditTurnRequest turnRequest) {
        X01Match match = getMatch(matchId);

        // Resolve the leg containing the turn being edited.
        X01SetEntry setEntry = matchProgressService.getSetOrThrow(match, turnRequest.getSet());
        X01LegEntry legEntry = setProgressService.getLegOrThrow(setEntry.set(), turnRequest.getLeg());

        int x01 = match.getMatchSettings().getX01();
        boolean trackDoubles = match.getMatchSettings().isTrackDoubles();

        // Replace the turn and rebuild the leg state affected by the change.
        legService.replaceTurn(new X01TurnMutation(
                x01,
                legEntry.leg(),
                turnRequest.getRound(),
                turnRequest.getScore(),
                turnRequest.getCheckoutDartsUsed(),
                turnRequest.getDoublesMissed(),
                turnRequest.getPlayerId(),
                trackDoubles
        ));

        // Rebuild and persist the match before processing any following Dart Bot turns.
        saveMatchAndProcessBotTurns(match, X01MatchMessageType.EDIT_TURN);

        return match;
    }

    @Override
    @Transactional
    public X01Match deleteLastTurn(ObjectId matchId) {
        X01Match match = getMatch(matchId);

        // Remove the latest turn and any trailing empty match structure.
        matchProgressService.removeLastTurnFromMatch(match);

        // Rebuild and persist the match before processing any following Dart Bot turns.
        saveMatchAndProcessBotTurns(match, X01MatchMessageType.DELETE_LAST_TURN);

        return match;
    }

    @Override
    @Transactional
    public void deleteMatch(ObjectId matchId) {
        checkMatchExists(matchId);

        // Delete the aggregate before publishing its removal to connected clients.
        matchRepository.deleteById(matchId);
        broadcastMatchEvent(matchId, X01MatchMessageType.DELETE_MATCH, matchId);
    }

    @Override
    @Transactional
    public X01Match resetMatch(ObjectId matchId) {
        X01Match match = getMatch(matchId);

        // Create the reset match state while preserving its identity and configuration.
        X01Match resetMatch = matchSetupService.resetMatch(match);

        // Persist the reset state and process any immediately scheduled Dart Bot turns.
        saveMatchAndProcessBotTurns(resetMatch, X01MatchMessageType.RESET_MATCH);

        return resetMatch;
    }

    @Override
    @Transactional
    public X01Match reprocessMatch(ObjectId matchId) {
        X01Match match = getMatch(matchId);

        // Rebuild calculated state from the recorded history and persist the normalized aggregate.
        saveMatchAndProcessBotTurns(match, X01MatchMessageType.PROCESS_MATCH);

        return match;
    }

    /**
     * Applies a turn request to the current thrower in the active round.
     *
     * @param match       the match to update
     * @param turnRequest the turn creation request
     */
    private void addTurnToCurrentPlayer(X01Match match, X01CreateTurnRequest turnRequest) {
        addTurnToCurrentPlayer(match, turnRequest.getScore(), turnRequest.getCheckoutDartsUsed(), turnRequest.getDoublesMissed());
    }

    /**
     * Applies a generated Dart Bot turn to the current thrower in the active round.
     *
     * @param match       the match to update
     * @param dartBotTurn the generated Dart Bot turn
     */
    private void addTurnToCurrentPlayer(X01Match match, X01DartBotTurn dartBotTurn) {
        addTurnToCurrentPlayer(match, dartBotTurn.score(), dartBotTurn.checkoutDartsUsed(), dartBotTurn.doublesMissed());
    }

    /**
     * Applies turn values to the current thrower in the active round.
     *
     * @param match             the match to update
     * @param score             the points scored in the turn
     * @param checkoutDartsUsed the number of darts used for the checkout
     * @param doublesMissed     the number of doubles missed
     */
    private void addTurnToCurrentPlayer(X01Match match, int score, Integer checkoutDartsUsed, Integer doublesMissed) {
        // Resolve or create the active set, leg and round.
        X01SetEntry currentSetEntry = matchProgressService.getCurrentSetOrCreate(match)
                .orElseThrow(() -> new ResourceNotFoundException(X01Set.class, null));

        X01LegEntry currentLegEntry = matchProgressService.getCurrentLegOrCreate(match, currentSetEntry)
                .orElseThrow(() -> new ResourceNotFoundException(X01Leg.class, null));

        X01LegRoundEntry currentRoundEntry = matchProgressService.getCurrentLegRoundOrCreate(
                        match,
                        currentLegEntry.leg()
                )
                .orElseThrow(() -> new ResourceNotFoundException(X01LegRound.class, null));

        // Determine the current thrower from the scores already recorded in the active round.
        ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(
                currentRoundEntry.round(),
                currentLegEntry.leg().getThrowsFirst(),
                match.getPlayers()
        );

        int x01 = match.getMatchSettings().getX01();
        boolean trackDoubles = match.getMatchSettings().isTrackDoubles();

        // Apply the turn and rebuild the affected leg state.
        legService.applyTurn(new X01TurnMutation(
                x01,
                currentLegEntry.leg(),
                currentRoundEntry.roundNumber(),
                score,
                checkoutDartsUsed,
                doublesMissed,
                currentThrower,
                trackDoubles
        ));
    }

    /**
     * Saves a match and automatically processes consecutive Dart Bot turns.
     *
     * @param match       the match to save
     * @param messageType the message type for the triggering operation
     */
    private void saveMatchAndProcessBotTurns(X01Match match, X01MatchMessageType messageType) {
        // Process and persist the triggering match state before checking whether a bot should throw.
        saveMatch(match, messageType);

        int botTurnsProcessed = 0;

        // Continue until control passes to a human player or the match concludes.
        while (isCurrentThrowerDartBot(match)) {
            if (botTurnsProcessed >= MAX_BOT_TURNS) {
                throw new IllegalStateException(
                        "Invalid match state: three bot turns in a row are not allowed (matchId=" + match.getId() + ")"
                );
            }

            // Generate and apply the Dart Bot turn before rebuilding and publishing the resulting state.
            X01DartBotTurn dartBotTurn = dartBotService.createDartBotTurn(match);
            addTurnToCurrentPlayer(match, dartBotTurn);
            saveMatch(match, X01MatchMessageType.ADD_BOT_TURN);

            botTurnsProcessed++;
        }
    }

    /**
     * Rebuilds, persists and broadcasts the current match state.
     *
     * @param match       the match to save
     * @param messageType the message type to publish
     */
    private void saveMatch(X01Match match, X01MatchMessageType messageType) {
        if (messageType == X01MatchMessageType.DELETE_MATCH) {
            throw new IllegalArgumentException("Invalid event type for save operation: " + messageType);
        }

        // Rebuild all derived state before persisting and publishing the aggregate.
        updateMatch(match);

        matchRepository.save(match);
        broadcastMatchEvent(match.getId(), messageType, match);
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

        // Increment the version published to connected clients.
        match.setBroadcastVersion(match.getBroadcastVersion() + 1);
    }

    /**
     * Determines whether the current thrower is a Dart Bot.
     *
     * @param match the match to inspect
     * @return true when the current thrower is a Dart Bot
     */
    private boolean isCurrentThrowerDartBot(X01Match match) {
        ObjectId currentThrower = match.getMatchProgress().getCurrentThrower();
        if (currentThrower == null) return false;

        return getPlayerById(match, currentThrower)
                .map(player -> player.getPlayerType() == PlayerType.DART_BOT)
                .orElse(false);
    }

    /**
     * Finds a match player by id.
     *
     * @param match    the match containing the players
     * @param playerId the player id
     * @return the matching player, or empty when the player is not found
     */
    private Optional<X01MatchPlayer> getPlayerById(X01Match match, ObjectId playerId) {
        return match.getPlayers()
                .stream()
                .filter(player -> Objects.equals(player.getPlayerId(), playerId))
                .findFirst();
    }

    /**
     * Broadcasts an X01 match event to match subscribers.
     *
     * @param matchId     the match id
     * @param messageType the message type
     * @param payload     the event payload
     * @param <P>         the payload type
     */
    private <P> void broadcastMatchEvent(ObjectId matchId, X01MatchMessageType messageType, P payload) {
        webSocketEventPublisher.broadcast(
                WebSocketDestinations.broadcast(WebSocketDestinations.X01.MATCH, matchId),
                messageType,
                payload
        );
    }
}