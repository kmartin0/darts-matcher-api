package nl.kmartin.dartsmatcherapi.features.x01.x01leground.model;

public class X01TurnAlreadyExistsException extends RuntimeException {

    public X01TurnAlreadyExistsException() {
        super("A turn already exists for this player in the round.");
    }
}
