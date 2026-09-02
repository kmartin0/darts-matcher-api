package nl.kmartin.dartsmatcherapi.features.x01.x01leg.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.kmartin.dartsmatcherapi.features.x01.x01leg.model.X01Leg;
import org.bson.types.ObjectId;

public interface IX01LegResultService {
    void updateLegResult(@NotNull @Valid X01Leg leg, @Positive int x01);

    int getRemainingForPlayer(@NotNull @Valid X01Leg leg, @NotNull ObjectId playerId, @Positive int x01);

    void updateRemainingForPlayer(@NotNull @Valid X01Leg leg, @NotNull ObjectId playerId, @Positive int x01);

    int calculateDartsUsed(@NotNull @Valid X01Leg leg, @NotNull ObjectId playerId);
}