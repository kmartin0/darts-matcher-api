package com.dartsmatcher.dartsmatcherapi.features.match;

import com.dartsmatcher.dartsmatcherapi.features.match.models.Match;
import com.dartsmatcher.dartsmatcherapi.utils.Endpoints;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@RestController
public class MatchController {

	private final IMatchService matchService;

	public MatchController(IMatchService matchService) {
		this.matchService = matchService;
	}

	@PostMapping(path = Endpoints.SAVE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public Match saveMatch(@Valid @RequestBody Match match) {

		return matchService.saveMatch(match);
	}

	@GetMapping(path = Endpoints.GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
//	@PreAuthorize("isAuthenticated()")
	public Match getMatch(@PathVariable @NotNull ObjectId matchId) {

		return matchService.getMatch(matchId);
	}

	@GetMapping(path = Endpoints.GET_ALL_USER_MATCHES, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public ArrayList<Match> getUserMatches() {

		return matchService.getAuthenticatedUserMatches();
	}

	@PutMapping(path = Endpoints.UPDATE_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public Match updateMatch(@Valid @RequestBody Match match, @PathVariable ObjectId matchId) {

		return matchService.updateMatch(match, matchId);
	}

	@DeleteMapping(path = Endpoints.DELETE_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("isAuthenticated()")
	public void setDeleteMatch(@PathVariable ObjectId matchId) {

		matchService.deleteMatchForAuthenticatedUser(matchId);
	}

}
