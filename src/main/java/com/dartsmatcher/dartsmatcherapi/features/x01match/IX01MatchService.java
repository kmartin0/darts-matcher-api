package com.dartsmatcher.dartsmatcherapi.features.x01match;

import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.InvalidArgumentsException;
import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.dartsmatcherapi.features.x01livematch.dto.X01DeleteLeg;
import com.dartsmatcher.dartsmatcherapi.features.x01livematch.dto.X01DeleteSet;
import com.dartsmatcher.dartsmatcherapi.features.x01livematch.dto.X01DeleteThrow;
import com.dartsmatcher.dartsmatcherapi.features.x01livematch.dto.X01Throw;
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

//	@PreAuthorize("isAuthenticated()")
	X01Match updateMatch(@Valid X01Match x01Match, @NotNull ObjectId matchId);

	X01Match updateMatch(@Valid X01Throw x01Throw) throws ResourceNotFoundException, IOException, InvalidArgumentsException;

	X01Match updateMatch(@Valid X01DeleteThrow x01DeleteThrow);

	X01Match updateMatch(@Valid X01DeleteSet x01DeleteSet);

	X01Match updateMatch(@Valid X01DeleteLeg x01DeleteLeg);

	ArrayList<X01Checkout> getCheckouts() throws IOException;

}
