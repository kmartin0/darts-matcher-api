package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.kmartin.dartsmatcherapi.features.basematch.model.BaseMatch;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchStatus;
import nl.kmartin.dartsmatcherapi.features.basematch.model.MatchType;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01standings.model.X01StandingsEntry;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Represents an X01 match, including its settings, ordered sets, progress and standings.
 */
@Getter
@Setter
@ToString(callSuper = true)
@Document(collection = "x01_matches")
@TypeAlias("X01Match")
public class X01Match extends BaseMatch<X01MatchPlayer> {

    @NotNull
    @Valid
    private X01MatchSettings matchSettings;

    @NotNull
    private NavigableMap<@NotNull @Positive Integer, @NotNull @Valid X01Set> sets = new TreeMap<>();

    @NotNull
    @Valid
    private X01MatchProgress matchProgress;

    @NotNull
    private LinkedHashMap<@NotNull ObjectId, @NotNull @Valid X01StandingsEntry> standings = new LinkedHashMap<>();

    public X01Match() {
        setMatchType(MatchType.X01);
    }

    public X01Match(
            ObjectId id,
            Integer version,
            int broadcastVersion,
            Instant startDate,
            Instant endDate,
            MatchStatus matchStatus,
            ArrayList<X01MatchPlayer> players,
            X01MatchSettings matchSettings,
            NavigableMap<Integer, X01Set> sets,
            X01MatchProgress matchProgress,
            LinkedHashMap<ObjectId, X01StandingsEntry> standings
    ) {
        super(id, version, broadcastVersion, startDate, endDate, matchStatus, players, MatchType.X01);
        this.matchSettings = matchSettings;
        this.sets = sets;
        this.matchProgress = matchProgress;
        this.standings = standings;
    }

    /**
     * Gets the sets keyed by set number in chronological order.
     *
     * @return the ordered sets
     */
    @JsonIgnore
    public @Valid NavigableMap<Integer, X01Set> getSets() {
        return sets;
    }

    /**
     * Serializes the ordered sets as a list to preserve their chronological order.
     *
     * @return the sets as ordered entries
     */
    @JsonGetter("sets")
    public List<X01SetEntry> getSetEntries() {
        return sets.entrySet().stream()
                .map(X01SetEntry::new)
                .toList();
    }

    /**
     * Deserializes set entries into a map ordered by set number.
     *
     * @param entries the serialized set entries
     */
    @JsonSetter(value = "sets", nulls = Nulls.AS_EMPTY)
    public void setSetEntries(List<X01SetEntry> entries) {
        sets = entries.stream()
                .collect(Collectors.toMap(
                        X01SetEntry::setNumber,
                        X01SetEntry::set,
                        (oldValue, newValue) -> newValue,
                        TreeMap::new
                ));
    }
}