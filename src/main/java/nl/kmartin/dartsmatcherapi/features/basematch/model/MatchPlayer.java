package nl.kmartin.dartsmatcherapi.features.basematch.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;

/**
 * Represents a player participating in a match.
 *
 * Stores the player's identity, display name, player type and eventual match result.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayer {
    public static final int MINIMUM_PLAYER_NAME_LENGTH = 3;
    public static final int MAXIMUM_PLAYER_NAME_LENGTH = 30;

    private ObjectId playerId;

    @NotNull
    @Length(min = MINIMUM_PLAYER_NAME_LENGTH, max = MAXIMUM_PLAYER_NAME_LENGTH)
    private String playerName;

    @NotNull
    private PlayerType playerType;

    private ResultType resultType;
}