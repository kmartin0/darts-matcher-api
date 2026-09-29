package nl.kmartin.dartsmatcherapi.features.x01.x01match.message;

import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;

import java.util.function.Function;

/**
 * Builds an {@link X01MatchMessage} carrying an {@link X01Match} payload.
 */
@FunctionalInterface
public interface X01MatchMessageFactory extends Function<X01Match, X01MatchMessage<X01Match>> {
}