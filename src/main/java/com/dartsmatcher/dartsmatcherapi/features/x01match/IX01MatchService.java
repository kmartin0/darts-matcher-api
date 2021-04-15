package com.dartsmatcher.dartsmatcherapi.features.x01match;

import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.checkout.X01Checkout;
import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;

@Validated
public interface IX01MatchService {

//	@PreAuthorize("isAuthenticated()")
	X01Match saveMatch(@Valid X01Match x01Match);

	X01Match getMatch(@NotNull ObjectId matchId);

	@PreAuthorize("isAuthenticated()")
	ArrayList<X01Match> getAuthenticatedUserMatches();

	@PreAuthorize("isAuthenticated()")
	X01Match updateMatch(@Valid X01Match x01Match, @NotNull ObjectId matchId);

	@PreAuthorize("isAuthenticated()")
	void deleteMatchForAuthenticatedUser(@NotNull ObjectId matchId);

	ArrayList<X01Checkout> getCheckouts() throws IOException;

}
