package com.dartsmatcher.dartsmatcherapi.features.friendrequest;

import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ForbiddenException;
import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ResourceAlreadyExistsException;
import com.dartsmatcher.dartsmatcherapi.exceptionhandler.exception.ResourceNotFoundException;
import com.dartsmatcher.dartsmatcherapi.features.user.IUserService;
import com.dartsmatcher.dartsmatcherapi.features.user.User;
import com.dartsmatcher.dartsmatcherapi.utils.MessageResolver;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class FriendRequestServiceImpl implements IFriendRequestService {

	private final IUserService userService;
	private final FriendRequestRepository friendRequestRepository;
	private final MessageResolver messageResolver;

	public FriendRequestServiceImpl(IUserService userService, FriendRequestRepository friendRequestRepository, MessageResolver messageResolver) {
		this.userService = userService;
		this.friendRequestRepository = friendRequestRepository;
		this.messageResolver = messageResolver;
	}

	public FriendRequest createFriendRequest(@Valid FriendRequest friendRequest) {
		// Check if sender and receiver exist.
		User sender = userService.getUser(friendRequest.getSender());
		userService.getUser(friendRequest.getReceiver());

		// Check if the sender is the authenticated user.
		User authenticatedUser = userService.getAuthenticatedUser();

		if (authenticatedUser.getId() != sender.getId()) {
			throw new ForbiddenException(messageResolver.getMessage("exception.forbidden"));
		}

		// Check if the friend request doesn't exist already.
		if (!friendRequestRepository.findBySenderAndReceiver(friendRequest.getSender(), friendRequest.getReceiver()).isEmpty()) {
			throw new ResourceAlreadyExistsException(FriendRequest.class, "receiver", friendRequest.getReceiver());
		}

		// Add the date.
		friendRequest.setDate(LocalDateTime.now());

		return friendRequestRepository.save(friendRequest);
	}

	public ArrayList<FriendRequest> getFriendRequests(ObjectId userId) {

		return friendRequestRepository.findBySenderOrReceiver(userId, userId);
	}

	public FriendRequest updateFriendRequest(ObjectId friendRequestId, @Valid FriendRequestStatus status) {
		// Get the friend request
		FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
				.orElseThrow(() -> new ResourceNotFoundException(FriendRequest.class, friendRequestId));

		// Check if the user is allowed to update the friend request.
		User authenticatedUser = userService.getAuthenticatedUser();

		if (authenticatedUser.getId() != friendRequest.getSender() ||
				authenticatedUser.getId() != friendRequest.getReceiver()) {
			throw new ForbiddenException(messageResolver.getMessage("exception.forbidden"));
		}

		// Accept the friend request when it's accepted. Otherwise simply delete it.
		if (status == FriendRequestStatus.ACCEPTED) acceptFriendRequest(friendRequest);

		// Delete the friend request.
		friendRequestRepository.delete(friendRequest);

		return friendRequest;
	}

	private void acceptFriendRequest(FriendRequest friendRequest) {
		User sender = userService.getUser(friendRequest.getSender());
		User receiver = userService.getUser(friendRequest.getReceiver());

		sender.getFriends().add(receiver.getId());
		receiver.getFriends().add(sender.getId());

		userService.updateFriends(sender.getId(), sender.getFriends());
		userService.updateFriends(receiver.getId(), receiver.getFriends());
	}

}
