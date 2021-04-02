package com.dartsmatcher.dartsmatcherapi.features.x01match.models.leg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01LegRound {

	private int round;

	private ArrayList<X01LegRoundScore> playerScores;

}
