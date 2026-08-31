package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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

    @JsonIgnore
    public X01ClearByTwoRule getClearByTwoLegsRuleForSet(int setNumber) {
        int bestOfSets = getSets();
        boolean isBestOfSetsReached = setNumber >= bestOfSets;

        return isBestOfSetsReached
                ? getClearByTwoLegsInFinalSetRule()
                : getClearByTwoLegsRule();
    }
}
