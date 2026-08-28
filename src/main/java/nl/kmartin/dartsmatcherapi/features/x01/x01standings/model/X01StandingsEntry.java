package nl.kmartin.dartsmatcherapi.features.x01.x01standings.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class X01StandingsEntry {
    private int setsWon;
    private int legsWonInCurrentSet;
}
