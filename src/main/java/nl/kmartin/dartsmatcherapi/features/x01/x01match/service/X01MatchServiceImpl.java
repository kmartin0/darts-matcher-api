package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.error.response.ErrorTargets;
import nl.kmartin.dartsmatcherapi.error.response.TargetError;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model.X01CheckoutInsufficientDartsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.model.X01DartBotTurn;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service.IX01DartBotService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegAlreadyWonException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnAlreadyExistsException;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnEntry;
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
import nl.kmartin.dartsmatcherapi.i18n.MessageKeys;
import nl.kmartin.dartsmatcherapi.i18n.MessageResolver;
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
    private final MessageResolver messageResolver;

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
            IX01StandingsService standingsService, MessageResolver messageResolver
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
        this.messageResolver = messageResolver;
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
    public X01Match addTurn(ObjectId matchId, X01CreateTurnRequest turnRequest) {
        X01Match match = getMatch(matchId);

        // Apply the submitted turn to the currently active round and thrower.
        try {
            addTurnToCurrentPlayer(match, turnRequest.getScore(), turnRequest.getCheckoutDartsUsed(), turnRequest.getDoublesMissed());
        } catch (X01TurnAlreadyExistsException e) {
            throw mapTurnAlreadyExistsException();
        } catch (X01CheckoutInsufficientDartsException e) {
            throw mapCheckoutInsufficientDartsException(e);
        } catch (X01LegAlreadyWonException e) {
            throw mapLegAlreadyWonException();
        }

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
        try {
            legService.replaceTurn(new X01TurnMutation(
                    x01,
                    legEntry.leg(),
                    turnRequest.getRound(),
                    turnRequest.getScore(),
                    turnRequest.getDoublesMissed(),
                    turnRequest.getCheckoutDartsUsed(),
                    turnRequest.getPlayerId(),
                    trackDoubles
            ));
        } catch (X01CheckoutInsufficientDartsException e) {
            throw mapCheckoutInsufficientDartsException(e);
        } catch (X01LegAlreadyWonException e) {
            throw mapLegAlreadyWonException();
        }

        // Rebuild and persist the match before processing any following Dart Bot turns.
        saveMatchAndProcessBotTurns(match, X01MatchMessageType.EDIT_TURN);

        return match;
    }

    @Override
    @Transactional
    public X01Match deleteLastHumanTurn(ObjectId matchId) {
        X01Match match = getMatch(matchId);

        // Leave the match unchanged when there is no human turn to undo.
        if (!hasHumanTurn(match)) {
            return match;
        }

        // Repeatedly delete the last turn from the match until a human turn has been deleted.
        // Stop early if no turn remains or the removed turn's player cannot be found.
        while (true) {
            Optional<X01TurnEntry> removedTurn = matchProgressService.removeLastTurnFromMatch(match);
            if (removedTurn.isEmpty()) break;

            Optional<X01MatchPlayer> removedTurnPlayer = getPlayerById(match, removedTurn.get().playerId());
            if (removedTurnPlayer.isEmpty()) break;

            if (removedTurnPlayer.get().getPlayerType() != PlayerType.DART_BOT) break;
        }

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
     * Applies turn values to the current thrower in the active round.
     *
     * @param match             the match to update
     * @param score             the points scored in the turn
     * @param checkoutDartsUsed the number of darts used for the checkout
     * @param doublesMissed     the number of doubles missed
     * @throws X01TurnAlreadyExistsException         when the current thrower already has a turn in the active round
     * @throws X01LegAlreadyWonException             when another player's turn is applied after the leg has been won
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     * @throws ResourceNotFoundException             when the active set, leg or round cannot be resolved
     */
    private void addTurnToCurrentPlayer(X01Match match, int score, Integer checkoutDartsUsed, Integer doublesMissed) {
        // Resolve or create the active set, leg and round.
        X01SetEntry currentSetEntry = matchProgressService.getCurrentSetOrCreate(match)
                .orElseThrow(() -> new ResourceNotFoundException(X01Set.class, null));

        X01LegEntry currentLegEntry = matchProgressService.getCurrentLegOrCreate(match, currentSetEntry)
                .orElseThrow(() -> new ResourceNotFoundException(X01Leg.class, null));

        X01LegRoundEntry currentRoundEntry = matchProgressService.getCurrentLegRoundOrCreate(match, currentLegEntry.leg())
                .orElseThrow(() -> new ResourceNotFoundException(X01LegRound.class, null));

        // Determine the current thrower from the turns already recorded in the active round.
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
                doublesMissed,
                checkoutDartsUsed,
                currentThrower,
                trackDoubles
        ));
    }

    /**
     * Saves a match and automatically processes consecutive Dart Bot turns.
     *
     * @param match       the match to save
     * @param messageType the message type for the triggering operation
     * @throws X01TurnAlreadyExistsException         when the current thrower already has a turn in the active round
     * @throws X01LegAlreadyWonException             when another player's turn is applied after the leg has been won
     * @throws X01CheckoutInsufficientDartsException when the checkout requires more darts than were used
     * @throws ResourceNotFoundException             when the active set, leg or round cannot be resolved
     * @throws OptimisticLockingFailureException     when the match was modified concurrently
     * @throws IllegalStateException                 when more than the allowed number of consecutive Dart Bot turns is reached,
     *                                               or when the current Dart Bot state cannot be resolved
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
            X01DartBotTurn dartBotTurn = createDartBotTurnForCurrentPlayer(match);
            addTurnToCurrentPlayer(match, dartBotTurn.score(), dartBotTurn.checkoutDartsUsed(), dartBotTurn.doublesMissed());
            saveMatch(match, X01MatchMessageType.ADD_BOT_TURN);

            botTurnsProcessed++;
        }
    }

    /**
     * Creates a Dart Bot turn for the current configured Dart Bot player and active leg.
     *
     * @param match the match containing the current Dart Bot turn state
     * @return the generated Dart Bot turn
     * @throws IllegalStateException when the current thrower is not a configured Dart Bot or the current leg cannot be resolved
     */
    private X01DartBotTurn createDartBotTurnForCurrentPlayer(X01Match match) {
        ObjectId currentThrower = match.getMatchProgress().getCurrentThrower();

        X01MatchPlayer dartBotPlayer = getPlayerById(match, currentThrower)
                .filter(player ->
                        player.getPlayerType() == PlayerType.DART_BOT && player.getX01DartBotSettings() != null
                )
                .orElseThrow(() -> new IllegalStateException("Current thrower is not a configured Dart Bot"));

        X01LegEntry currentLegEntry = matchProgressService.getCurrentLeg(match)
                .orElseThrow(() -> new IllegalStateException("Unable to resolve the current leg for Dart Bot turn creation"));

        return dartBotService.createDartBotTurn(
                dartBotPlayer,
                currentLegEntry.leg(),
                match.getMatchSettings().getX01(),
                match.getMatchSettings().isTrackDoubles()
        );
    }

    /**
     * Rebuilds, persists and broadcasts the current match state.
     *
     * @param match       the match to save
     * @param messageType the message type to publish
     * @throws OptimisticLockingFailureException when the match was modified concurrently
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
     * Checks whether the match contains a human player turn anywhere in the match.
     *
     * @param match the match to inspect
     * @return whether any recorded turn belongs to a human player
     */
    private boolean hasHumanTurn(X01Match match) {
        return match.getSets().values().stream()
                .flatMap(set -> set.getLegs().values().stream())
                .flatMap(leg -> leg.getRounds().values().stream())
                .flatMap(round -> round.getTurns().keySet().stream())
                .anyMatch(playerId -> getPlayerById(match, playerId)
                        .map(player -> player.getPlayerType() == PlayerType.HUMAN)
                        .orElse(false)
                );
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

    /**
     * Maps an existing-turn domain error to the corresponding request-field error.
     *
     * @return the mapped invalid-arguments exception
     */
    private InvalidArgumentsException mapTurnAlreadyExistsException() {
        return new InvalidArgumentsException(
                new TargetError(
                        ErrorTargets.SCORE,
                        messageResolver.getMessage(MessageKeys.MESSAGE_X01_TURN_ALREADY_EXISTS)
                )
        );
    }

    /**
     * Maps an already-won leg error to the corresponding request-field error.
     *
     * @return the mapped invalid-arguments exception
     */
    private InvalidArgumentsException mapLegAlreadyWonException() {
        return new InvalidArgumentsException(
                new TargetError(
                        ErrorTargets.SCORE,
                        messageResolver.getMessage(MessageKeys.MESSAGE_LEG_ALREADY_WON)
                )
        );
    }

    /**
     * Maps an insufficient checkout dart count to the corresponding request-field error.
     *
     * @param exception the checkout validation exception
     * @return the mapped invalid-arguments exception
     */
    private InvalidArgumentsException mapCheckoutInsufficientDartsException(X01CheckoutInsufficientDartsException exception) {
        return new InvalidArgumentsException(
                new TargetError(
                        ErrorTargets.CHECKOUT_DARTS_USED,
                        messageResolver.getMessage(
                                MessageKeys.MESSAGE_IMPOSSIBLE_CHECKOUT_MIN_DARTS,
                                exception.getScore(),
                                exception.getDartsUsed()
                        )
                )
        );
    }
}