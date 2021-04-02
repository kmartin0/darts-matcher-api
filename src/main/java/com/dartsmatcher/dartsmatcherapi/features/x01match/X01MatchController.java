package com.dartsmatcher.dartsmatcherapi.features.x01match;

import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
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
public class X01MatchController {

	private final IX01MatchService matchService;

	public X01MatchController(IX01MatchService matchService) {
		this.matchService = matchService;
	}

	@PostMapping(path = Endpoints.SAVE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public X01Match saveMatch(@Valid @RequestBody X01Match x01Match) {

		return matchService.saveMatch(x01Match);
	}

	@GetMapping(path = Endpoints.GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public X01Match getMatch(@PathVariable @NotNull ObjectId matchId) {

		return matchService.getMatch(matchId);
	}

	@GetMapping(path = Endpoints.GET_ALL_USER_MATCHES, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public ArrayList<X01Match> getUserMatches() {

		return matchService.getAuthenticatedUserMatches();
	}

	@PutMapping(path = Endpoints.UPDATE_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public X01Match updateMatch(@Valid @RequestBody X01Match x01Match, @PathVariable ObjectId matchId) {

		return matchService.updateMatch(x01Match, matchId);
	}

	@DeleteMapping(path = Endpoints.DELETE_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("isAuthenticated()")
	public void setDeleteMatch(@PathVariable ObjectId matchId) {

		matchService.deleteMatchForAuthenticatedUser(matchId);
	}

}
