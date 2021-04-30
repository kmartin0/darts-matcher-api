package com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Leg {

	@Min(1)
	private int leg;

	private String winner;

	private String throwsFirst;

	@Valid
	@NotNull
	private ArrayList<X01LegRound> rounds;

	@JsonIgnore
	public Optional<X01LegRound> getRound(int roundNumber) {
		return getRounds().stream()
				.filter(round -> round.getRound() == roundNumber)
				.findFirst();
	}

}
