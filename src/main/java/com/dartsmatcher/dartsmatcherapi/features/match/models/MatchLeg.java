package com.dartsmatcher.dartsmatcherapi.features.match.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchLeg {

	@Min(1)
	private int leg;

	@NotNull
	private String winner;

	@Min(1)
	@Max(3)
	private int dartsUsedFinalThrow;

	@Valid
	@NotNull
	private ArrayList<PlayerLegScore> players;

}
