package com.dartsmatcher.dartsmatcherapi.features.x01match;

import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import com.dartsmatcher.dartsmatcherapi.features.x01match.models.checkout.X01Checkout;
import com.dartsmatcher.dartsmatcherapi.utils.Endpoints;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;

@RestController
public class X01MatchController {

	private final IX01MatchService matchService;

	public X01MatchController(IX01MatchService matchService) {
		this.matchService = matchService;
	}

	@PostMapping(path = Endpoints.SAVE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
//	@PreAuthorize("isAuthenticated()")
	public X01Match saveMatch(@Valid @RequestBody X01Match x01Match) {

		return matchService.saveMatch(x01Match);
	}

	@GetMapping(path = Endpoints.GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public X01Match getMatch(@PathVariable @NotNull ObjectId matchId) {

		return matchService.getMatch(matchId);
	}

	@PutMapping(path = Endpoints.UPDATE_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public X01Match updateMatch(@Valid @RequestBody X01Match x01Match, @PathVariable ObjectId matchId) {

		return matchService.updateMatch(x01Match, matchId);
	}

	@GetMapping(path = Endpoints.GET_CHECKOUTS, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ArrayList<X01Checkout> getFinishes() throws IOException {

		return matchService.getCheckouts();
	}

}
