package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

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
public class X01MatchSettings {
    public static final int MINIMUM_X01 = 101;
    public static final int MAXIMUM_X01 = 1001;

    @Min(MINIMUM_X01)
    @Max(MAXIMUM_X01)
    private int x01;

    private boolean trackDoubles;

    @NotNull
    @Valid
    private X01BestOf bestOf;
}
