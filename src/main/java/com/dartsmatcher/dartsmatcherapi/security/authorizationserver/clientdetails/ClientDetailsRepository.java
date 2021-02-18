package com.dartsmatcher.dartsmatcherapi.security.authorizationserver.clientdetails;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClientDetailsRepository extends MongoRepository<ClientDetailsImpl, ObjectId> {
	Optional<ClientDetailsImpl> findByClientId(String clientId);
}
