package com.dartsmatcher.dartsmatcherapi.features.match;

import com.dartsmatcher.dartsmatcherapi.validators.anonymousname.ValidMatchPlayerIds;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.Valid;
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

	@NotNull
	@ValidMatchPlayerIds
	@Valid
	private ArrayList<MatchPlayer> players;

	@NotNull
	private MatchType matchType;
}
