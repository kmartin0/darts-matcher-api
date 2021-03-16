package com.dartsmatcher.dartsmatcherapi.features.match.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchBestOf {

	@Min(1)
	private int legs;

	@Min(1)
	private int sets;

	@NotNull
	MatchBestOfType type;

}
