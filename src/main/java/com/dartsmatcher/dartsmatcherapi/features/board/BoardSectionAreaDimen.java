package com.dartsmatcher.dartsmatcherapi.features.board;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoardSectionAreaDimen {

	private BoardSectionArea sectionArea;
	private int inner;
	private int outer;

}
