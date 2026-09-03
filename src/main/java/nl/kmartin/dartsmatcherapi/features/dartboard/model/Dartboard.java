package nl.kmartin.dartsmatcherapi.features.dartboard.model;

import java.util.List;

/**
 * Defines the fixed dimensions, geometry and section order of a standard dartboard.
 */
public final class Dartboard {
    public static final int NUMBER_OF_SECTIONS = 20;

    public static final double FULL_ROTATION_RADIANS = Math.PI * 2;
    public static final double SECTION_ANGLE_RADIANS = FULL_ROTATION_RADIANS / NUMBER_OF_SECTIONS;
    public static final double HALF_SECTION_ANGLE_RADIANS = SECTION_ANGLE_RADIANS / 2;

    public static final int DOUBLE_BULL_INNER_RADIUS_MM = 0;
    public static final int DOUBLE_BULL_OUTER_RADIUS_MM = 7;
    public static final int SINGLE_BULL_OUTER_RADIUS_MM = 17;
    public static final int TRIPLE_INNER_RADIUS_MM = 97;
    public static final int TRIPLE_OUTER_RADIUS_MM = 107;
    public static final int DOUBLE_INNER_RADIUS_MM = 160;
    public static final int DOUBLE_OUTER_RADIUS_MM = 170;

    private Dartboard() {
    }

    public static final List<DartboardSection> SECTIONS = List.of(
            DartboardSection.SIX,
            DartboardSection.THIRTEEN,
            DartboardSection.FOUR,
            DartboardSection.EIGHTEEN,
            DartboardSection.ONE,
            DartboardSection.TWENTY,
            DartboardSection.FIVE,
            DartboardSection.TWELVE,
            DartboardSection.NINE,
            DartboardSection.FOURTEEN,
            DartboardSection.ELEVEN,
            DartboardSection.EIGHT,
            DartboardSection.SIXTEEN,
            DartboardSection.SEVEN,
            DartboardSection.NINETEEN,
            DartboardSection.THREE,
            DartboardSection.SEVENTEEN,
            DartboardSection.TWO,
            DartboardSection.FIFTEEN,
            DartboardSection.TEN,
            DartboardSection.SIX
    );

    public static final List<DartboardSectionAreaDimen> AREA_DIMENSIONS = List.of(
            new DartboardSectionAreaDimen(DartboardSectionArea.DOUBLE_BULL, DOUBLE_BULL_INNER_RADIUS_MM, DOUBLE_BULL_OUTER_RADIUS_MM),
            new DartboardSectionAreaDimen(DartboardSectionArea.SINGLE_BULL, DOUBLE_BULL_OUTER_RADIUS_MM, SINGLE_BULL_OUTER_RADIUS_MM),
            new DartboardSectionAreaDimen(DartboardSectionArea.INNER_SINGLE, SINGLE_BULL_OUTER_RADIUS_MM, TRIPLE_INNER_RADIUS_MM),
            new DartboardSectionAreaDimen(DartboardSectionArea.TRIPLE, TRIPLE_INNER_RADIUS_MM, TRIPLE_OUTER_RADIUS_MM),
            new DartboardSectionAreaDimen(DartboardSectionArea.OUTER_SINGLE, TRIPLE_OUTER_RADIUS_MM, DOUBLE_INNER_RADIUS_MM),
            new DartboardSectionAreaDimen(DartboardSectionArea.DOUBLE, DOUBLE_INNER_RADIUS_MM, DOUBLE_OUTER_RADIUS_MM),
            new DartboardSectionAreaDimen(DartboardSectionArea.MISS, DOUBLE_OUTER_RADIUS_MM, Integer.MAX_VALUE)
    );
}