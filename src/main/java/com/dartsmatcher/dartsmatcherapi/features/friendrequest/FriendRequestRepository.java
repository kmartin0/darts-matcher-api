package com.dartsmatcher.dartsmatcherapi.features.friendrequest;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;
import java.util.Optional;

public interface FriendRequestRepository extends MongoRepository<FriendRequest, ObjectId> {
	ArrayList<FriendRequest> findBySenderAndReceiver(ObjectId sender, ObjectId receiver);

	ArrayList<FriendRequest> findBySenderOrReceiver(ObjectId sender, ObjectId receiver);

	ArrayList<FriendRequest> findBySender(ObjectId sender);

	ArrayList<FriendRequest> findAll();

//	ArrayList<FriendRequest> findBySender(ObjectId sender);
}
