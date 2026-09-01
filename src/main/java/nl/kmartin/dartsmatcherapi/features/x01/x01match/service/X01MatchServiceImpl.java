package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01dartbot.service.IX01DartBotService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.service.IX01LegService;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.message.X01MatchMessageType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01EditTurn;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;
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

    /**
     * Creates and initializes a new X01 match.
     *
     * @param match the match to create
     * @return the created match
     */
    @Override
    @Transactional
    public X01Match createMatch(@NotNull @Valid X01Match match) {
        // Initialize server-managed state before the match is first processed.
        matchSetupService.setupMatch(match);

        saveMatchAndProcessBotTurns(match, X01MatchMessageType.PROCESS_MATCH);
        return match;
    }

    /**
     * Gets an X01 match by id.
     *
     * @param matchId the match id
     * @return the requested match
     * @throws ResourceNotFoundException when the match does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public X01Match getMatch(@NotNull ObjectId matchId) throws ResourceNotFoundException {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException(X01Match.class, matchId));
    }

    /**
     * Gets existing X01 matches for the supplied ids while preserving the requested order.
     *
     * Missing matches are omitted from the result.
     *
     * @param matchIds the match ids
     * @return the existing matches in requested order
     */
    @Override
    @Transactional(readOnly = true)
    public List<X01Match> getMatches(@NotNull List<ObjectId> matchIds) {
        Map<ObjectId, X01Match> matchMap = matchRepository.findAllById(matchIds).stream()
                .collect(Collectors.toMap(X01Match::getId, Function.identity()));

        return matchIds.stream()
                .map(matchMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Verifies that an X01 match exists.
     *
     * @param matchId the match id
     * @throws ResourceNotFoundException when the match does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public void checkMatchExists(ObjectId matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException(X01Match.class, matchId);
        }
    }

    /**
     * Adds a turn for the current thrower and processes the resulting match state.
     *
     * @param matchId the match id
     * @param turn    the turn to add
     * @return the updated match
     */
    @Override
    @Transactional
    public X01Match addTurn(@NotNull ObjectId matchId, @NotNull @Valid X01Turn turn) {
        X01Match match = getMatch(matchId);

        // Apply the submitted turn to the currently active round and thrower.
        addTurnToCurrentPlayer(match, turn);

        saveMatchAndProcessBotTurns(match, X01MatchMessageType.ADD_HUMAN_TURN);
        return match;
    }

    /**
     * Replaces an existing turn and reprocesses the resulting match state.
     *
     * @param matchId  the match id
     * @param editTurn the edited turn and its match position
     * @return the updated match
     */
    @Override
    @Transactional
    public X01Match editTurn(@NotNull ObjectId matchId, @NotNull @Valid X01EditTurn editTurn) {
        X01Match match = getMatch(matchId);

        // Resolve the leg containing the turn being edited.
        X01SetEntry setEntry = matchProgressService.getSetOrThrow(match, editTurn.getSet());
        X01LegEntry legEntry = setProgressService.getLegOrThrow(setEntry.set(), editTurn.getLeg());

        // Replace the turn and let the leg service rebuild state affected by the edit.

        int x01 = match.getMatchSettings().getX01();
        boolean trackDoubles = match.getMatchSettings().isTrackDoubles();

        legService.applyTurn(
                x01,
                legEntry.leg(),
                editTurn.getRound(),
                editTurn,
                editTurn.getPlayerId(),
                trackDoubles
        );

        saveMatchAndProcessBotTurns(match, X01MatchMessageType.EDIT_TURN);
        return match;
    }

    /**
     * Deletes the last recorded turn and reprocesses the match.
     *
     * @param matchId the match id
     * @return the updated match
     */
    @Override
    @Transactional
    public X01Match deleteLastTurn(@NotNull ObjectId matchId) {
        X01Match match = getMatch(matchId);

        // Remove the latest score and any trailing empty match structure.
        matchProgressService.removeLastScoreFromMatch(match);

        saveMatchAndProcessBotTurns(match, X01MatchMessageType.DELETE_LAST_TURN);
        return match;
    }

    /**
     * Deletes an X01 match and broadcasts its removal.
     *
     * @param matchId the match id
     */
    @Override
    @Transactional
    public void deleteMatch(ObjectId matchId) {
        checkMatchExists(matchId);

        matchRepository.deleteById(matchId);
        broadcastMatchEvent(matchId, X01MatchMessageType.DELETE_MATCH, matchId);
    }

    /**
     * Resets an X01 match to its initial state and reprocesses it.
     *
     * @param matchId the match id
     * @return the reset match
     */
    @Override
    @Transactional
    public X01Match resetMatch(ObjectId matchId) {
        X01Match match = getMatch(matchId);

        // Reinitialize server-managed state while preserving the existing match identity.
        matchSetupService.setupMatch(match);

        saveMatchAndProcessBotTurns(match, X01MatchMessageType.RESET_MATCH);
        return match;
    }

    /**
     * Reprocesses the derived state of an existing X01 match.
     *
     * @param matchId the match id
     * @return the reprocessed match
     */
    @Override
    @Transactional
    public X01Match reprocessMatch(ObjectId matchId) {
        X01Match match = getMatch(matchId);

        // Rebuild calculated state from the recorded match history before persisting it again.
        saveMatchAndProcessBotTurns(match, X01MatchMessageType.PROCESS_MATCH);

        return match;
    }

    /**
     * Applies a turn to the current thrower in the active round.
     *
     * @param match the match to update
     * @param turn  the turn to apply
     */
    private void addTurnToCurrentPlayer(@NotNull X01Match match, @NotNull @Valid X01Turn turn) {
        // Resolve or create the active set, leg and round.
        X01SetEntry currentSetEntry = matchProgressService.getCurrentSetOrCreate(match)
                .orElseThrow(() -> new ResourceNotFoundException(X01Set.class, null));

        X01LegEntry currentLegEntry = matchProgressService.getCurrentLegOrCreate(match, currentSetEntry)
                .orElseThrow(() -> new ResourceNotFoundException(X01Leg.class, null));

        X01LegRoundEntry currentRoundEntry =
                matchProgressService.getCurrentLegRoundOrCreate(match, currentLegEntry.leg())
                        .orElseThrow(() -> new ResourceNotFoundException(X01LegRound.class, null));

        // Determine the current thrower from the scores already recorded in the active round.
        ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(
                currentRoundEntry.round(),
                currentLegEntry.leg().getThrowsFirst(),
                match.getPlayers()
        );

        int x01 = match.getMatchSettings().getX01();
        boolean trackDoubles = match.getMatchSettings().isTrackDoubles();

        legService.applyTurn(
                x01,
                currentLegEntry.leg(),
                currentRoundEntry.roundNumber(),
                turn,
                currentThrower,
                trackDoubles
        );
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

            X01Turn dartBotTurn = dartBotService.createDartBotTurn(match);
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
        if (match == null || match.getMatchProgress().getCurrentThrower() == null) return false;

        return getPlayerById(match, match.getMatchProgress().getCurrentThrower())
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
        if (playerId == null) return Optional.empty();

        return match.getPlayers().stream()
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