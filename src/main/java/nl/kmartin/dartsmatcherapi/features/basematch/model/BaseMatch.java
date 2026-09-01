package nl.kmartin.dartsmatcherapi.features.basematch.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.validators.noduplicatematchplayername.NoDuplicateMatchPlayerName;
import nl.kmartin.dartsmatcherapi.validators.validplayercomposition.ValidPlayerComposition;
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
@NoArgsConstructor
public abstract class BaseMatch<P extends MatchPlayer> {
    public static final int MINIMUM_PLAYERS = 1;
    public static final int MAXIMUM_PLAYERS = 4;

    @MongoId
    private ObjectId id;

    @Version
    private Integer version;

    private int broadcastVersion;
    private Instant startDate;
    private Instant endDate;
    private MatchStatus matchStatus;

    @Valid
    @NotNull
    @Size(min = MINIMUM_PLAYERS, max = MAXIMUM_PLAYERS)
    @NoDuplicateMatchPlayerName
    @ValidPlayerComposition
    private ArrayList<P> players = new ArrayList<>();

    private MatchType matchType;

    public BaseMatch(
            ObjectId id,
            Integer version,
            int broadcastVersion,
            Instant startDate,
            Instant endDate,
            MatchStatus matchStatus,
            ArrayList<P> players,
            MatchType matchType
    ) {
        this.id = id;
        this.version = version;
        this.broadcastVersion = broadcastVersion;
        this.startDate = startDate;
        this.endDate = endDate;
        this.matchStatus = matchStatus;
        this.setPlayers(players);
        this.matchType = matchType;
    }

    /**
     * Sets the match players, replacing a null value with an empty list.
     *
     * @param players the match players
     */
    public void setPlayers(
            @Valid
            @NotNull
            @Size(min = MINIMUM_PLAYERS, max = MAXIMUM_PLAYERS)
            ArrayList<P> players
    ) {
        this.players = players != null ? players : new ArrayList<>();
    }
}