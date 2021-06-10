package com.dartsmatcher.dartsmatcherapi.features.friendrequest;

import com.dartsmatcher.dartsmatcherapi.features.user.IUserService;
import com.dartsmatcher.dartsmatcherapi.features.user.User;
import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;

@Controller
@RestController
public class FriendRequestController {

	private final IFriendRequestService friendRequestService;
	private final IUserService userService;
	private final SimpMessagingTemplate messagingTemplate;

	public FriendRequestController(IFriendRequestService friendRequestService, IUserService userService, SimpMessagingTemplate messagingTemplate) {
		this.friendRequestService = friendRequestService;
		this.userService = userService;
		this.messagingTemplate = messagingTemplate;
	}

	@MessageMapping("/topic/friends/requests:create")
	@PreAuthorize("isAuthenticated()")
	public void createFriendRequest(@Valid @Payload FriendRequest friendRequest) {
		friendRequestService.createFriendRequest(friendRequest);

		sendUpdatedFriendRequests(friendRequest.getSender(), friendRequest.getReceiver());
	}

	@MessageMapping("/topic/friends/requests/{friendRequestId}:update")
	@PreAuthorize("isAuthenticated()")
	public void updateFriendRequest(@DestinationVariable ObjectId friendRequestId,
									@Valid @Payload FriendRequestStatus friendRequestStatus) {
		FriendRequest updatedFriendRequest = friendRequestService.updateFriendRequest(friendRequestId, friendRequestStatus);

		sendUpdatedFriendRequests(updatedFriendRequest.getSender(), updatedFriendRequest.getReceiver());
	}

	@SubscribeMapping("/topic/friends/requests")
	@PreAuthorize("isAuthenticated()")
	public ArrayList<FriendRequest> subscribeFriendRequests() {
		User authenticatedUser = userService.getAuthenticatedUser();

		return friendRequestService.getFriendRequests(authenticatedUser.getId());
	}

	private void sendUpdatedFriendRequests(ObjectId senderId, ObjectId receiverId) {
		// Send updated friend requests to sender. // TODO: Sort by date.
		User sender = userService.getUser(senderId);
		messagingTemplate.convertAndSendToUser(
				sender.getEmail(),
				"/topic/friends/requests",
				friendRequestService.getFriendRequests(sender.getId())
		);

		User receiver = userService.getUser(receiverId);

		// Send updated friend requests to receiver. // TODO: Sort by date.
		messagingTemplate.convertAndSendToUser(
				receiver.getEmail(),
				"/topic/friends/requests",
				friendRequestService.getFriendRequests(receiver.getId())
		);
	}
}


/*
Step 1:
		===============================
		Friend_Requests
		===============================
		from: objectId,
		to: objectId,
		status: (Declined, Accepted, Pending)
		===============================

		===============================
		User
		===============================
		friends: objectId[]
		...
		===============================

		Step 2:
		Expose Websocket (topic/{userId}/friends)
		Expose Message (topic/friends/requests:create)
		Expose Message (topic/friends/requests:update)

		User 1 Websocket Listens to subscribeFriends
		User 2 Websocket Listens to subscribeFriends

		User 2 Sends message which creates friend request to User 1
		User 1 + User 2 Websocket receives map (friends: Friend[], friendRequests: FriendRequest)
		User 1 Sends friend request update Message
		User 1 + User 2 Websocket receives map (friends: Friend[], friendRequests: FriendRequest)
*/
