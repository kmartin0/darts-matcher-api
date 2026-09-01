package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.basematch.model.PlayerType;
import nl.kmartin.dartsmatcherapi.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapi.features.x01.x01statistics.model.X01Statistics;
import nl.kmartin.dartsmatcherapi.validators.validx01dartbotsettings.ValidX01DartBotSettings;
import org.bson.types.ObjectId;

/**
 * Represents a player participating in an X01 match, including bot settings and X01 statistics.
 */
@Getter
@Setter
@NoArgsConstructor
@ValidX01DartBotSettings
public class X01MatchPlayer extends MatchPlayer {

    @Valid
    private X01DartBotSettings x01DartBotSettings;

    @Valid
    private X01Statistics statistics;

    public X01MatchPlayer(
            ObjectId playerId,
            String playerName,
            PlayerType playerType,
            ResultType resultType,
            X01DartBotSettings x01DartBotSettings,
            X01Statistics statistics
    ) {
        super(playerId, playerName, playerType, resultType);
        this.x01DartBotSettings = x01DartBotSettings;
        this.statistics = statistics;
    }
}