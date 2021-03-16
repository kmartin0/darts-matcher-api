package com.dartsmatcher.dartsmatcherapi.features.match;

import com.dartsmatcher.dartsmatcherapi.features.match.models.Match;
import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Validated
public interface IMatchService {

	@PreAuthorize("isAuthenticated()")
	Match saveMatch(@Valid Match match);

//	@PreAuthorize("isAuthenticated()")
	Match getMatch(@NotNull ObjectId matchId);

	@PreAuthorize("isAuthenticated()")
	ArrayList<Match> getAuthenticatedUserMatches();

	@PreAuthorize("isAuthenticated()")
	Match updateMatch(@Valid Match match, @NotNull ObjectId matchId);

	@PreAuthorize("isAuthenticated()")
	void deleteMatchForAuthenticatedUser(@NotNull ObjectId matchId);

}
