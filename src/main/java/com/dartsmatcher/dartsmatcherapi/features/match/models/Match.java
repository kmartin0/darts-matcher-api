package com.dartsmatcher.dartsmatcherapi.features.match.models;

import com.dartsmatcher.dartsmatcherapi.utils.MatchUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "matches")
@TypeAlias("Match")
public class Match {

	@MongoId
	private ObjectId id;

	@NotNull
	@Valid
	private MatchPlayers players;

	@NotNull
	private LocalDateTime startDate;

	private LocalDateTime endDate;

	@NotNull
	private MatchType matchType;

	@NotNull
	private MatchStatus matchStatus;

	@NotNull
	private String throwFirst;

	@Valid
	@NotNull
	private MatchBestOf bestOf;

	@Valid
	private ArrayList<PlayerResult> result;

	@Valid
	private ArrayList<MatchPlayerStatistics> statistics;

	@Valid
	private ArrayList<MatchSet> timeline;

	public void updateResultAndStatistics(Clock clock) {
		MatchUtils.updateMatchStatisticsAndResult(this, clock);
	}

}
