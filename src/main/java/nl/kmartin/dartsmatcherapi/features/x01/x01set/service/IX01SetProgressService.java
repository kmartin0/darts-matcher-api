package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01LegEntry;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01BestOf;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01Set;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IX01SetProgressService {
    Optional<X01LegEntry> getLeg(X01Set set, int legNumber, boolean throwIfNotFound);

    Optional<X01LegEntry> getCurrentLeg(X01Set set);

    Optional<X01LegEntry> createNextLeg(X01SetEntry setEntry, List<X01MatchPlayer> players, X01BestOf bestOf);

    boolean isSetConcluded(X01Set set);

    boolean removeLastScoreFromSet(X01Set set);
}
