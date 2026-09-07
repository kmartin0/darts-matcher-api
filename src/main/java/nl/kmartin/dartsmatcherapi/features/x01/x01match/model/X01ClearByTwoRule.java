package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Defines whether a clear-by-two rule is enabled and how far it may extend play.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class X01ClearByTwoRule {

    public static final int MAXIMUM_LIMIT = 20;

    private boolean enabled;

    @PositiveOrZero
    @Max(MAXIMUM_LIMIT)
    private int limit;
}