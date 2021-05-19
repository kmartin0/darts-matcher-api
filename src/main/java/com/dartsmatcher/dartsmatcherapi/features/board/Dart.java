package com.dartsmatcher.dartsmatcherapi.features.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dart {

	// TODO: Custom validator checks if valid section (1...20, 25, 50)
	private int section;

	private BoardSectionArea area;

	public int getScore() {
		return section * area.getMultiplier();
	}
}
