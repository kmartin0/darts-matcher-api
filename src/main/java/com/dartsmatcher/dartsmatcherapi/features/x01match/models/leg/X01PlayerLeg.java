package com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01PlayerLeg {

	@NotNull
	private String playerId;

	private int doublesMissed;

	@Valid
	@NotNull
	private ArrayList<Integer> scoring;

}
