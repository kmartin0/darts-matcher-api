package nl.kmartin.dartsmatcherapi.features.dartboard.model;

/**
 * Represents the radial dimensions of a dartboard scoring area.
 *
 * @param sectionArea the scoring area
 * @param inner       the inner radius in millimeters
 * @param outer       the outer radius in millimeters
 */
public record DartboardSectionAreaDimen(
        DartboardSectionArea sectionArea,
        int inner,
        int outer
) {
}