package nl.kmartin.dartsmatcherapi.features.x01.x01leg.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRound;
import nl.kmartin.dartsmatcherapi.features.x01.x01leground.model.X01LegRoundEntry;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Represents a leg within an X01 match.
 *
 * Stores the player that throws first, the leg winner, checkout dart usage
 * and the ordered rounds played within the leg.
 */
@Getter
@Setter
@NoArgsConstructor
public class X01Leg {
    public static final int MINIMUM_CHECKOUT_DARTS_USED = 1;
    public static final int MAXIMUM_CHECKOUT_DARTS_USED = 3;

    private ObjectId winner;

    @NotNull
    private ObjectId throwsFirst;

    @Min(MINIMUM_CHECKOUT_DARTS_USED)
    @Max(MAXIMUM_CHECKOUT_DARTS_USED)
    private Integer checkoutDartsUsed;

    @NotNull
    private NavigableMap<@NotNull @Positive Integer, @NotNull @Valid X01LegRound> rounds = new TreeMap<>();

    public X01Leg(ObjectId winner, ObjectId throwsFirst, NavigableMap<Integer, X01LegRound> rounds) {
        this.winner = winner;
        this.throwsFirst = throwsFirst;
        this.rounds = rounds;
    }

    @JsonIgnore
    public NavigableMap<Integer, X01LegRound> getRounds() {
        return rounds;
    }

    /**
     * Returns the rounds as an ordered list for JSON serialization.
     *
     * @return the ordered round entries
     */
    @JsonGetter("rounds")
    public List<X01LegRoundEntry> getRoundEntries() {
        return rounds.entrySet()
                .stream()
                .map(X01LegRoundEntry::new)
                .toList();
    }

    /**
     * Restores the ordered rounds from their JSON representation.
     *
     * @param entries the round entries
     */
    @JsonSetter(value = "rounds", nulls = Nulls.AS_EMPTY)
    public void setRoundEntries(List<X01LegRoundEntry> entries) {
        this.rounds = entries.stream()
                .collect(Collectors.toMap(
                        X01LegRoundEntry::roundNumber,
                        X01LegRoundEntry::round,
                        (oldValue, newValue) -> newValue,
                        TreeMap::new
                ));
    }
}
