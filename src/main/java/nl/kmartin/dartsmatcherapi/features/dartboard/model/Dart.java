package nl.kmartin.dartsmatcherapi.features.dartboard.model;

/**
 * Represents a dartboard position using a numbered section and scoring area.
 *
 * @param section the dartboard section
 * @param area the scoring area
 */
public record Dart(DartboardSection section, DartboardSectionArea area) {

    /**
     * Calculates the score represented by this dart position.
     *
     * @return the dart score
     */
    public int getScore() {
        return section.getScore(area);
    }
}