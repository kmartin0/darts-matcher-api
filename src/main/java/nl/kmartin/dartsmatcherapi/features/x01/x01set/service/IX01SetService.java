package nl.kmartin.dartsmatcherapi.features.x01.x01set.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01MatchPlayer;
import nl.kmartin.dartsmatcherapi.features.x01.x01set.model.X01SetEntry;

import java.util.List;

public interface IX01SetService {

    X01SetEntry createNewSet(
            @Positive int setNumber,
            @NotEmpty List<@NotNull @Valid X01MatchPlayer> players
    );
}