package com.dartsmatcher.dartsmatcherapi.features.friendrequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "friend_requests")
@TypeAlias("FriendRequest")
public class FriendRequest {

	@MongoId
	private ObjectId id;

	@NotNull
	private ObjectId sender;

	@NotNull
	private ObjectId receiver;

	private LocalDateTime date;

}
