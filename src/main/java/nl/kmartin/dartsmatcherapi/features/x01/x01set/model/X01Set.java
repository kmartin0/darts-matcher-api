package nl.kmartin.dartsmatcherapi.features.x01.x01set.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.basematch.model.ResultType;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Represents a set within an X01 match.
 *
 * Stores the ordered legs, the player that throws first and the player results once the set is concluded.
 */
@Getter
@Setter
@NoArgsConstructor
public class X01Set {
    @Valid
    private NavigableMap<Integer, X01Leg> legs = new TreeMap<>();

    private ObjectId throwsFirst;

    private Map<ObjectId, ResultType> result;

    public X01Set(NavigableMap<Integer, X01Leg> legs, ObjectId throwsFirst, Map<ObjectId, ResultType> result) {
        this.setLegs(legs);
        this.throwsFirst = throwsFirst;
        this.result = result;
    }

    @JsonIgnore
    public @Valid NavigableMap<Integer, X01Leg> getLegs() {
        return legs;
    }

    /**
     * Sets the legs, replacing a null value with an empty ordered map.
     *
     * @param legs the numbered legs
     */
    public void setLegs(@Valid NavigableMap<Integer, X01Leg> legs) {
        this.legs = legs != null ? legs : new TreeMap<>();
    }

    /**
     * Returns the legs as an ordered list for JSON serialization.
     *
     * @return the numbered leg entries
     */
    @JsonProperty("legs")
    public List<X01LegEntry> getLegEntries() {
        return legs.entrySet()
                .stream()
                .map(X01LegEntry::new)
                .toList();
    }

    /**
     * Restores the ordered legs from their JSON representation.
     *
     * @param entries the numbered leg entries
     */
    @JsonProperty("legs")
    public void setLegEntries(List<X01LegEntry> entries) {
        if (entries == null) {
            this.legs = new TreeMap<>();
            return;
        }

        this.legs = entries.stream()
                .collect(Collectors.toMap(
                        X01LegEntry::legNumber,
                        X01LegEntry::leg,
                        (oldValue, newValue) -> newValue,
                        TreeMap::new
                ));
    }
}