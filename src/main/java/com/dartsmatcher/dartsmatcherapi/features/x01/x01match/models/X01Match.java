package com.dartsmatcher.dartsmatcherapi.features.x01.x01match.models;

import com.dartsmatcher.dartsmatcherapi.features.basematch.BaseMatch;
import com.dartsmatcher.dartsmatcherapi.features.basematch.MatchPlayer;
import com.dartsmatcher.dartsmatcherapi.features.basematch.MatchStatus;
import com.dartsmatcher.dartsmatcherapi.features.basematch.MatchType;
import com.dartsmatcher.dartsmatcherapi.features.x01.x01match.models.bestof.X01BestOf;
import com.dartsmatcher.dartsmatcherapi.features.x01.x01match.models.playerresult.X01PlayerResult;
import com.dartsmatcher.dartsmatcherapi.features.x01.x01match.models.set.X01Set;
import com.dartsmatcher.dartsmatcherapi.features.x01.x01match.models.statistics.X01PlayerStatistics;
import com.dartsmatcher.dartsmatcherapi.utils.X01ResultUtils;
import com.dartsmatcher.dartsmatcherapi.utils.X01StatisticsUtils;
import com.dartsmatcher.dartsmatcherapi.utils.X01ThrowerUtils;
import com.dartsmatcher.dartsmatcherapi.utils.X01TimelineUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "matches")
@TypeAlias("X01Match")
public class X01Match extends BaseMatch {

	public X01Match(ObjectId id, @NotNull LocalDateTime startDate, LocalDateTime endDate, String currentThrower,
					@NotNull @Valid ArrayList<MatchPlayer> players, @NotNull MatchType matchType, @Min(0) int x01,
					boolean trackDoubles, @NotNull MatchStatus matchStatus, @Valid @NotNull X01BestOf bestOf,
					@Valid ArrayList<X01PlayerResult> result, @Valid ArrayList<X01PlayerStatistics> statistics,
					@Valid ArrayList<X01Set> timeline) {
		super(id, startDate, endDate, currentThrower, matchStatus, players, matchType);
		this.x01 = x01;
		this.trackDoubles = trackDoubles;
		this.bestOf = bestOf;
		this.result = result;
		this.statistics = statistics;
		this.timeline = timeline;
	}

	// TODO: Replace with X01MatchSettings
	@Min(0)
	private int x01;

	private boolean trackDoubles;

	@Valid
	@NotNull
	private X01BestOf bestOf;

	@Valid
	private ArrayList<X01PlayerResult> result;

	@Valid
	private ArrayList<X01PlayerStatistics> statistics;

	@Valid
	private ArrayList<X01Set> timeline;

	@JsonIgnore
	public Optional<X01Set> getSet(int set) {
		if (getTimeline() == null) return Optional.empty();

		return getTimeline().stream()
				.filter(x01PlayerSet -> x01PlayerSet.getSet() == set)
				.findFirst();
	}

	@JsonIgnore
	public void updateThrower() {
		X01ThrowerUtils.updateThrower(this);
	}

	@JsonIgnore
	public void updateResult() {
		X01ResultUtils.updateMatchResult(this);
		updateMatchStatus();
	}

	@JsonIgnore
	public void updateResult(int set) {
		X01ResultUtils.updateMatchResult(this, getSet(set).orElse(null));
		updateMatchStatus();
	}

	@JsonIgnore
	public void updateStatistics() {
		X01StatisticsUtils.updateStatistics(this);
	}

	@JsonIgnore
	public void updateTimeline() {
		X01TimelineUtils.updateTimeline(this);
	}

	@JsonIgnore
	public void updateAll() {
		updateResult();
		updateTimeline();
		updateThrower();
		updateStatistics();
	}

	@JsonIgnore
	public void updateAll(int set) {
		updateResult(set);
		updateTimeline();
		updateThrower();
		updateStatistics();
	}

	@JsonIgnore
	public void updateMatchStatus() {
		if (MatchStatus.LOBBY.equals(getMatchStatus())) return;

		if (getResult() != null && getResult().stream().anyMatch(playerResult -> playerResult.getResult() != null)) {
			setMatchStatus(MatchStatus.CONCLUDED);
		} else {
			setMatchStatus(MatchStatus.IN_PLAY);
		}
	}

}
