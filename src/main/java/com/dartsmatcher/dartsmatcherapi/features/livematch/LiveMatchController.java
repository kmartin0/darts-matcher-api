package com.dartsmatcher.dartsmatcherapi.features.livematch;

import com.dartsmatcher.dartsmatcherapi.features.x01match.models.X01Match;
import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

@Controller
public class LiveMatchController {

	private final ILiveMatchService liveMatchService;

	public LiveMatchController(ILiveMatchService liveMatchService) {
		this.liveMatchService = liveMatchService;
	}

	@MessageMapping("/live/matches/{matchId}:update")
	@SendTo("/live/matches/{matchId}")
	public X01Match updateMatch(@Valid @Payload X01Throw x01Throw) {

		return liveMatchService.updateLiveMatch(x01Throw);
	}

	@MessageMapping("/live/matches/{matchId}:delete-throw")
	@SendTo("/live/matches/{matchId}")
	public X01Match deleteThrowLiveMatch(@Valid @Payload X01DeleteThrow x01DeleteThrow) {

		return liveMatchService.deleteThrowLiveMatch(x01DeleteThrow);
	}

	@MessageMapping("/live/matches/{matchId}:delete-set")
	@SendTo("/live/matches/{matchId}")
	public X01Match deleteThrowLiveMatch(@Valid @Payload X01DeleteSet x01DeleteSet) {

		return liveMatchService.deleteSetLiveMatch(x01DeleteSet);
	}

	@MessageMapping("/live/matches/{matchId}:delete-leg")
	@SendTo("/live/matches/{matchId}")
	public X01Match deleteThrowLiveMatch(@Valid @Payload X01DeleteLeg x01DeleteLeg) {

		return liveMatchService.deleteLegLiveMatch(x01DeleteLeg);
	}

	@SubscribeMapping("/live/matches/{matchId}")
	public X01Match subscribeMatch(@DestinationVariable ObjectId matchId) {
		return liveMatchService.getLiveMatch(matchId);
	}

}

// http://jxy.me/websocket-debug-tool/

// Websocket Url:
// ws://localhost:8080/darts-matcher-websocket/websocket

// STOMP subscribe destination:
// /live/matches/60548c38ccf8f5463f9d3402

// STOMP send destination:
// /live/matches/60548c38ccf8f5463f9d3402:update

// message content:
/*
{
	"matchId": "60548c38ccf8f5463f9d3402",
	"playerId": "Kevinmartin0",
	"leg": 1,
	"set": 1,
	"round": 1,
	"score": 60
}
 */