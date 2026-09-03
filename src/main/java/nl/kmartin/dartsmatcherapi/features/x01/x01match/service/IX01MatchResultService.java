package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;

public interface IX01MatchResultService {

    /**
     * Rebuilds the result-related state of a match from its set history.
     *
     * @param match the match whose result state should be rebuilt
     */
    void updateMatchResult(@NotNull @Valid X01Match match);
}