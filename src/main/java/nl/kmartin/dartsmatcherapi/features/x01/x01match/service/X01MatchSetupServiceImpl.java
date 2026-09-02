package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchProgress;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.model.X01Statistics;
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
        ArrayList<X01MatchPlayer> players = createMatchPlayers(request.players());
        ObjectId throwsFirst = players.get(0).getPlayerId();

        return new X01Match(
                null,
                null,
                0,
                Instant.now(),
                null,
                MatchStatus.IN_PLAY,
                players,
                request.matchSettings(),
                new TreeMap<>(),
                new X01MatchProgress(),
                new LinkedHashMap<>()
        );
    }

    @Override
    public X01Match resetMatch(X01Match match) {
        ArrayList<X01MatchPlayer> players = resetMatchPlayers(match.getPlayers());
        ObjectId throwsFirst = players.get(0).getPlayerId();

        return new X01Match(
                match.getId(),
                match.getVersion(),
                match.getBroadcastVersion(),
                Instant.now(),
                null,
                MatchStatus.IN_PLAY,
                players,
                match.getMatchSettings(),
                new TreeMap<>(),
                new X01MatchProgress(),
                new LinkedHashMap<>()
        );
    }

    /**
     * Maps requested players to newly initialized X01 match players.
     *
     * @param players the requested players
     * @return the initialized match players
     */
    private ArrayList<X01MatchPlayer> createMatchPlayers(List<X01CreateMatchRequest.Player> players) {
        return players.stream()
                .map(player -> new X01MatchPlayer(
                        new ObjectId(),
                        player.playerName(),
                        player.playerType(),
                        null,
                        player.x01DartBotSettings(),
                        new X01Statistics()
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Recreates match players for a reset while preserving their identity and configuration.
     *
     * @param players the existing match players
     * @return the reset match players
     */
    private ArrayList<X01MatchPlayer> resetMatchPlayers(List<X01MatchPlayer> players) {
        return players.stream()
                .map(player -> new X01MatchPlayer(
                        player.getPlayerId(),
                        player.getPlayerName(),
                        player.getPlayerType(),
                        null,
                        player.getX01DartBotSettings(),
                        new X01Statistics()
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}