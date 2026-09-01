package nl.kmartin.dartsmatcherapi.features.x01.x01standings.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores a player's position in the X01 match standings.
 *
 * Tracks the number of sets won and legs won within the current set.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class X01StandingsEntry {
    private int setsWon;
    private int legsWonInCurrentSet;
}