package com.dartsmatcher.dartsmatcherapi.features.livematch;

import com.dartsmatcher.dartsmatcherapi.features.match.models.Match;
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
	public Match updateMatch(@Valid @Payload Match match, @DestinationVariable ObjectId matchId) {

		return liveMatchService.updateLiveMatch(match, matchId);
	}

	@SubscribeMapping("/live/matches/{matchId}")
	public Match subscribeMatch(@DestinationVariable ObjectId matchId) {
		return liveMatchService.getLiveMatch(matchId);
	}

}

// http://jxy.me/websocket-debug-tool/

// Websocket Url:
// ws://localhost:8080/darts-matcher-websocket/websocket

// STOMP subscribe destination:
// /live/matches/603790be22cb35a3de6ef027

// STOMP send destination:
// /live/matches/603790be22cb35a3de6ef027:update

// message content:
// { 	"id": "603790be22cb35a3de6ef027", 	"players": ["603790be22cb35a3de6ef023", "603790be22cb35a3de6ef024"], 	"startDate": "2021-02-02T14:12:00", 	"matchType": "MATCH_501", 	"matchStatus": "IN_PLAY", 	"throwFirst": "603790be22cb35a3de6ef023", 	"bestOf": { 		"legs": 3, 		"sets": 1, 		"bestOfType": "LEGS" 	}, 	"result": [{ 		"player": "603790be22cb35a3de6ef023", 		"score": 2, 		"result": "WIN" 	}, { 		"player": "603790be22cb35a3de6ef024", 		"score": 1, 		"result": "LOSE" 	}], 	"statistics": [{ 		"player": "603790be22cb35a3de6ef023", 		"pointsThrown": 1242, 		"dartsThrown": 34, 		"average": 109, 		"tonPlus": 3, 		"tonForty": 3, 		"tonEighty": 1, 		"checkoutHighest": 31, 		"checkoutTonPlus": 0, 		"checkoutPercentage": 28, 		"checkoutsMissed": 5, 		"checkoutsHit": 2 	}, { 		"player": "603790be22cb35a3de6ef024", 		"pointsThrown": 1194, 		"dartsThrown": 33, 		"average": 108, 		"tonPlus": 2, 		"tonForty": 1, 		"tonEighty": 3, 		"checkoutHighest": 141, 		"checkoutTonPlus": 1, 		"checkoutPercentage": 100, 		"checkoutsMissed": 0, 		"checkoutsHit": 1 	}], 	"timeline": [{ 		"set": 1, 		"legs": [{ 			"leg": 1, 			"winner": "603790be22cb35a3de6ef023", 			"dartsUsedFinalThrow": 2, 			"players": [{ 				"player": "603790be22cb35a3de6ef023", 				"doublesMissed": 3, 				"scoring": [140, 100, 90, 140, 31] 			}, { 				"player": "603790be22cb35a3de6ef024", 				"doublesMissed": 0, 				"scoring": [120, 88, 27, 180] 			}] 		}, { 			"leg": 2, 			"winner": "603790be22cb35a3de6ef024", 			"dartsUsedFinalThrow": 3, 			"players": [{ 				"player": "603790be22cb35a3de6ef023", 				"doublesMissed": 0, 				"scoring": [140, 100] 			}, { 				"player": "603790be22cb35a3de6ef024", 				"doublesMissed": 0, 				"scoring": [180, 180, 141] 			}] 		}, { 			"leg": 3, 			"winner": "603790be22cb35a3de6ef023", 			"dartsUsedFinalThrow": 2, 			"players": [{ 				"player": "603790be22cb35a3de6ef023", 				"doublesMissed": 2, 				"scoring": [121, 180, 90, 90, 20] 			}, { 				"player": "603790be22cb35a3de6ef024", 				"doublesMissed": 0, 				"scoring": [120, 88, 27, 43] 			}] 		}], 		"winner": "603790be22cb35a3de6ef023" 	}] }
