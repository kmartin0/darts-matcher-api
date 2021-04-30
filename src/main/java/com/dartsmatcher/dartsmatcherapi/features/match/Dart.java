package com.dartsmatcher.dartsmatcherapi.features.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dart {
	@Min(1)
	@Max(20)
	private int section;

	private DartSectionArea area;
}
