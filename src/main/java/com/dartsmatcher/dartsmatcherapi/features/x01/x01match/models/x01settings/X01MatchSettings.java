package com.dartsmatcher.dartsmatcherapi.features.x01.x01match.models.x01settings;

import com.dartsmatcher.dartsmatcherapi.features.basematch.MatchStatus;
import com.dartsmatcher.dartsmatcherapi.features.x01.x01match.models.bestof.X01BestOf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01MatchSettings {

	@Min(0)
	private int x01;

	private boolean trackDoubles;

	@NotNull
	private MatchStatus matchStatus; // TODO: Move to base match

	@Valid
	@NotNull
	private X01BestOf bestOf;

}
