package com.dartsmatcher.dartsmatcherapi.features;

import com.dartsmatcher.dartsmatcherapi.features.board.Board;
import com.dartsmatcher.dartsmatcherapi.features.board.BoardSectionArea;
import com.dartsmatcher.dartsmatcherapi.utils.CartesianCoordinate;
import com.dartsmatcher.dartsmatcherapi.utils.PolarCoordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BoardTests {

	private Board board;

	@BeforeEach
	void beforeEach() {
		board = new Board();
	}

	@Test
	void test_0_0_isBull() {
		Assertions.assertEquals(50, board.getScoreCartesian(new CartesianCoordinate(0, 0)));
	}

	@Test
	void test_2_101_isTriple20() {
		Assertions.assertEquals(60, board.getScoreCartesian(new CartesianCoordinate(2, 101)));
	}

	@Test
	void test_Neg163_Neg1_isDouble11() {
		Assertions.assertEquals(22, board.getScoreCartesian(new CartesianCoordinate(-163, -1)));
	}

	@Test
	void test_6_18_isSingle1() {
		Assertions.assertEquals(1, board.getScoreCartesian(new CartesianCoordinate(6, 18)));
	}

	@Test
	void test_Neg6_18_isSingle5() {
		Assertions.assertEquals(5, board.getScoreCartesian(new CartesianCoordinate(-6, 18)));
	}

	@Test
	void test_45_Neg169_isMiss() {
		Assertions.assertEquals(0, board.getScoreCartesian(new CartesianCoordinate(45, -169)));
	}

	@Test
	void test_22_22_isSingle4() {
		Assertions.assertEquals(4, board.getScoreCartesian(new CartesianCoordinate(22, 22)));
	}

	@Test
	void test_Neg150_0_isSingle11() {
		Assertions.assertEquals(11, board.getScoreCartesian(new CartesianCoordinate(-150, 0)));
	}

	@Test
	void test_Neg100_5_isTriple17() {
		Assertions.assertEquals(51, board.getScoreCartesian(new CartesianCoordinate(16, -101)));
	}

	@Test
	void testGetCenterBull() {
		Assertions.assertEquals(new PolarCoordinate(0, 0), board.getCenter(20, BoardSectionArea.DOUBLE_BULL));
	}

	@Test
	void testGetCenter_20_SingleBull() {
		Assertions.assertEquals(new PolarCoordinate(10.5, 1.5707963267948966), board.getCenter(20, BoardSectionArea.SINGLE_BULL));
	}

	@Test
	void testGetCenter_6_InnerSingle() {
		Assertions.assertEquals(new PolarCoordinate(57, 0), board.getCenter(6, BoardSectionArea.INNER_SINGLE));
	}

	@Test
	void testGetCenter_17_Triple() {
		Assertions.assertEquals(new PolarCoordinate(102.5, 5.026548245743669), board.getCenter(17, BoardSectionArea.TRIPLE));
	}

	@Test
	void testGetCenter_11_OuterSingle() {
		Assertions.assertEquals(new PolarCoordinate(134, Math.PI), board.getCenter(11, BoardSectionArea.OUTER_SINGLE));
	}

	@Test
	void testGetCenter_5_Double() {
		Assertions.assertEquals(new PolarCoordinate(165.5, 1.8849555921538759), board.getCenter(5, BoardSectionArea.DOUBLE));
	}

}
