package nl.kmartin.dartsmatcherapi.features.x01.x01match.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchSettings;

import java.util.List;

/**
 * Request payload for creating a new X01 match.
 *
 * @param matchSettings the match settings
 * @param players       the players participating in the match
 */
public record X01CreateMatchRequest(
        @NotNull @Valid X01MatchSettings matchSettings,
        @NotEmpty List<@Valid Player> players
) {

    /**
     * A player supplied when creating a match.
     *
     * @param playerName         the player's display name
     * @param playerType         the player type
     * @param x01DartBotSettings the Dart Bot settings when applicable
     */
    public record Player(
            @NotBlank String playerName,
            @NotNull PlayerType playerType,
            @Valid X01DartBotSettings x01DartBotSettings
    ) {
    }
}
