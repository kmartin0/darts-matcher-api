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
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01TurnMutation;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.service.IX01LegRoundService;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.service.IX01SetProgressService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * Applies turn mutations to loaded X01 matches.
 *
 * Resolves the required match position and delegates turn processing to the leg service.
 */
@Service
@Validated
public class X01MatchTurnServiceImpl implements IX01MatchTurnService {

    private final IX01MatchProgressService matchProgressService;
    private final IX01SetProgressService setProgressService;
    private final IX01LegService legService;
    private final IX01LegRoundService legRoundService;
    private final IX01DartBotService dartBotService;

    public X01MatchTurnServiceImpl(
            IX01MatchProgressService matchProgressService,
            IX01SetProgressService setProgressService,
            IX01LegService legService,
            IX01LegRoundService legRoundService,
            IX01DartBotService dartBotService
    ) {
        this.matchProgressService = matchProgressService;
        this.setProgressService = setProgressService;
        this.legService = legService;
        this.legRoundService = legRoundService;
        this.dartBotService = dartBotService;
    }

    @Override
    public void addTurnToCurrentThrower(X01Match match, X01CreateTurnRequest turnRequest) {
        legService.applyTurn(createAddTurnMutation(match, turnRequest));
    }

    @Override
    public boolean addDartBotTurnToCurrentThrower(X01Match match) {
        Optional<X01DartBotTurn> dartBotTurn = createDartBotTurnForCurrentThrower(match);
        if (dartBotTurn.isEmpty()) {
            return false;
        }

        X01DartBotTurn turn = dartBotTurn.get();
        addTurnToCurrentThrower(match, new X01CreateTurnRequest(
                turn.score(),
                turn.doublesMissed(),
                turn.checkoutDartsUsed()
        ));

        return true;
    }

    @Override
    public void replaceTurn(X01Match match, X01EditTurnRequest turnRequest) {
        legService.replaceTurn(createReplaceTurnMutation(match, turnRequest));
    }

    @Override
    public boolean deleteLastHumanTurn(X01Match match) {
        // Leave the recorded turns unchanged when there is no human turn to undo.
        if (!hasHumanTurn(match)) {
            return false;
        }

        boolean turnRemoved = false;

        // Delete trailing Dart Bot turns and then the preceding human turn.
        // Stop early if no turn remains or the removed turn's player cannot be found.
        while (true) {
            Optional<X01TurnEntry> removedTurn = matchProgressService.removeLastTurnFromMatch(match);
            if (removedTurn.isEmpty()) break;

            turnRemoved = true;

            Optional<X01MatchPlayer> removedTurnPlayer = match.getPlayerById(removedTurn.get().playerId());
            if (removedTurnPlayer.isEmpty()) break;

            if (removedTurnPlayer.get().getPlayerType() != PlayerType.DART_BOT) break;
        }

        return turnRemoved;
    }

    /**
     * Creates a turn mutation for the current thrower, creating the active set, leg and round when needed.
     *
     * @param match       the match to add a turn to
     * @param turnRequest the turn values to apply
     * @return the mutation targeting the current thrower in the active round
     * @throws ResourceNotFoundException when the active set, leg or round cannot be resolved or created
     */
    private X01TurnMutation createAddTurnMutation(X01Match match, X01CreateTurnRequest turnRequest) {
        X01SetEntry currentSetEntry = matchProgressService.getCurrentSetOrCreate(match)
                .orElseThrow(() -> new ResourceNotFoundException(X01Set.class, null));

        X01LegEntry currentLegEntry = matchProgressService.getCurrentLegOrCreate(match, currentSetEntry)
                .orElseThrow(() -> new ResourceNotFoundException(X01Leg.class, null));

        X01LegRoundEntry currentRoundEntry = matchProgressService.getCurrentLegRoundOrCreate(
                match, currentLegEntry.leg()
        ).orElseThrow(() -> new ResourceNotFoundException(X01LegRound.class, null));

        ObjectId currentThrower = legRoundService.getCurrentThrowerInRound(
                currentRoundEntry.round(),
                currentLegEntry.leg().getThrowsFirst(),
                match.getPlayers()
        );

        return new X01TurnMutation(
                match.getMatchSettings().getX01(),
                currentLegEntry.leg(),
                currentRoundEntry.roundNumber(),
                turnRequest.getScore(),
                turnRequest.getDoublesMissed(),
                turnRequest.getCheckoutDartsUsed(),
                currentThrower,
                match.getMatchSettings().isTrackDoubles()
        );
    }

    /**
     * Generates a turn when the current thrower is a Dart Bot.
     *
     * @param match the match containing the current thrower and active leg
     * @return the generated turn, or empty when there is no current thrower or the thrower is not a Dart Bot
     * @throws IllegalStateException when the current Dart Bot's active leg cannot be resolved
     */
    private Optional<X01DartBotTurn> createDartBotTurnForCurrentThrower(X01Match match) {
        return matchProgressService.getCurrentThrower(match)
                .filter(player -> player.getPlayerType() == PlayerType.DART_BOT)
                .map(dartBotPlayer -> {
                    X01LegEntry currentLegEntry = matchProgressService.getCurrentLeg(match)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Unable to resolve the current leg for Dart Bot turn creation"
                            ));

                    return dartBotService.createDartBotTurn(
                            dartBotPlayer,
                            currentLegEntry.leg(),
                            match.getMatchSettings().getX01(),
                            match.getMatchSettings().isTrackDoubles()
                    );
                });
    }

    /**
     * Creates a turn mutation targeting the player and match position specified in the edit request.
     *
     * @param match       the match containing the turn
     * @param turnRequest the target position, player and replacement turn values
     * @return the mutation targeting the turn to replace
     * @throws ResourceNotFoundException when the target set or leg cannot be found
     */
    private X01TurnMutation createReplaceTurnMutation(X01Match match, X01EditTurnRequest turnRequest) {
        X01SetEntry setEntry = matchProgressService.getSetOrThrow(match, turnRequest.getSet());
        X01LegEntry legEntry = setProgressService.getLegOrThrow(setEntry.set(), turnRequest.getLeg());

        return new X01TurnMutation(
                match.getMatchSettings().getX01(),
                legEntry.leg(),
                turnRequest.getRound(),
                turnRequest.getScore(),
                turnRequest.getDoublesMissed(),
                turnRequest.getCheckoutDartsUsed(),
                turnRequest.getPlayerId(),
                match.getMatchSettings().isTrackDoubles()
        );
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
                .anyMatch(playerId -> match.getPlayerById(playerId)
                        .map(player -> player.getPlayerType() == PlayerType.HUMAN)
                        .orElse(false)
                );
    }
}