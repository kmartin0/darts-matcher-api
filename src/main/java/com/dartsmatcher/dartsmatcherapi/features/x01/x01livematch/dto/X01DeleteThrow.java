package com.dartsmatcher.dartsmatcherapi.features.x01.x01livematch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01DeleteThrow {

	private ObjectId matchId;

	@NotEmpty
	private String playerId;

	@Min(0)
	private int leg;

	@Min(0)
	private int set;

	@Min(0)
	private int round;

}
