package nl.kmartin.dartsmatcherapi.features.x01.x01match.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@NoArgsConstructor
@AllArgsConstructor
public class X01DartBotSettings {

    public static final int MINIMUM_BOT_AVG = 3;
    public static final int MAXIMUM_BOT_AVG = 167;

    @Min(MINIMUM_BOT_AVG)
    @Max(MAXIMUM_BOT_AVG)
    private int threeDartAverage;

    /**
     * Returns the configured three-dart average as a one-dart average.
     *
     * @return the equivalent one-dart average
     */
    @JsonIgnore
    public double getOneDartAverage() {
        return threeDartAverage / 3.0;
    }
}