package com.dartsmatcher.dartsmatcherapi.features.match.models;

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
public class PlayerLeg {

	@Min(1)
	private int leg;

	private ResultType result;

	@Max(3)
	private int dartsUsedFinalThrow;

	private int doublesMissed;

	@Valid
	@NotNull
	private ArrayList<Integer> scoring;

}
