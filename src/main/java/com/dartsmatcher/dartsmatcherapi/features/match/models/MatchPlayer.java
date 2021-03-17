package com.dartsmatcher.dartsmatcherapi.features.match.models;

import com.dartsmatcher.dartsmatcherapi.validators.anonymousname.NoReservedNames;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPlayer {

//	@NoReservedNames
//	private T playerId;

	@Valid
	private PlayerResult result;

	@Valid
	private MatchPlayerStatistics statistics;

	@Valid
	private ArrayList<PlayerSet> timeline;

}
