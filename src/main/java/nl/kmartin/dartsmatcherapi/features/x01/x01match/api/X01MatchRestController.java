package nl.kmartin.dartsmatcherapi.features.x01.x01match.api;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateMatchRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchService;
import nl.kmartin.dartsmatcherapi.rest.RestEndpoints;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
     * @param createMatchRequest the match creation request
     * @return the created match
     */
    @PostMapping(path = RestEndpoints.X01.MATCHES, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public X01Match createMatch(@Valid @RequestBody X01CreateMatchRequest createMatchRequest) {
        return matchService.createMatch(createMatchRequest);
    }

    /**
     * Gets an X01 match by id.
     *
     * @param matchId the match id
     * @return the requested match
     * @throws ResourceNotFoundException when the match does not exist
     */
    @GetMapping(path = RestEndpoints.X01.MATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match getMatch(@PathVariable ObjectId matchId) {
        return matchService.getMatch(matchId);
    }

    /**
     * Gets multiple X01 matches by id.
     *
     * @param ids the match ids
     * @return the matching x01 matches
     */
    @GetMapping(path = RestEndpoints.X01.MATCHES, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<X01Match> getMatches(@RequestParam("ids") List<ObjectId> ids) {
        return matchService.getMatches(ids);
    }

    /**
     * Checks whether an X01 match exists.
     *
     * @param matchId the match id
     * @throws ResourceNotFoundException when the match does not exist
     */
    @GetMapping(path = RestEndpoints.X01.MATCH_EXISTS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void matchExists(@PathVariable ObjectId matchId) {
        matchService.checkMatchExists(matchId);
    }

    /**
     * Adds a turn to an X01 match.
     *
     * @param matchId     the match id
     * @param turnRequest the turn creation request
     * @return the updated match
     * @throws ResourceNotFoundException when the match or active set, leg or round cannot be resolved
     */
    @PostMapping(path = RestEndpoints.X01.MATCH_TURNS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match addTurn(@PathVariable ObjectId matchId, @Valid @RequestBody X01CreateTurnRequest turnRequest) {
        return matchService.addTurn(matchId, turnRequest);
    }

    /**
     * Edits an existing turn in an X01 match.
     *
     * @param matchId     the match id
     * @param turnRequest the turn edit request
     * @return the updated match
     * @throws ResourceNotFoundException when the match, target set, leg, round or player's existing turn cannot be found
     */
    @PostMapping(path = RestEndpoints.X01.MATCH_TURNS_EDIT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match editTurn(@PathVariable ObjectId matchId, @Valid @RequestBody X01EditTurnRequest turnRequest) {
        return matchService.editTurn(matchId, turnRequest);
    }

    /**
     * Deletes the last turn from an X01 match.
     *
     * @param matchId the match id
     * @return the updated match
     * @throws ResourceNotFoundException when the match does not exist
     */
    @PostMapping(path = RestEndpoints.X01.MATCH_TURNS_DELETE_LAST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match deleteLastTurn(@PathVariable ObjectId matchId) {
        return matchService.deleteLastTurn(matchId);
    }

    /**
     * Deletes an X01 match.
     *
     * @param matchId the match id
     * @throws ResourceNotFoundException when the match does not exist
     */
    @DeleteMapping(path = RestEndpoints.X01.MATCH)
    @ResponseStatus(HttpStatus.OK)
    public void deleteMatch(@PathVariable ObjectId matchId) {
        matchService.deleteMatch(matchId);
    }

    /**
     * Resets an X01 match to its initial state.
     *
     * @param matchId the match id
     * @return the reset match
     * @throws ResourceNotFoundException when the match does not exist
     */
    @PostMapping(path = RestEndpoints.X01.MATCH_RESET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match resetMatch(@PathVariable ObjectId matchId) {
        return matchService.resetMatch(matchId);
    }

    /**
     * Reprocesses all derived state of an X01 match.
     *
     * @param matchId the match id
     * @return the reprocessed match
     * @throws ResourceNotFoundException when the match does not exist
     */
    @PostMapping(path = RestEndpoints.X01.MATCH_REPROCESS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public X01Match reprocessMatch(@PathVariable ObjectId matchId) {
        return matchService.reprocessMatch(matchId);
    }
}