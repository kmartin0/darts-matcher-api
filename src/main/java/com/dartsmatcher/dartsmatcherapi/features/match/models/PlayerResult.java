package com.dartsmatcher.dartsmatcherapi.features.match.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResult {

	@NotNull
	private String player;

	@Min(0)
	private int score;

	private ResultType result;

}
