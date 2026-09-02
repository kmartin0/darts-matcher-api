package nl.kmartin.dartsmatcherapi.features.x01.x01match.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nl.kmartin.dartsmatcherapi.features.basematch.model.BaseMatch;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01DartBotSettings;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchSettings;
import nl.kmartin.dartsmatcherapi.validators.noduplicatematchplayername.NoDuplicateMatchPlayerName;
import nl.kmartin.dartsmatcherapi.validators.validplayercomposition.ValidPlayerComposition;
import nl.kmartin.dartsmatcherapi.validators.validx01dartbotsettings.ValidX01DartBotSettings;

import java.util.List;

/**
 * Request payload for creating a new X01 match.
 *
 * @param matchSettings the match settings
 * @param players       the players participating in the match
 */
public record X01CreateMatchRequest(
        @NotNull
        @Valid
        X01MatchSettings matchSettings,

        @NotNull
        @Size(min = BaseMatch.MINIMUM_PLAYERS, max = BaseMatch.MAXIMUM_PLAYERS)
        @NoDuplicateMatchPlayerName
        @ValidPlayerComposition
        List<@NotNull @Valid Player> players
) {

    /**
     * A player supplied when creating a match.
     *
     * @param playerName         the player's display name
     * @param playerType         the player type
     * @param x01DartBotSettings the Dart Bot settings when applicable
     */
    @ValidX01DartBotSettings
    public record Player(
            @NotBlank
            @Size(min = MatchPlayer.MINIMUM_PLAYER_NAME_LENGTH, max = MatchPlayer.MAXIMUM_PLAYER_NAME_LENGTH)
            String playerName,

            @NotNull
            PlayerType playerType,

            @Valid
            X01DartBotSettings x01DartBotSettings
    ) {
    }
}
