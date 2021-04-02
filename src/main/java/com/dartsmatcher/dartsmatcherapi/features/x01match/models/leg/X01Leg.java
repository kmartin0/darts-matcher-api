package com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Leg {

	@Min(1)
	private int leg;

	private String winner;

	@Max(3)
	private int dartsUsedFinalThrow;

	@Valid
	@NotNull
	private ArrayList<X01PlayerLeg> players;

//	@Valid
//	@NotNull
//	private ArrayList<X01LegRound> rounds;

}
