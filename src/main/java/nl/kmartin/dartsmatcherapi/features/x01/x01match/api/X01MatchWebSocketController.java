package nl.kmartin.dartsmatcherapi.features.x01.x01match.api;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapi.error.exception.InvalidArgumentsException;
import nl.kmartin.dartsmatcherapi.error.exception.ResourceNotFoundException;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01CreateTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.dto.X01EditTurnRequest;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.message.X01MatchMessageType;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.model.X01Match;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchService;
import nl.kmartin.dartsmatcherapi.websocket.destination.WebSocketDestinations;
import nl.kmartin.dartsmatcherapi.websocket.destination.WebSocketHeaders;
import nl.kmartin.dartsmatcherapi.websocket.event.publisher.IWebSocketEventPublisher;
import nl.kmartin.dartsmatcherapi.websocket.message.model.WebSocketMessage;
import org.bson.types.ObjectId;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * Handles WebSocket subscriptions and commands for X01 matches.
 *
 * Match updates are processed by the match service and published back through the WebSocket event publisher.
 */
@Controller
public class X01MatchWebSocketController {

    private final IX01MatchService matchService;
    private final IWebSocketEventPublisher webSocketEventPublisher;

    public X01MatchWebSocketController(
            IX01MatchService matchService,
            IWebSocketEventPublisher webSocketEventPublisher
    ) {
        this.matchService = matchService;
        this.webSocketEventPublisher = webSocketEventPublisher;
    }

    /**
     * Returns the current match when a client subscribes to an X01 match.
     *
     * @param matchId the match id
     * @return a message containing the current match
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     */
    @SubscribeMapping(WebSocketDestinations.X01.MATCH)
    public WebSocketMessage<X01MatchMessageType, X01Match> subscribeX01Match(
            @DestinationVariable ObjectId matchId
    ) {
        return new WebSocketMessage<>(
                X01MatchMessageType.PROCESS_MATCH,
                matchService.getMatch(matchId)
        );
    }

    /**
     * Sends the existing or newly created rematch to the requesting client session.
     *
     * @param matchId   the original match id
     * @param publishId the optional client publish id
     * @param sessionId the WebSocket session id
     * @throws ResourceNotFoundException         when the original match does not exist
     * @throws OptimisticLockingFailureException when an affected match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    @MessageMapping(WebSocketDestinations.X01.REMATCH)
    public void createRematch(
            @DestinationVariable ObjectId matchId,
            @Header(value = WebSocketHeaders.PUBLISH_ID, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        webSocketEventPublisher.sendToUser(
                X01MatchMessageType.REMATCH,
                matchService.createRematch(matchId),
                sessionId,
                publishId
        );
    }

    /**
     * Adds a turn and publishes the updated match.
     *
     * @param matchId     the match id
     * @param turnRequest the turn creation request
     * @param publishId   the optional client publish id
     * @param sessionId   the WebSocket session id
     * @throws InvalidArgumentsException         when the submitted turn cannot be applied to the current match state
     * @throws ResourceNotFoundException         when the match or active set, leg or round cannot be resolved
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    @MessageMapping(WebSocketDestinations.X01.ADD_TURN)
    public void addTurn(
            @DestinationVariable ObjectId matchId,
            @Valid @Payload X01CreateTurnRequest turnRequest,
            @Header(value = WebSocketHeaders.PUBLISH_ID, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        webSocketEventPublisher.sendToUser(
                X01MatchMessageType.ADD_HUMAN_TURN,
                matchService.addTurn(matchId, turnRequest),
                sessionId,
                publishId
        );
    }

    /**
     * Edits a turn and publishes the updated match.
     *
     * @param matchId     the match id
     * @param turnRequest the turn edit request
     * @param publishId   the optional client publish id
     * @param sessionId   the WebSocket session id
     * @throws InvalidArgumentsException         when the submitted turn cannot be applied to the resulting match state
     * @throws ResourceNotFoundException         when the match, target set, leg, round or player's existing turn cannot be found
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    @MessageMapping(WebSocketDestinations.X01.EDIT_TURN)
    public void editTurn(
            @DestinationVariable ObjectId matchId,
            @Valid @Payload X01EditTurnRequest turnRequest,
            @Header(value = WebSocketHeaders.PUBLISH_ID, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        webSocketEventPublisher.sendToUser(
                X01MatchMessageType.EDIT_TURN,
                matchService.editTurn(matchId, turnRequest),
                sessionId,
                publishId
        );
    }

    /**
     * Deletes the last human turn and any following Dart Bot turns and publishes the resulting match.
     *
     * @param matchId   the match id
     * @param publishId the optional client publish id
     * @param sessionId the WebSocket session id
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    @MessageMapping(WebSocketDestinations.X01.DELETE_LAST_TURN)
    public void deleteLastHumanTurn(
            @DestinationVariable ObjectId matchId,
            @Header(value = WebSocketHeaders.PUBLISH_ID, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        webSocketEventPublisher.sendToUser(
                X01MatchMessageType.DELETE_LAST_TURN,
                matchService.deleteLastHumanTurn(matchId),
                sessionId,
                publishId
        );
    }

    /**
     * Deletes an X01 match and sends its id to the requesting client session.
     *
     * @param matchId   the match id
     * @param publishId the optional client publish id
     * @param sessionId the WebSocket session id
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when an affected match was modified concurrently
     */
    @MessageMapping(WebSocketDestinations.X01.DELETE_MATCH)
    public void deleteMatch(
            @DestinationVariable ObjectId matchId,
            @Header(value = WebSocketHeaders.PUBLISH_ID, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        matchService.deleteMatch(matchId);

        webSocketEventPublisher.sendToUser(
                X01MatchMessageType.DELETE_MATCH,
                matchId,
                sessionId,
                publishId
        );
    }

    /**
     * Resets a match and publishes the reset state.
     *
     * @param matchId   the match id
     * @param publishId the optional client publish id
     * @param sessionId the WebSocket session id
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    @MessageMapping(WebSocketDestinations.X01.RESET_MATCH)
    public void resetMatch(
            @DestinationVariable ObjectId matchId,
            @Header(value = WebSocketHeaders.PUBLISH_ID, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        webSocketEventPublisher.sendToUser(
                X01MatchMessageType.RESET_MATCH,
                matchService.resetMatch(matchId),
                sessionId,
                publishId
        );
    }

    /**
     * Reprocesses a match and publishes the rebuilt state.
     *
     * @param matchId   the match id
     * @param publishId the optional client publish id
     * @param sessionId the WebSocket session id
     * @throws ResourceNotFoundException         when the match does not exist
     * @throws OptimisticLockingFailureException when the match was modified concurrently
     * @throws IllegalStateException             when Dart Bot processing encounters invalid match state
     */
    @MessageMapping(WebSocketDestinations.X01.REPROCESS_MATCH)
    public void reprocessMatch(
            @DestinationVariable ObjectId matchId,
            @Header(value = WebSocketHeaders.PUBLISH_ID, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        webSocketEventPublisher.sendToUser(
                X01MatchMessageType.PROCESS_MATCH,
                matchService.reprocessMatch(matchId),
                sessionId,
                publishId
        );
    }
}