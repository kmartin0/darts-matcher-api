package com.dartsmatcher.dartsmatcherapi.features.user;

import com.dartsmatcher.dartsmatcherapi.features.friendrequest.FriendsDetails;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface UserRepository extends MongoRepository<User, ObjectId> {
	Optional<User> findByEmailIgnoreCase(String email);
	Optional<User> findByUserNameIgnoreCase(String userName);

	@Aggregation(pipeline ={"{ $match: { _id: '?0' } }", "{ $lookup: { from : 'users', let: { friends: '$friends' }, pipeline: [ { $match: { $expr: { $in: ['$_id', '$$friends'] } } }, { $project: { userName: 1, firstName: 1, lastName: 1 } } ], as: 'friendsDetails' } }", "{ $project: { friendsDetails: 1, _id: 0 } }"})
	Optional<FriendsDetails> findFriendsDetails(ObjectId userId);

}
