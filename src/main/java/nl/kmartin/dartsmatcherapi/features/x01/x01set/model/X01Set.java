package nl.kmartin.dartsmatcherapi.features.x01.x01set.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
@ToString
@NoArgsConstructor
public class X01Set {
    @NotNull
    private NavigableMap<@NotNull @Positive Integer, @NotNull @Valid X01Leg> legs = new TreeMap<>();

    @NotNull
    private ObjectId throwsFirst;

    private Map<@NotNull ObjectId, @NotNull ResultType> result;

    public X01Set(NavigableMap<Integer, X01Leg> legs, ObjectId throwsFirst, Map<ObjectId, ResultType> result) {
        this.legs = legs;
        this.throwsFirst = throwsFirst;
        this.result = result;
    }

    @JsonIgnore
    public NavigableMap<Integer, X01Leg> getLegs() {
        return legs;
    }

    /**
     * Returns the legs as an ordered list for JSON serialization.
     *
     * @return the numbered leg entries
     */
    @JsonGetter("legs")
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
    @JsonSetter(value = "legs", nulls = Nulls.AS_EMPTY)
    public void setLegEntries(List<X01LegEntry> entries) {
        this.legs = entries.stream()
                .collect(Collectors.toMap(
                        X01LegEntry::legNumber,
                        X01LegEntry::leg,
                        (oldValue, newValue) -> newValue,
                        TreeMap::new
                ));
    }
}