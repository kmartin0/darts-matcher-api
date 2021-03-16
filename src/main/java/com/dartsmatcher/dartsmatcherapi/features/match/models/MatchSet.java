package com.dartsmatcher.dartsmatcherapi.features.match.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchSet {

	@Min(1)
	private int set;

	@NotNull
	private String winner;

	@NotNull
	@Valid
	private ArrayList<MatchLeg> legs;

}