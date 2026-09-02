package nl.kmartin.dartsmatcherapi.features.x01.x01standings.model;

import jakarta.validation.constraints.PositiveOrZero;
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
@NoArgsConstructor
@AllArgsConstructor
public class X01StandingsEntry {
    @PositiveOrZero
    private int setsWon;

    @PositiveOrZero
    private int legsWonInCurrentSet;
}