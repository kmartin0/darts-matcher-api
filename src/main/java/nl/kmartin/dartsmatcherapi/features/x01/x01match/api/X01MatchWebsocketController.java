package nl.kmartin.dartsmatcherapi.features.x01.x01match.api;

import jakarta.validation.Valid;
import nl.kmartin.dartsmatcherapi.common.Constants;
import nl.kmartin.dartsmatcherapi.common.IEventPublisherService;
import nl.kmartin.dartsmatcherapi.common.WebSocketSendToUserEvent;
import nl.kmartin.dartsmatcherapi.common.WebsocketDestinations;
import nl.kmartin.dartsmatcherapi.features.x01.model.X01EditTurn;
import nl.kmartin.dartsmatcherapi.features.x01.model.X01Turn;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.event.X01MatchEvent;
import nl.kmartin.dartsmatcherapi.features.x01.x01match.service.IX01MatchService;
import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class X01MatchWebsocketController {

    private final IX01MatchService matchService;
    private final IEventPublisherService publisherService;

    public X01MatchWebsocketController(
            IX01MatchService matchService,
            IEventPublisherService publisherService
    ) {
        this.matchService = matchService;
        this.publisherService = publisherService;
    }

    @SubscribeMapping(WebsocketDestinations.X01.MATCH)
    public X01MatchEvent.ProcessMatch subscribeX01Match(@DestinationVariable ObjectId matchId) {
        return new X01MatchEvent.ProcessMatch(matchService.getMatch(matchId));
    }

    @MessageMapping(WebsocketDestinations.X01.ADD_TURN)
    public void addTurn(
            @DestinationVariable ObjectId matchId,
            @Valid @Payload X01Turn turn,
            @Header(value = Constants.PUBLISH_ID_HEADER, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        publishToUser(
                new X01MatchEvent.AddHumanTurn(matchService.addTurn(matchId, turn)),
                sessionId,
                publishId
        );
    }

    @MessageMapping(WebsocketDestinations.X01.EDIT_TURN)
    public void editTurn(
            @DestinationVariable ObjectId matchId,
            @Valid @Payload X01EditTurn editTurn,
            @Header(value = Constants.PUBLISH_ID_HEADER, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        publishToUser(
                new X01MatchEvent.EditTurn(matchService.editTurn(matchId, editTurn)),
                sessionId,
                publishId
        );
    }

    @MessageMapping(WebsocketDestinations.X01.DELETE_LAST_TURN)
    public void deleteLastTurn(
            @DestinationVariable ObjectId matchId,
            @Header(value = Constants.PUBLISH_ID_HEADER, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        publishToUser(
                new X01MatchEvent.DeleteLastTurn(matchService.deleteLastTurn(matchId)),
                sessionId,
                publishId
        );
    }

    @MessageMapping(WebsocketDestinations.X01.DELETE_MATCH)
    public void deleteMatch(
            @DestinationVariable ObjectId matchId,
            @Header(value = Constants.PUBLISH_ID_HEADER, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        matchService.deleteMatch(matchId);

        publishToUser(
                new X01MatchEvent.DeleteMatch(matchId),
                sessionId,
                publishId
        );
    }

    @MessageMapping(WebsocketDestinations.X01.RESET_MATCH)
    public void resetMatch(
            @DestinationVariable ObjectId matchId,
            @Header(value = Constants.PUBLISH_ID_HEADER, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        publishToUser(
                new X01MatchEvent.ResetMatch(matchService.resetMatch(matchId)),
                sessionId,
                publishId
        );
    }

    @MessageMapping(WebsocketDestinations.X01.REPROCESS_MATCH)
    public void reprocessMatch(
            @DestinationVariable ObjectId matchId,
            @Header(value = Constants.PUBLISH_ID_HEADER, required = false) String publishId,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
    ) {
        publishToUser(
                new X01MatchEvent.ProcessMatch(matchService.reprocessMatch(matchId)),
                sessionId,
                publishId
        );
    }

    private void publishToUser(Object payload, String sessionId, String publishId) {
        publisherService.publish(
                new WebSocketSendToUserEvent(
                        payload,
                        sessionId,
                        publishId
                )
        );
    }
}