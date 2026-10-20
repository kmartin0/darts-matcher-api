package nl.kmartin.dartsmatcher.api.features.x01.x01match.service;

import nl.kmartin.dartsmatcher.api.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01BestOfType;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchFormat;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcher.api.features.x01.x01match.model.X01MatchProgress;
import nl.kmartin.dartsmatcher.api.features.x01.x01statistics.model.X01Statistics;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Creates new X01 matches and rebuilds existing matches into their starting state.
 */
@Service
@Validated
public class X01MatchSetupServiceImpl implements IX01MatchSetupService {

    @Override
    public X01Match initializeNewMatch(X01CreateMatchRequest request) {
        // Create fresh match players from the requested player configuration.
        ArrayList<X01MatchPlayer> players = createMatchPlayers(request.players(), request.matchSettings().getFormat());

        // Normalize best of sets to 1, when best of type is legs.
        X01BestOf bestOfSettings = request.matchSettings().getBestOf();
        if (bestOfSettings.getBestOfType() == X01BestOfType.LEGS) {
            bestOfSettings.setSets(1);
            bestOfSettings.getClearByTwoSetsRule().setEnabled(false);
        }

        // Initialize the match without played sets or derived match state.
        return new X01Match(
                null,
                null,
                0,
                Instant.now(),
                null,
                MatchStatus.IN_PLAY,
                players,
                null,
                request.matchSettings(),
                new TreeMap<>(),
                new X01MatchProgress(),
                new LinkedHashMap<>()
        );
    }

    @Override
    public X01Match initializeRematch(X01Match match) {
        // Create fresh player state while preserving player identity, configuration and order.
        ArrayList<X01MatchPlayer> players = createResetMatchPlayers(match.getPlayers(), match.getMatchSettings().getFormat());

        // Initialize a new match without played sets, derived state or a rematch reference.
        return new X01Match(
                null,
                null,
                0,
                Instant.now(),
                null,
                MatchStatus.IN_PLAY,
                players,
                null,
                match.getMatchSettings(),
                new TreeMap<>(),
                new X01MatchProgress(),
                new LinkedHashMap<>()
        );
    }

    @Override
    public X01Match resetMatch(X01Match match) {
        // Reset player state while preserving player identity and configuration.
        ArrayList<X01MatchPlayer> players = createResetMatchPlayers(match.getPlayers(), match.getMatchSettings().getFormat());

        // Recreate the starting match state while preserving persisted identity and configuration.
        return new X01Match(
                match.getId(),
                match.getVersion(),
                match.getBroadcastVersion(),
                Instant.now(),
                null,
                MatchStatus.IN_PLAY,
                players,
                match.getRematchId(),
                match.getMatchSettings(),
                new TreeMap<>(),
                new X01MatchProgress(),
                new LinkedHashMap<>()
        );
    }

    /**
     * Maps requested players to newly initialized X01 match players.
     *
     * @param players     the requested players
     * @param matchFormat the match format
     * @return the initialized match players
     */
    private ArrayList<X01MatchPlayer> createMatchPlayers(List<X01CreateMatchRequest.Player> players, X01MatchFormat matchFormat) {
        return players.stream()
                .map(player -> new X01MatchPlayer(
                        new ObjectId(),
                        player.playerName(),
                        player.playerType(),
                        null,
                        player.x01DartBotSettings(),
                        new X01Statistics(matchFormat)
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Creates fresh player state while preserving player identity, configuration and order.
     *
     * @param players     the existing match players
     * @param matchFormat the match format
     * @return the players with reset state
     */
    private ArrayList<X01MatchPlayer> createResetMatchPlayers(List<X01MatchPlayer> players, X01MatchFormat matchFormat) {
        return players.stream()
                .map(player -> new X01MatchPlayer(
                        player.getPlayerId(),
                        player.getPlayerName(),
                        player.getPlayerType(),
                        null,
                        player.getX01DartBotSettings(),
                        new X01Statistics(matchFormat)
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}