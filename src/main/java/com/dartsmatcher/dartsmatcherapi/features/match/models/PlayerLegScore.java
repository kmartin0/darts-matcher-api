package com.dartsmatcher.dartsmatcherapi.features.match.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerLegScore {

	@NotNull
	private String player;

	private int doublesMissed;

	@Valid
	@NotNull
	private ArrayList<Integer> scoring;

}
