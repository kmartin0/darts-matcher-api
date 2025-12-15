package nl.kmartin.dartsmatcherapi.features.dartboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DartboardSectionAreaDimen {
    private DartboardSectionArea sectionArea;
    private int inner;
    private int outer;
}
