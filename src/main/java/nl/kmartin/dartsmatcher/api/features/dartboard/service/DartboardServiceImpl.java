package nl.kmartin.dartsmatcher.api.features.dartboard.service;

import nl.kmartin.dartsmatcher.api.features.dartboard.model.CartesianCoordinate;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.Dart;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.Dartboard;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.DartboardSection;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.DartboardSectionArea;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.DartboardSectionAreaDimen;
import nl.kmartin.dartsmatcher.api.features.dartboard.model.PolarCoordinate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Provides geometric calculations for determining where a dart lands on the dartboard.
 *
 * Cartesian offsets in millimeters are applied to the target's aiming point,
 * with the bullseye at (0, 0). The resulting position is converted to polar
 * coordinates to determine the scoring area and numbered section.
 *
 * The radius (r) determines the scoring area. The angle is converted to the
 * range [0...2π] before identifying the numbered section.
 */
@Service
@Validated
public class DartboardServiceImpl implements IDartboardService {

    @Override
    public Dart getScore(Dart target, double offsetX, double offsetY) {
        PolarCoordinate polarTarget = getCenter(target.section(), target.area());

        // Convert the target to Cartesian coordinates and apply the offsets.
        double x = polarTarget.r() * Math.cos(polarTarget.theta()) + offsetX;
        double y = polarTarget.r() * Math.sin(polarTarget.theta()) + offsetY;

        CartesianCoordinate position = new CartesianCoordinate(x, y);
        return getScorePolar(PolarCoordinate.fromCartesian(position));
    }

    /**
     * Resolves the dart hit at the supplied polar coordinate.
     *
     * Bull hits use the bull section regardless of their angle.
     *
     * @param polarCoordinate the position of the dart
     * @return the section and scoring area hit
     */
    private Dart getScorePolar(PolarCoordinate polarCoordinate) {
        DartboardSectionArea sectionArea = getSectionArea(polarCoordinate.r());

        DartboardSection section = switch (sectionArea) {
            case SINGLE_BULL, DOUBLE_BULL -> DartboardSection.BULL;
            default -> getSection(polarCoordinate.getThetaNormalized());
        };

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
            return Dartboard.FULL_ROTATION_RADIANS;
        }

        return (sectorIndex * Dartboard.SECTION_ANGLE_RADIANS) + Dartboard.HALF_SECTION_ANGLE_RADIANS;
    }

    /**
     * Determines the scoring area for a radial distance from the center.
     *
     * @param r the distance from the center in millimeters
     * @return the scoring area, or MISS when outside the scoring areas
     */
    private DartboardSectionArea getSectionArea(double r) {
        for (DartboardSectionAreaDimen areaDimension : Dartboard.AREA_DIMENSIONS) {
            if (r >= areaDimension.inner() && r < areaDimension.outer()) {
                return areaDimension.sectionArea();
            }
        }

        return DartboardSectionArea.MISS;
    }

    /**
     * Calculates the aiming point for a dartboard target.
     *
     * Double bull uses the board center. Other areas use their radial midpoint
     * and the section's center angle. An unknown area falls back to the board center.
     *
     * @param section     the dartboard section
     * @param sectionArea the scoring area within the section
     * @return the polar coordinate of the aiming point
     */
    private PolarCoordinate getCenter(DartboardSection section, DartboardSectionArea sectionArea) {
        if (sectionArea == DartboardSectionArea.DOUBLE_BULL) {
            return new PolarCoordinate(0, 0);
        }

        return Dartboard.AREA_DIMENSIONS.stream()
                .filter(dimen -> dimen.sectionArea() == sectionArea)
                .findFirst()
                .map(dimen -> {
                    // Use the center angle of the section.
                    double sectionTheta = getSectionTheta(Dartboard.SECTIONS.indexOf(section));
                    double sectionCenterTheta = sectionTheta - Dartboard.HALF_SECTION_ANGLE_RADIANS;

                    // Use the radial midpoint of the scoring area.
                    double r = (dimen.inner() + dimen.outer()) / 2.0;

                    return new PolarCoordinate(r, sectionCenterTheta);
                })
                .orElse(new PolarCoordinate(0, 0));
    }
}
