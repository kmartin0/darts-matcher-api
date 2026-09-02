package nl.kmartin.dartsmatcherapi.features.dartboard.service;

import nl.kmartin.dartsmatcherapi.features.dartboard.model.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Provides geometric calculations for determining where a dart lands on the dartboard.
 *
 * The dartboard is represented using Cartesian coordinates with the bullseye at (0, 0).
 * Coordinates are converted to polar coordinates, where:
 * - r represents the distance from the center in millimeters.
 * - theta represents the angle around the board in radians from 0 to 2π (0° to 360°).
 *
 * The radius (r) determines the scoring area and theta determines the numbered section.
 */
@Service
@Validated
public class DartboardServiceImpl implements IDartboardService {

    /**
     * Calculates where a dart lands when throwing at a target with a given deviation.
     *
     * The target starts at the center of its scoring area.
     * Setting both offsets to 0 results in a throw at the center of the requested target.
     *
     * @param target      the dartboard target
     * @param offsetR     radial deviation in millimeters
     * @param offsetTheta angular deviation in radians
     * @return the resulting dart
     */
    @Override
    public Dart getScore(Dart target, double offsetR, double offsetTheta) {
        // Create a polar coordinate from the center of the section area.
        PolarCoordinate polarTarget = getCenter(target.section(), target.area());

        // Add the deviation radial and angle to the target's polar coordinate.
        double r = polarTarget.r() + offsetR;
        double theta = PolarCoordinate.normalizeTheta(polarTarget.theta() + offsetTheta);

        // Return the Dart containing the result of the polar coordinate with deviation.
        return getScorePolar(new PolarCoordinate(r, theta));
    }

    /**
     * Determines where a Cartesian coordinate lies on the dartboard.
     *
     * @param cartesianCoordinate the Cartesian coordinate
     * @return the resulting dart
     */
    private Dart getScoreCartesian(CartesianCoordinate cartesianCoordinate) {
        return getScorePolar(PolarCoordinate.fromCartesian(cartesianCoordinate));
    }

    /**
     * Determines where a polar coordinate lies on the dartboard.
     *
     * @param polarCoordinate the polar coordinate
     * @return the resulting dart
     */
    private Dart getScorePolar(PolarCoordinate polarCoordinate) {
        DartboardSection section = getSection(polarCoordinate.getThetaNormalized());
        DartboardSectionArea sectionArea = getSectionArea(polarCoordinate.r());

        // Return the score multiplied by the section area multiplier.
        return new Dart(section, sectionArea);
    }

    /**
     * Determines the numbered dartboard section for an angle.
     * Ties on section boundaries are resolved clockwise.
     *
     * @param theta the angle around the board in radians
     * @return the dartboard section
     */
    private DartboardSection getSection(double theta) {

        for (int i = 0; i < Dartboard.SECTIONS.size(); i++) {
            if (theta <= getSectionTheta(i)) {
                return Dartboard.SECTIONS.get(i);
            }
        }

        return DartboardSection.MISS;
    }

    /**
     * Calculates the outer boundary angle of a dartboard section.
     *
     * @param sectorIndex the section index around the board
     * @return the section boundary angle in radians
     */
    private double getSectionTheta(int sectorIndex) {
        if (sectorIndex == Dartboard.NUMBER_OF_SECTIONS) {
            return Math.PI * 2;
        }

        return (sectorIndex * Dartboard.SECTION_ANGLE_RADIANS) + Dartboard.HALF_SECTION_ANGLE_RADIANS;
    }

    /**
     * Determines the scoring area for a radial distance from the center.
     *
     * @param r the distance from the center in millimeters
     * @return the scoring area
     */
    private DartboardSectionArea getSectionArea(double r) {

        for (DartboardSectionAreaDimen areaDimension : Dartboard.AREA_DIMENSIONS) {
            if (r >= areaDimension.inner() && r < areaDimension.outer())
                return areaDimension.sectionArea();
        }

        return DartboardSectionArea.MISS;
    }

    /**
     * Calculates the center coordinate of a dartboard target.
     *
     * @param section     the dartboard section
     * @param sectionArea the scoring area within the section
     * @return the polar coordinate at the center of the target
     */
    private PolarCoordinate getCenter(DartboardSection section, DartboardSectionArea sectionArea) {
        if (sectionArea == DartboardSectionArea.DOUBLE_BULL) return new PolarCoordinate(0, 0);

        return Dartboard.AREA_DIMENSIONS.stream()
                .filter(dimen -> dimen.sectionArea() == sectionArea)
                .findFirst()
                .map(dimen -> {
                    // Calculate what the center angle of a section is.
                    double sectionTheta = getSectionTheta(Dartboard.SECTIONS.indexOf(section));
                    double sectionCenterTheta = sectionTheta - Dartboard.HALF_SECTION_ANGLE_RADIANS;

                    // Calculate what the radial of the section area is.
                    double r = (dimen.inner() + dimen.outer()) / 2.0;

                    return new PolarCoordinate(r, sectionCenterTheta);
                })
                .orElse(new PolarCoordinate(0, 0));
    }
}
