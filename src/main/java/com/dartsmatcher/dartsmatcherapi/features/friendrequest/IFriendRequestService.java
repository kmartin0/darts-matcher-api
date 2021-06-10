package com.dartsmatcher.dartsmatcherapi.features.friendrequest;

import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;

@Validated
public interface IFriendRequestService {
	@PreAuthorize("isAuthenticated()")
	FriendRequest createFriendRequest(@Valid FriendRequest friendRequest);

	ArrayList<FriendRequest> getFriendRequests(ObjectId userId);

	@PreAuthorize("isAuthenticated()")
	FriendRequest updateFriendRequest(ObjectId friendRequestId, @Valid FriendRequestStatus status);

}
