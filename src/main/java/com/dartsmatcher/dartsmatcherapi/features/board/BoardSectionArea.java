package com.dartsmatcher.dartsmatcherapi.features.board;

public enum BoardSectionArea {
	DOUBLE_BULL, SINGLE_BULL, INNER_SINGLE, TRIPLE, OUTER_SINGLE, DOUBLE, MISS;

	public int getMultiplier() {
		switch (this) {
			case INNER_SINGLE:
			case OUTER_SINGLE:
			case SINGLE_BULL:
			case DOUBLE_BULL:
				return 1;

			case DOUBLE:
				return 2;

			case TRIPLE:
				return 3;

			default:
				return 0;
		}
	}
}
