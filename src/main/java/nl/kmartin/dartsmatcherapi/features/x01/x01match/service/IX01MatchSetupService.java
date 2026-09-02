package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;

/**
 * Defines operations for creating and resetting X01 matches to their starting state.
 */
public interface IX01MatchSetupService {

    /**
     * Creates a fully initialized X01 match from a creation request.
     *
     * @param request the match creation request
     * @return the initialized match
     */
    X01Match initializeNewMatch(@NotNull @Valid X01CreateMatchRequest request);

    /**
     * Creates a reset version of an existing X01 match while preserving its identity and configuration.
     *
     * @param match the match to reset
     * @return the reset match
     */
    X01Match resetMatch(@NotNull @Valid X01Match match);
}