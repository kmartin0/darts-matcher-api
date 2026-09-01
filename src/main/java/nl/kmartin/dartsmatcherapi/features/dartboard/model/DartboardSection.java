package nl.kmartin.dartsmatcherapi.features.dartboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Defines the numbered sections of a dartboard, including bull and miss.
 */
@Getter
@AllArgsConstructor
public enum DartboardSection {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    ELEVEN(11),
    TWELVE(12),
    THIRTEEN(13),
    FOURTEEN(14),
    FIFTEEN(15),
    SIXTEEN(16),
    SEVENTEEN(17),
    EIGHTEEN(18),
    NINETEEN(19),
    TWENTY(20),
    BULL(25),
    MISS(0);

    private final int sectionNumber;

    /**
     * Calculates the score for this section in the given scoring area.
     *
     * @param area the scoring area
     * @return the resulting dart score
     */
    public int getScore(DartboardSectionArea area) {
        return sectionNumber * area.getMultiplier();
    }
}