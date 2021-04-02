package com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRoundScore {

	private String player;

	private int doublesMissed;

	@Min(0)
	@Max(180)
	private int score;

}
