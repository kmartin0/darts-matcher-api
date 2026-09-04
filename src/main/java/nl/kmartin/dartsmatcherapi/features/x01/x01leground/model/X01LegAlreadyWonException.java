package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

public class X01LegAlreadyWonException extends RuntimeException {

    public X01LegAlreadyWonException() {
        super("The leg has already been won by another player.");
    }
}
