package nl.kmartin.dartsmatcherapi.features.basematch.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.kmartin.dartsmatcherapi.validator.noduplicatematchplayername.NoDuplicateMatchPlayerName;
import nl.kmartin.dartsmatcherapi.validator.validplayercomposition.ValidPlayerComposition;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Base model containing the state shared by all match types.
 *
 * Stores match identity and versioning, lifecycle dates and status, participating players
 * and the version used for WebSocket broadcasts.
 *
 * @param <P> the match player type
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public abstract class BaseMatch<P extends MatchPlayer> {
    public static final int MINIMUM_PLAYERS = 1;
    public static final int MAXIMUM_PLAYERS = 4;

    @MongoId
    private ObjectId id;

    @Version
    private Integer version;

    @PositiveOrZero
    private int broadcastVersion;

    @NotNull
    private Instant startDate;

    private Instant endDate;

    @NotNull
    private MatchStatus matchStatus;

    private ObjectId rematchId;

    @NotNull
    @Size(min = MINIMUM_PLAYERS, max = MAXIMUM_PLAYERS)
    @NoDuplicateMatchPlayerName
    @ValidPlayerComposition
    private ArrayList<@Valid P> players = new ArrayList<>();

    @NotNull
    @Setter(AccessLevel.PROTECTED)
    private MatchType matchType;

    protected BaseMatch(
            ObjectId id,
            Integer version,
            int broadcastVersion,
            Instant startDate,
            Instant endDate,
            MatchStatus matchStatus,
            ArrayList<P> players,
            MatchType matchType,
            ObjectId rematchId
    ) {
        this.id = id;
        this.version = version;
        this.broadcastVersion = broadcastVersion;
        this.startDate = startDate;
        this.endDate = endDate;
        this.matchStatus = matchStatus;
        this.players = players;
        this.matchType = matchType;
        this.rematchId = rematchId;
    }
}