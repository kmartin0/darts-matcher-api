package com.dartsmatcher.dartsmatcherapi.features.user;

import com.dartsmatcher.dartsmatcherapi.features.friendrequest.FriendsDetails;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;
import java.util.Optional;


public interface UserRepository extends MongoRepository<User, ObjectId> {
	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByUserNameIgnoreCase(String userName);

	// TODO: Sort by alphabet
	@Aggregation(pipeline = {"{ $match: { _id: '?0' } }", "{ $lookup: { from : 'users', let: { friends: '$friends' }, pipeline: [ { $match: { $expr: { $in: ['$_id', '$$friends'] } } } ], as: 'friendsDetails' } }"})
	Optional<FriendsDetails> findFriendsDetails(ObjectId userId);

	@ExistsQuery(value = "{ _id:  ?0, friends: { $in: [ ?1 ] } }")
	boolean hasFriend(ObjectId user1, ObjectId user2);

	ArrayList<User> findAllBy(TextCriteria criteria);

}
