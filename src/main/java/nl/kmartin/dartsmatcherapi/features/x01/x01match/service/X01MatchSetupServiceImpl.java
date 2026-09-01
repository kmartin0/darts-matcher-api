package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchProgress;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.model.X01Statistics;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * Initializes X01 matches to their starting state.
 */
@Service
public class X01MatchSetupServiceImpl implements IX01MatchSetupService {

    /**
     * Prepares an X01 match for play.
     *
     * @param match the match to initialize
     */
    @Override
    public void setupMatch(@Valid X01Match match) {
        if (match == null) return;

        setupMatchPlayers(match);
        setMatchTypeAndStatus(match);
        setMatchDates(match);
        setupMatchState(match);
    }

    /**
     * Initializes player identifiers, results and statistics.
     *
     * @param match the match whose players should be initialized
     */
    private void setupMatchPlayers(X01Match match) {
        match.getPlayers().forEach(player -> {
            // Preserve an existing player id, otherwise assign a new one.
            if (player.getPlayerId() == null) {
                player.setPlayerId(new ObjectId());
            }

            player.setResultType(null);
            player.setStatistics(new X01Statistics());
        });
    }

    /**
     * Initializes the match type and status.
     *
     * @param match the match to initialize
     */
    private void setMatchTypeAndStatus(X01Match match) {
        match.setMatchType(MatchType.X01);
        match.setMatchStatus(MatchStatus.IN_PLAY);
    }

    /**
     * Initializes the match dates.
     *
     * @param match the match to initialize
     */
    private void setMatchDates(X01Match match) {
        match.setStartDate(Instant.now());
        match.setEndDate(null);
    }

    /**
     * Initializes the match history, standings and current progress.
     *
     * @param match the match to initialize
     */
    private void setupMatchState(X01Match match) {
        // Clear any supplied or previously calculated match state.
        match.setSets(new TreeMap<>());
        match.setStandings(new LinkedHashMap<>());

        // Start the match at the first round with the first player throwing.
        ObjectId startsMatch = match.getPlayers().get(0).getPlayerId();
        match.setMatchProgress(new X01MatchProgress(1, 1, 1, startsMatch));
    }
}