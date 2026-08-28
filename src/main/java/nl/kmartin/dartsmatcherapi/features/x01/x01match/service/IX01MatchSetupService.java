package nl.kmartin.dartsmatcherapi.features.x01.x01match.service;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;

public interface IX01MatchSetupService {
    void setupMatch(@Valid X01Match match);
}
