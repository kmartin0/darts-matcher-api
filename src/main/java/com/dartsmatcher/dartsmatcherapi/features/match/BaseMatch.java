package com.dartsmatcher.dartsmatcherapi.features.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseMatch {

	@MongoId
	private ObjectId id;

	@NotNull
	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String currentThrower;

	protected ArrayList<String> orderOfPlay;

	@NotNull
	private MatchType matchType;
}
