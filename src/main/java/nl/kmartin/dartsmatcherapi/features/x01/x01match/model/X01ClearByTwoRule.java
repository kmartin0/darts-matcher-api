package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01ClearByTwoRule {
    public static final int MINIMUM_LIMIT = 0;
    public static final int MAXIMUM_LIMIT = 20;

    private boolean enabled;

    @Min(MINIMUM_LIMIT)
    @Max(MAXIMUM_LIMIT)
    private int limit;
}
