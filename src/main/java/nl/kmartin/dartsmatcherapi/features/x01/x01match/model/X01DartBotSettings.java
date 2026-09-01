package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Defines the playing strength used for an X01 dart bot.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class X01DartBotSettings {

    public static final int MINIMUM_BOT_AVG = 3;
    public static final int MAXIMUM_BOT_AVG = 167;

    @Min(MINIMUM_BOT_AVG)
    @Max(MAXIMUM_BOT_AVG)
    private int threeDartAverage;
}