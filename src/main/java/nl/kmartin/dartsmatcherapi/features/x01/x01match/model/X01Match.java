package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@NoArgsConstructor
@Document(collection = "x01_matches")
@TypeAlias("X01Match")
public class X01Match extends BaseMatch<X01MatchPlayer> {

    @NotNull
    @Valid
    private X01MatchSettings matchSettings;

    @Valid
    private NavigableMap<Integer, X01Set> sets = new TreeMap<>();

    @Valid
    private X01MatchProgress matchProgress;

    @Valid
    private LinkedHashMap<ObjectId, X01StandingsEntry> standings = new LinkedHashMap<>();

    public X01Match(
            ObjectId id,
            Integer version,
            int broadcastVersion,
            Instant startDate,
            Instant endDate,
            MatchStatus matchStatus,
            ArrayList<X01MatchPlayer> players,
            MatchType matchType,
            X01MatchSettings matchSettings,
            NavigableMap<Integer, X01Set> sets,
            X01MatchProgress matchProgress,
            LinkedHashMap<ObjectId, X01StandingsEntry> standings
    ) {
        super(id, version, broadcastVersion, startDate, endDate, matchStatus, players, matchType);
        this.matchSettings = matchSettings;
        this.setSets(sets);
        this.matchProgress = matchProgress;
        this.setStandings(standings);
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
     * Replaces the ordered sets.
     *
     * @param sets the sets, or null to use an empty map
     */
    public void setSets(@Valid NavigableMap<Integer, X01Set> sets) {
        this.sets = sets != null ? sets : new TreeMap<>();
    }

    /**
     * Replaces the match standings.
     *
     * @param standings the standings, or null to use an empty map
     */
    public void setStandings(@Valid LinkedHashMap<ObjectId, X01StandingsEntry> standings) {
        this.standings = standings != null ? standings : new LinkedHashMap<>();
    }

    /**
     * Serializes the ordered sets as a list to preserve their chronological order.
     *
     * @return the sets as ordered entries
     */
    @JsonProperty("sets")
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
    @JsonProperty("sets")
    public void setSetEntries(List<X01SetEntry> entries) {
        if (entries == null) {
            sets = new TreeMap<>();
            return;
        }

        sets = entries.stream()
                .collect(Collectors.toMap(
                        X01SetEntry::setNumber,
                        X01SetEntry::set,
                        (oldValue, newValue) -> newValue,
                        TreeMap::new
                ));
    }
}