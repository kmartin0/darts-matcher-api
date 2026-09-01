package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Defines the set and leg format of an X01 match, including clear-by-two rules.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class X01BestOf {

    public static final int MINIMUM_BEST_OF = 1;
    public static final int MAXIMUM_BEST_OF = 49;

    @Min(MINIMUM_BEST_OF)
    @Max(MAXIMUM_BEST_OF)
    private int sets;

    @Min(MINIMUM_BEST_OF)
    @Max(MAXIMUM_BEST_OF)
    private int legs;

    @NotNull
    private X01BestOfType bestOfType;

    @Valid
    @NotNull
    private X01ClearByTwoRule clearByTwoSetsRule;

    @Valid
    @NotNull
    private X01ClearByTwoRule clearByTwoLegsRule;

    @Valid
    @NotNull
    private X01ClearByTwoRule clearByTwoLegsInFinalSetRule;

    /**
     * Gets the clear-by-two leg rule that applies to a set.
     *
     * @param setNumber the set number
     * @return the final-set rule when the final set is reached, otherwise the regular leg rule
     */
    @JsonIgnore
    public X01ClearByTwoRule getClearByTwoLegsRuleForSet(int setNumber) {
        int bestOfSets = getSets();
        boolean isBestOfSetsReached = setNumber >= bestOfSets;

        return isBestOfSetsReached
                ? getClearByTwoLegsInFinalSetRule()
                : getClearByTwoLegsRule();
    }
}