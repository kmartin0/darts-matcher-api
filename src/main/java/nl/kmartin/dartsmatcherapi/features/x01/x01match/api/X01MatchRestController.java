package nl.kmartin.dartsmatcherapi.features.x01.x01match.api;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01EditTurn;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchService;
import nl.kmartin.dartsmatcherapi.rest.RestEndpoints;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provides REST endpoints for creating, retrieving and updating X01 matches.
 */
@RestController
public class X01MatchRestController {

    private final IX01MatchService matchService;

    public X01MatchRestController(IX01MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Creates a new X01 match.
     *
     * @param match the match to create
     * @return the created match
     */
    @PostMapping(path = RestEndpoints.X01_CREATE_MATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public X01Match createMatch(@Valid @RequestBody X01CreateMatchRequest createMatchRequest) {
        return matchService.createMatch(createMatchRequest);
    }

    /**
     * Gets an X01 match by id.
     *
     * @param matchId the match id
     * @return the requested match
     */
    @GetMapping(path = RestEndpoints.X01_GET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match getMatch(@PathVariable ObjectId matchId) {
        return matchService.getMatch(matchId);
    }

    /**
     * Gets multiple X01 matches by id.
     *
     * @param ids the match ids
     * @return the matching matches
     */
    @GetMapping(path = RestEndpoints.X01_GET_MATCHES, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<X01Match> getMatches(@RequestParam("ids") List<ObjectId> ids) {
        return matchService.getMatches(ids);
    }

    /**
     * Checks whether an X01 match exists.
     *
     * @param matchId the match id
     */
    @GetMapping(path = RestEndpoints.X01_MATCH_EXISTS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void matchExists(@PathVariable ObjectId matchId) {
        matchService.checkMatchExists(matchId);
    }

    /**
     * Adds a turn to an X01 match.
     *
     * @param matchId the match id
     * @param turn    the turn to add
     * @return the updated match
     */
    @PostMapping(path = RestEndpoints.X01_ADD_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match addTurn(@PathVariable ObjectId matchId, @Valid @RequestBody X01Turn turn) {
        return matchService.addTurn(matchId, turn);
    }

    /**
     * Edits an existing turn in an X01 match.
     *
     * @param matchId  the match id
     * @param editTurn the turn edit
     * @return the updated match
     */
    @PostMapping(path = RestEndpoints.X01_EDIT_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match editTurn(@PathVariable ObjectId matchId, @Valid @RequestBody X01EditTurn editTurn) {
        return matchService.editTurn(matchId, editTurn);
    }

    /**
     * Deletes the last turn from an X01 match.
     *
     * @param matchId the match id
     * @return the updated match
     */
    @PostMapping(path = RestEndpoints.X01_DELETE_LAST_TURN, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteLastTurn(@PathVariable ObjectId matchId) {
        return matchService.deleteLastTurn(matchId);
    }

    /**
     * Deletes an X01 match.
     *
     * @param matchId the match id
     */
    @PostMapping(path = RestEndpoints.X01_DELETE_MATCH)
    @ResponseStatus(HttpStatus.OK)
    public void deleteMatch(@PathVariable ObjectId matchId) {
        matchService.deleteMatch(matchId);
    }

    /**
     * Resets an X01 match to its initial state.
     *
     * @param matchId the match id
     * @return the reset match
     */
    @PostMapping(path = RestEndpoints.X01_RESET_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match resetMatch(@PathVariable ObjectId matchId) {
        return matchService.resetMatch(matchId);
    }

    /**
     * Reprocesses all derived state of an X01 match.
     *
     * @param matchId the match id
     * @return the reprocessed match
     */
    @PostMapping(path = RestEndpoints.X01_REPROCESS_MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match reprocessMatch(@PathVariable ObjectId matchId) {
        return matchService.reprocessMatch(matchId);
    }
}